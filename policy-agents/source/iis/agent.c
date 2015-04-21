/**
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014 - 2015 ForgeRock AS.
 */

#include "platform.h"
#include "version.h"
#ifdef __cplusplus
extern "C" {
#endif
#include "am.h"
#ifdef __cplusplus
}
#endif
#include <httpserv.h>

#define AM_MOD_SECTION                 L"system.webServer/OpenAmModule"
#define AM_MOD_SECTION_ENABLED         L"enabled"
#define AM_MOD_SECTION_CONFIGFILE      L"configFile"

static IHttpServer *server = NULL;
static void *modctx = NULL;
static am_status_t set_user(am_request_t *rq, const char *user);
static am_status_t set_custom_response(am_request_t *rq, const char *text, const char *cont_type);

struct process_wait {
    HANDLE wait;
    HANDLE proc;
    int pid;
};

static DWORD hr_to_winerror(HRESULT hr) {
    if ((hr & 0xFFFF0000) == MAKE_HRESULT(SEVERITY_ERROR, FACILITY_WIN32, 0)) {
        return HRESULT_CODE(hr);
    }
    if (hr == S_OK) {
        return ERROR_SUCCESS;
    }
    return ERROR_CAN_NOT_COMPLETE;
}

static void *alloc_request(IHttpContext *r, size_t s) {
    void *ret = (void *) r->AllocateRequestMemory((DWORD) s);
    if (ret != NULL) {
        memset(ret, 0, s);
    }
    return ret;
}

static char *utf8_encode(IHttpContext *r, const wchar_t *wstr, size_t *outlen) {
    char *tmp = NULL;
    int ret_len, out_len = wstr != NULL ?
            WideCharToMultiByte(CP_UTF8, 0, wstr, -1, NULL, 0, NULL, NULL) : 0;
    if (outlen) *outlen = 0;
    if (out_len > 0) {
        tmp = r == NULL ? (char *) malloc(out_len + 1) : (char *) alloc_request(r, out_len + 1);
        if (tmp != NULL) {
            memset(tmp, 0, out_len);
            ret_len = WideCharToMultiByte(CP_UTF8, 0, wstr, -1, tmp, (DWORD) out_len, NULL, NULL);
            if (outlen) *outlen = ret_len - 1;
        }
        return tmp;
    }
    return NULL;
}

static wchar_t *utf8_decode(IHttpContext *r, const char *str, size_t *outlen) {
    wchar_t *tmp = NULL;
    int ret_len, out_len = str != NULL ? MultiByteToWideChar(CP_UTF8, 0, str, -1, NULL, 0) : 0;
    if (outlen) *outlen = 0;
    if (out_len > 0) {
        tmp = (wchar_t *) alloc_request(r, sizeof (wchar_t) * out_len + 1);
        if (tmp != NULL) {
            memset(tmp, 0, sizeof (wchar_t) * out_len + 1);
            ret_len = MultiByteToWideChar(CP_UTF8, 0, str, -1, tmp, (DWORD) out_len);
            if (outlen) *outlen = ret_len - 1;
        }
        return tmp;
    }
    return NULL;
}

class OpenAMStoredConfig : public IHttpStoredContext{
    public :

    OpenAMStoredConfig() {
        enabled = FALSE;
        path = NULL;
        aconf = NULL;
    }

    ~OpenAMStoredConfig() {
        if (path != NULL) {
            delete [] path;
            path = NULL;
        }
        if (aconf != NULL) {
            am_config_free(&aconf);
            aconf = NULL;
        }
    }

    void CleanupStoredContext() {
        delete this;
    }

    BOOL IsEnabled() {
        return enabled;
    }

    char *GetPath(IHttpContext * r) {
        return utf8_encode(r, path, NULL);
    }

    am_config_t * GetBootConf() {
        return aconf;
    }

    static HRESULT GetConfig(IHttpContext *pContext, OpenAMStoredConfig **ppModuleConfig) {
        HRESULT hr = S_OK;
        OpenAMStoredConfig * pModuleConfig = NULL;
        IHttpModuleContextContainer * pMetadataContainer = NULL;
        IAppHostConfigException * pException = NULL;

        pMetadataContainer = pContext->GetMetadata()->GetModuleContextContainer();
        if (pMetadataContainer == NULL) {
            return E_UNEXPECTED;
        }

        pModuleConfig = (OpenAMStoredConfig *) pMetadataContainer->GetModuleContext(modctx);
        if (pModuleConfig != NULL) {
            *ppModuleConfig = pModuleConfig;
            return S_OK;
        }

        pModuleConfig = new OpenAMStoredConfig();
        if (pModuleConfig == NULL) {
            return E_OUTOFMEMORY;
        }

        hr = pModuleConfig->Initialize(pContext, &pException);
        if (FAILED(hr) || pException != NULL) {
            pModuleConfig->CleanupStoredContext();
            pModuleConfig = NULL;
            return E_UNEXPECTED;
        }

        hr = pMetadataContainer->SetModuleContext(pModuleConfig, modctx);
        if (FAILED(hr)) {
            pModuleConfig->CleanupStoredContext();
            pModuleConfig = NULL;
            if (hr == HRESULT_FROM_WIN32(ERROR_ALREADY_ASSIGNED)) {
                *ppModuleConfig = (OpenAMStoredConfig *) pMetadataContainer->GetModuleContext(modctx);
                return S_OK;
            }
        }

        *ppModuleConfig = pModuleConfig;
        return hr;
    }

    HRESULT Initialize(IHttpContext * pW3Context, IAppHostConfigException ** ppException) {
        HRESULT hr = S_OK;
        IAppHostAdminManager *pAdminManager = NULL;
        IAppHostElement *el = NULL;
        IAppHostPropertyException *excp = NULL;
        char *config = NULL;

        PCWSTR pszConfigPath = pW3Context->GetMetadata()->GetMetaPath();
        BSTR bstrUrlPath = SysAllocString(pszConfigPath);

        do {
            pAdminManager = server->GetAdminManager();
            if (pAdminManager == NULL) {
                hr = E_UNEXPECTED;
                break;
            }

            hr = pAdminManager->GetAdminSection(AM_MOD_SECTION, bstrUrlPath, &el);
            if (FAILED(hr)) {
                break;
            }
            if (el == NULL) {
                hr = E_UNEXPECTED;
                break;
            }

            hr = GetBooleanPropertyValue(el, AM_MOD_SECTION_ENABLED, &excp, &enabled);
            if (FAILED(hr)) {
                break;
            }
            if (excp != NULL) {
                ppException = (IAppHostConfigException**) & excp;
                hr = E_UNEXPECTED;
                break;
            }

            hr = GetStringPropertyValue(el, AM_MOD_SECTION_CONFIGFILE, &excp, &path);
            if (FAILED(hr)) {
                break;
            }
            if (excp != NULL) {
                ppException = (IAppHostConfigException**) & excp;
                hr = E_UNEXPECTED;
                break;
            }

            config = utf8_encode(NULL, path, NULL);
            if (config != NULL) {
                aconf = am_get_config_file(0, config);
                free(config);
            }


        } while (FALSE);

        SysFreeString(bstrUrlPath);
        return hr;
    }

private:

    HRESULT GetBooleanPropertyValue(IAppHostElement* pElement, WCHAR* pszPropertyName,
    IAppHostPropertyException** pException, BOOL * pBoolValue) {
        HRESULT hr = S_OK;
        IAppHostProperty *pProperty = NULL;
        VARIANT vPropertyValue;

        do {
            if (pElement == NULL || pszPropertyName == NULL ||
                    pException == NULL || pBoolValue == NULL) {
                hr = E_INVALIDARG;
                break;
            }

            hr = pElement->GetPropertyByName(pszPropertyName, &pProperty);
            if (FAILED(hr)) break;
            if (pProperty == NULL) {
                hr = E_UNEXPECTED;
                break;
            }

            VariantInit(&vPropertyValue);
            hr = pProperty->get_Value(&vPropertyValue);
            if (FAILED(hr)) break;

            *pException = NULL;
            hr = pProperty->get_Exception(pException);
            if (FAILED(hr)) break;
            if (*pException != NULL) {
                hr = E_UNEXPECTED;
                break;
            }

            *pBoolValue = (vPropertyValue.boolVal == VARIANT_TRUE) ? TRUE : FALSE;

        } while (FALSE);

        VariantClear(&vPropertyValue);
        if (pProperty != NULL) {
            pProperty->Release();
            pProperty = NULL;
        }
        return hr;
    }

    HRESULT GetStringPropertyValue(IAppHostElement* pElement, WCHAR* pszPropertyName,
    IAppHostPropertyException** pException, WCHAR ** ppszValue) {
        HRESULT hr = S_OK;
        IAppHostProperty *pProperty = NULL;
        DWORD dwLength;
        VARIANT vPropertyValue;

        do {

            if (pElement == NULL || pszPropertyName == NULL ||
                    pException == NULL || ppszValue == NULL) {
                hr = E_INVALIDARG;
                break;
            }

            *ppszValue = NULL;

            hr = pElement->GetPropertyByName(pszPropertyName, &pProperty);
            if (FAILED(hr)) break;
            if (pProperty == NULL) {
                hr = E_UNEXPECTED;
                break;
            }

            VariantInit(&vPropertyValue);
            hr = pProperty->get_Value(&vPropertyValue);
            if (FAILED(hr)) break;

            *pException = NULL;
            hr = pProperty->get_Exception(pException);
            if (FAILED(hr)) break;
            if (*pException != NULL) {
                hr = E_UNEXPECTED;
                break;
            }

            dwLength = SysStringLen(vPropertyValue.bstrVal);
            *ppszValue = new WCHAR[dwLength + 1];
            if (*ppszValue == NULL) {
                hr = E_OUTOFMEMORY;
                break;
            }

            wcsncpy(*ppszValue, vPropertyValue.bstrVal, dwLength);
            (*ppszValue)[dwLength] = L'\0';

        } while (FALSE);

        VariantClear(&vPropertyValue);
        if (pProperty != NULL) {
            pProperty->Release();
            pProperty = NULL;
        }
        return hr;
    }

    BOOL enabled;
    WCHAR *path;
    am_config_t *aconf;
};

class OpenAMHttpUser : public IHttpUser{
    public :

    virtual PCWSTR GetRemoteUserName(VOID) {
        return userName;
    }

    virtual PCWSTR GetUserName(VOID) {
        return userName;
    }

    virtual PCWSTR GetAuthenticationType(VOID) {
        return L"OpenAM";
    }

    virtual PCWSTR GetPassword(VOID) {
        return showPassword ? userPassword : L"";
    }

    virtual HANDLE GetImpersonationToken(VOID) {
        return hToken;
    }

    VOID SetImpersonationToken(HANDLE tkn) {
        hToken = tkn;
    }

    virtual HANDLE GetPrimaryToken(VOID) {
        return NULL;
    }

    virtual VOID ReferenceUser(VOID) {
        InterlockedIncrement(&m_refs);
    }

    virtual VOID DereferenceUser(VOID) {
        if (InterlockedDecrement(&m_refs) <= 0) {
            if (hToken) CloseHandle(hToken);
            delete this;
        }
    }

    virtual BOOL SupportsIsInRole(VOID) {
        return FALSE;
    }

    virtual HRESULT IsInRole(IN PCWSTR pszRoleName, OUT BOOL * pfInRole) {
        return E_NOTIMPL;
    }

    virtual PVOID GetUserVariable(IN PCSTR pszVariableName) {
        return NULL;
    }

    OpenAMHttpUser(PCWSTR usrn, PCWSTR usrp, PCWSTR usrpcrypted,
    BOOL showpass, BOOL dologon) : userName(usrn), userPassword(usrpcrypted),
    showPassword(showpass), status(FALSE), error(0) {
        HANDLE tkn = NULL;
        m_refs = 1;
        if (dologon) {
            if (usrn != NULL && usrp != NULL) {
                status = LogonUserW(usrn, NULL, usrp,
                        LOGON32_LOGON_NETWORK, LOGON32_PROVIDER_DEFAULT, &tkn);
                error = GetLastError();
                if (status) {
                    SetImpersonationToken(tkn);
                }
            } else {
                error = ERROR_INVALID_DATA;
            }
        } else {
            SetImpersonationToken(tkn);
            status = TRUE;
        }
    }

    BOOL GetStatus() {
        return status;
    }

    DWORD GetError() {
        return error;
    }

private:

    LONG m_refs;
    PCWSTR userName;
    PCWSTR userPassword;
    HANDLE hToken;
    BOOL status;
    BOOL showPassword;
    DWORD error;

    virtual ~OpenAMHttpUser() {
    }
};

static const char *get_server_variable(IHttpContext *ctx,
        unsigned long instance_id, PCSTR var) {
    const char* thisfunc = "get_server_variable():";
    PCSTR val = NULL;
    DWORD size = 0;

    if (!ISVALID(var)) return NULL;

    AM_LOG_DEBUG(instance_id, "%s trying to fetch server variable %s", thisfunc, var);

    if (FAILED(ctx->GetServerVariable(var, &val, &size))) {
        AM_LOG_WARNING(instance_id,
                "%s server variable %s is not available in HttpContext (error: %d)",
                thisfunc, var, GetLastError());
    } else {
        AM_LOG_DEBUG(instance_id, "%s found variable %s with value [%s]",
                thisfunc, var, LOGEMPTY(val));
    }
    return val;
}

static am_status_t get_request_url(am_request_t *rq) {
    IHttpContext *r = (IHttpContext *) (rq != NULL ? rq->ctx : NULL);
    HTTP_COOKED_URL url;
    am_status_t status = AM_EINVAL;
    if (r == NULL) return status;
    url = r->GetRequest()->GetRawHttpRequest()->CookedUrl;
    if (url.FullUrlLength > 0 && url.pFullUrl != NULL) {
        char *purl = NULL;
        size_t urlsz = 0;
        purl = utf8_encode(r, url.pFullUrl, &urlsz);
        if (purl != NULL) {
            char *urlc = (char *) alloc_request(r, urlsz + 1);
            if (urlc != NULL) {
                memcpy(urlc, purl, urlsz);
                rq->orig_url = urlc;
                status = AM_SUCCESS;
            }
        } else {
            status = AM_ENOMEM;
        }
    }
    return status;
}

static am_status_t set_header_in_request(am_request_t *rq, const char *key, const char *value) {
    static const char *thisfunc = "set_header_in_request():";
    IHttpContext *r = (IHttpContext *) (rq != NULL ? rq->ctx : NULL);
    HRESULT hr;
    if (r == NULL || !ISVALID(key)) return AM_EINVAL;

    AM_LOG_DEBUG(rq->instance_id, "%s %s = %s", thisfunc, LOGEMPTY(key), LOGEMPTY(value));

    /* remove all instances of the header first */
    hr = r->GetRequest()->DeleteHeader(key);
    if (FAILED(hr)) {
        AM_LOG_WARNING(rq->instance_id, "%s failed to delete request header %s (%d)", thisfunc,
                LOGEMPTY(key), hr_to_winerror(hr));
    }
    if (ISVALID(value)) {
        size_t key_sz = strlen(key);
        size_t value_sz = strlen(value);
        char *key_data = (char *) alloc_request(r, key_sz + 1);
        char *value_data = (char *) alloc_request(r, value_sz + 1);
        if (key_data != NULL && value_data != NULL) {
            memcpy(key_data, key, key_sz);
            key_data[key_sz] = 0;
            memcpy(value_data, value, value_sz);
            value_data[value_sz] = 0;
            hr = r->GetRequest()->SetHeader(key_data, value_data, (USHORT) value_sz, TRUE);
            if (FAILED(hr)) {
                AM_LOG_WARNING(rq->instance_id, "%s failed to set request header %s value %s (%d)", thisfunc,
                        LOGEMPTY(key), LOGEMPTY(value), hr_to_winerror(hr));
                return AM_ERROR;
            }
        } else {
            return AM_ENOMEM;
        }
    }
    return AM_SUCCESS;
}

static am_status_t set_cookie(am_request_t *rq, const char *header) {
    static const char *thisfunc = "set_cookie():";
    am_status_t status = AM_ERROR;
    const char *cookie;
    size_t header_sz = 0;
    char *header_data;
    HRESULT hr;
    IHttpContext *r = (IHttpContext *) (rq != NULL ? rq->ctx : NULL);
    if (r == NULL || !ISVALID(header)) return AM_EINVAL;

    header_sz = strlen(header);
    header_data = (char *) alloc_request(r, header_sz + 1);
    if (header_data != NULL) {
        memcpy(header_data, header, header_sz);
        header_data[header_sz] = 0;
        hr = r->GetResponse()->SetHeader("Set-Cookie", header_data, (USHORT) header_sz, FALSE);
        if (!FAILED(hr)) {
            status = AM_SUCCESS;
        } else {
            AM_LOG_WARNING(rq->instance_id, "%s failed to set response header Set-Cookie value %s (%d)", thisfunc,
                    LOGEMPTY(header_data), hr_to_winerror(hr));
        }
    } else {
        status = AM_ENOMEM;
    }

    cookie = get_server_variable(r, rq->instance_id, "HTTP_COOKIE");
    if (cookie == NULL) {
        status = set_header_in_request(rq, "Cookie", header);
    } else {
        size_t cookie_sz = strlen(cookie);
        char *new_cookie = (char *) alloc_request(r, cookie_sz + header_sz + 3);
        if (new_cookie != NULL) {
            strcpy(new_cookie, cookie);
            strcat(new_cookie, "; ");
            strcat(new_cookie, header);
            status = set_header_in_request(rq, "Cookie", new_cookie);
        } else {
            status = AM_ENOMEM;
        }
    }
    return status;
}

static am_status_t add_header_in_response(am_request_t *rq, const char *key, const char *value) {
    static const char *thisfunc = "add_header_in_response():";
    am_status_t status = AM_ERROR;
    size_t key_sz, value_sz;
    char *key_data, *value_data;
    HRESULT hr;
    IHttpContext *r = (IHttpContext *) (rq != NULL ? rq->ctx : NULL);
    if (r == NULL || !ISVALID(key)) return AM_EINVAL;
    if (!ISVALID(value)) {
        /*value is empty, sdk is setting a cookie in response*/
        return set_cookie(rq, key);
    }
    key_sz = strlen(key);
    value_sz = strlen(value);
    key_data = (char *) alloc_request(r, key_sz + 1);
    value_data = (char *) alloc_request(r, value_sz + 1);
    if (key_data != NULL && value_data != NULL) {
        memcpy(key_data, key, key_sz);
        key_data[key_sz] = 0;
        memcpy(value_data, value, value_sz);
        value_data[value_sz] = 0;
        hr = r->GetResponse()->SetHeader(key_data, value_data, (USHORT) value_sz, FALSE);
        if (!FAILED(hr)) {
            status = AM_SUCCESS;
        } else {
            AM_LOG_WARNING(rq->instance_id, "%s failed to set response header %s value %s (%d)", thisfunc,
                    LOGEMPTY(key), LOGEMPTY(value), hr_to_winerror(hr));
        }
    } else {
        status = AM_ENOMEM;
    }
    return status;
}

static REQUEST_NOTIFICATION_STATUS am_status_value(IHttpContext *ctx, am_status_t v) {
    IHttpResponse *res = ctx->GetResponse();
    switch (v) {
        case AM_SUCCESS:
            return RQ_NOTIFICATION_CONTINUE;
        case AM_EAGAIN:
            return RQ_NOTIFICATION_PENDING;
        case AM_PDP_DONE:
        case AM_DONE:
            return RQ_NOTIFICATION_FINISH_REQUEST;
        case AM_NOT_HANDLING:
            return RQ_NOTIFICATION_CONTINUE;
        case AM_NOT_FOUND:
            res->SetStatus(404, "Not Found");
            return RQ_NOTIFICATION_FINISH_REQUEST;
        case AM_REDIRECT:
            res->SetStatus(302, "Found");
            return RQ_NOTIFICATION_FINISH_REQUEST;
        case AM_FORBIDDEN:
            res->SetStatus(403, "Forbidden");
            return RQ_NOTIFICATION_FINISH_REQUEST;
        case AM_BAD_REQUEST:
            res->SetStatus(400, "Bad Request");
            return RQ_NOTIFICATION_FINISH_REQUEST;
        case AM_ERROR:
            res->SetStatus(500, "Internal Server Error");
            return RQ_NOTIFICATION_FINISH_REQUEST;
        case AM_NOT_IMPLEMENTED:
            res->SetStatus(501, "Not Implemented");
            return RQ_NOTIFICATION_FINISH_REQUEST;
        default:
            res->SetStatus(500, "Internal Server Error");
            return RQ_NOTIFICATION_FINISH_REQUEST;
    }
}

static am_status_t set_method(am_request_t *rq) {
    IHttpContext *r = (IHttpContext *) (rq != NULL ? rq->ctx : NULL);
    if (r == NULL) return AM_EINVAL;
    if (FAILED(r->GetRequest()->SetHttpMethod(am_method_num_to_str(rq->method)))) {
        return AM_ERROR;
    }
    return AM_SUCCESS;
}

static am_status_t set_request_body(am_request_t *rq) {
    static const char *thisfunc = "set_request_body():";
    IHttpContext *r = (IHttpContext *) (rq != NULL ? rq->ctx : NULL);
    am_status_t status = AM_SUCCESS;
    HRESULT hr = S_OK;
    if (r == NULL) return AM_EINVAL;
    if (ISVALID(rq->post_data) && rq->post_data_sz > 0) {
        void *body = alloc_request(r, rq->post_data_sz);
        if (body != NULL) {
            memcpy(body, rq->post_data, rq->post_data_sz);
            hr = r->GetRequest()->InsertEntityBody(body, (DWORD) rq->post_data_sz);
            if (hr != S_OK) {
                status = AM_ERROR;
            }
        } else {
            status = AM_ENOMEM;
        }
    }
    if (status != AM_SUCCESS) {
        AM_LOG_WARNING(rq->instance_id, "%s status %s (%d)", thisfunc,
                am_strerror(status), hr_to_winerror(hr));
    }
    return status;
}

static am_status_t get_request_body(am_request_t *rq) {
    static const char *thisfunc = "get_request_body():";
    IHttpContext *ctx = (IHttpContext *) (rq != NULL ? rq->ctx : NULL);
    IHttpRequest *r = ctx != NULL ? ctx->GetRequest() : NULL;
    DWORD read_bytes = 0, rc = 1024;
    HRESULT hr;
    am_status_t status = AM_ERROR;
    char *out = NULL, *out_tmp;
    void *data;

    if (r == NULL) return AM_EINVAL;
    data = alloc_request(ctx, rc);
    if (data == NULL) return AM_ENOMEM;

    if (r->GetRemainingEntityBytes() > 0) {
        while (r->GetRemainingEntityBytes() != 0) {
            hr = r->ReadEntityBody(data, rc, FALSE, &rc, NULL);
            if (FAILED(hr)) {
                if (ERROR_HANDLE_EOF != (hr & 0x0000FFFF)) {
                    am_free(out);
                    return AM_ERROR;
                }
            }
            out_tmp = (char *) realloc(out, read_bytes + rc + 1);
            if (out_tmp == NULL) {
                am_free(out);
                return AM_ENOMEM;
            } else {
                out = out_tmp;
            }
            memcpy(out + read_bytes, data, rc);
            read_bytes += rc;
            out[read_bytes] = 0;
            status = AM_SUCCESS;
        }
        rq->post_data = out;
        rq->post_data_sz = read_bytes;
    }

    if (status == AM_SUCCESS) {
        AM_LOG_DEBUG(rq->instance_id, "%s read %d bytes \n%s", thisfunc,
                read_bytes, LOGEMPTY(out));
        r->DeleteHeader("CONTENT_LENGTH");
    }
    return status;
}

static am_status_t des_decrypt(const char *encrypted, const char *keys, char **clear) {
    am_status_t status = AM_ERROR;
    HCRYPTPROV hCryptProv;
    HCRYPTKEY hKey;
    BYTE IV[8] = {0, 0, 0, 0, 0, 0, 0, 0};
    BYTE bKey[20], *data = NULL, *key = NULL;
    DWORD keyLen = 8, dataLen = 0;
    int i;
    BLOBHEADER keyHeader;
    size_t enc_sz = strlen(encrypted);
    size_t key_sz = strlen(keys);

    data = (BYTE *) base64_decode(encrypted, &enc_sz);
    dataLen = (DWORD) enc_sz;
    key = (BYTE *) base64_decode(keys, &key_sz);
    if (dataLen > 0 && key_sz > 0) {
        keyHeader.bType = PLAINTEXTKEYBLOB;
        keyHeader.bVersion = CUR_BLOB_VERSION;
        keyHeader.reserved = 0;
        keyHeader.aiKeyAlg = CALG_DES;
        for (i = 0; i<sizeof (keyHeader); i++) {
            bKey[i] = *((BYTE*) & keyHeader + i);
        }
        for (i = 0; i<sizeof (keyLen); i++) {
            bKey[i + sizeof (keyHeader)] = *((BYTE*) & keyLen + i);
        }
        for (i = 0; i < 8; i++) {
            bKey[i + sizeof (keyHeader) + sizeof (keyLen)] = key[i];
        }
        if (CryptAcquireContext(&hCryptProv, NULL, NULL, PROV_RSA_AES, CRYPT_VERIFYCONTEXT)) {
            if (CryptImportKey(hCryptProv, (BYTE*) & bKey, sizeof (keyHeader) + sizeof (DWORD) + 8, 0, 0, &hKey)) {
                DWORD desMode = CRYPT_MODE_ECB;
                CryptSetKeyParam(hKey, KP_MODE, (BYTE*) & desMode, 0);
                DWORD padding = ZERO_PADDING;
                CryptSetKeyParam(hKey, KP_PADDING, (BYTE*) & padding, 0);
                CryptSetKeyParam(hKey, KP_IV, &IV[0], 0);
                if (CryptDecrypt(hKey, 0, FALSE, 0, data, &dataLen)) {
                    *clear = (char *) calloc(1, (size_t) dataLen + 1);
                    memcpy(*clear, data, (size_t) dataLen);
                    (*clear)[dataLen] = 0;
                    status = AM_SUCCESS;
                }
            }
        }
        CryptDestroyKey(hKey);
        CryptReleaseContext(hCryptProv, 0);
    }
    am_free(data);
    am_free(key);
    return status;
}

class OpenAMHttpModule : public CHttpModule{
    public :

    OpenAMHttpModule(HANDLE elog) {
        eventLog = elog;
        doLogOn = FALSE;
        showPassword = FALSE;
        userName = NULL;
        userPassword = NULL;
        userPasswordCrypted = NULL;
        userPasswordSize = 0;
        userPasswordCryptedSize = 0;
        clonedContext = NULL;
    }

    REQUEST_NOTIFICATION_STATUS OnAsyncCompletion(IHttpContext * ctx, DWORD dwNotification,
    BOOL fPostNotification, IHttpEventProvider * prov, IHttpCompletionInfo * pCompletionInfo) {
        if (clonedContext != NULL) {
            clonedContext->ReleaseClonedContext();
            clonedContext = NULL;
        }
        return RQ_NOTIFICATION_CONTINUE;
    }

    REQUEST_NOTIFICATION_STATUS OnBeginRequest(IHttpContext *ctx,
    IHttpEventProvider * prov) {
        static const char *thisfunc = "OpenAMHttpModule():";
        REQUEST_NOTIFICATION_STATUS status = RQ_NOTIFICATION_CONTINUE;
        IHttpRequest *req = ctx->GetRequest();
        IHttpResponse *res = ctx->GetResponse();
        IHttpSite *site = ctx->GetSite();
        HRESULT hr = S_OK;
        int rv;
        am_request_t d;
        const am_config_t *boot = NULL;
        am_config_t *rq_conf = NULL;
        OpenAMStoredConfig *conf = NULL;
        char ip[INET6_ADDRSTRLEN];

        /* agent module is not enabled for this 
         * server/site - we are not handling this request
         **/
        hr = OpenAMStoredConfig::GetConfig(ctx, &conf);
        if (FAILED(hr)) {
            WriteEventLog("%s GetConfig failed", thisfunc);
            return RQ_NOTIFICATION_CONTINUE;
        }
        if (conf->IsEnabled() == FALSE) {
            WriteEventLog("%s GetConfig config is not enabled for %d", thisfunc, site->GetSiteId());
            return RQ_NOTIFICATION_CONTINUE;
        }

        boot = conf->GetBootConf();
        if (boot != NULL) {
            /* register and update instance logger configuration (for already registered
             * instances - update logging level only)
             */
            am_log_register_instance(site->GetSiteId(), boot->debug_file, boot->debug_level,
                    boot->audit_file, boot->audit_level);
        } else {
            WriteEventLog("%s GetConfig boot == NULL (%d)", thisfunc, site->GetSiteId());
            res->SetStatus(500, "Internal Server Error");
            return RQ_NOTIFICATION_FINISH_REQUEST;
        }

        AM_LOG_DEBUG(site->GetSiteId(), "%s begin", thisfunc);

        res->DisableKernelCache(9);

        rv = am_get_agent_config(site->GetSiteId(), conf->GetPath(ctx), &rq_conf);
        if (rq_conf == NULL || rv != AM_SUCCESS) {
            WriteEventLog("%s am_get_agent_config failed (%d)", thisfunc, site->GetSiteId());
            AM_LOG_ERROR(site->GetSiteId(), "%s failed to get agent configuration instance, error: %s",
                    thisfunc, am_strerror(rv));
            res->SetStatus(403, "Forbidden");
            return RQ_NOTIFICATION_FINISH_REQUEST;
        }

        /* set up request processor data structure */
        memset(&d, 0, sizeof (am_request_t));
        d.conf = rq_conf;
        d.status = AM_ERROR;
        d.instance_id = site->GetSiteId();
        d.ctx = ctx;
        d.ctx_class = this; /*set_user use only*/
        d.method = am_method_str_to_num(req->GetHttpMethod());
        d.content_type = get_server_variable(ctx, d.instance_id, "CONTENT_TYPE");
        d.cookies = get_server_variable(ctx, d.instance_id, "HTTP_COOKIE");

        if (ISVALID(d.conf->client_ip_header)) {
            d.client_ip = (char *) get_server_variable(ctx, d.instance_id,
                    d.conf->client_ip_header);
        }
        if (!ISVALID(d.client_ip)) {
            unsigned long s = sizeof (ip);
            PSOCKADDR sa = req->GetRemoteAddress();
            if (sa != NULL) {
                memset(&ip[0], 0, sizeof (ip));
                if (sa->sa_family == AF_INET) {
                    struct sockaddr_in *ipv4 = reinterpret_cast<struct sockaddr_in *>(sa);
                    if (WSAAddressToStringA((LPSOCKADDR) ipv4, sizeof (*ipv4), NULL, ip, &s) == 0) {
                        char *b = strchr(ip, ':');
                        if (b != NULL) *b = 0;
                        d.client_ip = ip;
                    }
                } else {
                    struct sockaddr_in6 *ipv6 = reinterpret_cast<struct sockaddr_in6 *>(sa);
                    if (WSAAddressToStringA((LPSOCKADDR) ipv6, sizeof (*ipv6), NULL, ip, &s) == 0) {
                        char *b;
                        if (ip[0] == '[') {
                            memmove(ip, ip + 1, s - 2);
                        }
                        b = strchr(ip, ']');
                        if (b != NULL) *b = 0;
                        d.client_ip = ip;
                    }
                }
            }
        }
        if (ISVALID(d.conf->client_hostname_header)) {
            d.client_host = (char *) get_server_variable(ctx, d.instance_id,
                    d.conf->client_hostname_header);
        }

        d.am_get_request_url_f = get_request_url;
        d.am_get_post_data_f = get_request_body;
        d.am_set_post_data_f = set_request_body;
        d.am_set_user_f = set_user;
        d.am_set_header_in_request_f = set_header_in_request;
        d.am_add_header_in_response_f = add_header_in_response;
        d.am_set_cookie_f = set_cookie;
        d.am_set_custom_response_f = set_custom_response;

        am_process_request(&d);

        am_config_free(&d.conf);
        am_request_free(&d);

        status = am_status_value(ctx, d.status);
        AM_LOG_DEBUG(site->GetSiteId(), "%s exit status: %s (%d)", thisfunc, am_strerror(d.status), d.status);
        return status;
    }

    REQUEST_NOTIFICATION_STATUS OnAuthenticateRequest(IHttpContext *ctx,
    IAuthenticationProvider * prov) {
        IHttpUser *currentUser = ctx->GetUser();
        IHttpResponse *res = ctx->GetResponse();
        IHttpSite *site = ctx->GetSite();
        if (currentUser == NULL) {
            if (userName != NULL) {
                PCWSTR user = utf8_decode(ctx, userName, NULL);
                OpenAMHttpUser *httpUser = new OpenAMHttpUser(user, userPassword,
                        userPasswordCrypted, showPassword, doLogOn);
                if (httpUser == NULL || !httpUser->GetStatus()) {
                    AM_LOG_ERROR(site->GetSiteId(), "OpenAMHttpModule(): failed (invalid Windows/AD user credentials). "
                            "Responding with HTTP403 error (%d)", httpUser->GetStatus());
                    res->SetStatus(403, "Forbidden");
                    return RQ_NOTIFICATION_FINISH_REQUEST;
                } else {
                    AM_LOG_DEBUG(site->GetSiteId(), "OpenAMHttpModule(): context user set to \"%s\"", userName);
                }
                prov->SetUser(httpUser);
            }
        }
        return RQ_NOTIFICATION_CONTINUE;
    }

    REQUEST_NOTIFICATION_STATUS OnEndRequest(IHttpContext *ctx,
    IHttpEventProvider * pProvider) {
        if (userPassword != NULL && userPasswordSize > 0) {
            SecureZeroMemory((PVOID) userPassword, userPasswordSize);
        }
        if (userPasswordCrypted != NULL && userPasswordCryptedSize > 0) {
            SecureZeroMemory((PVOID) userPasswordCrypted, userPasswordCryptedSize);
        }
        return RQ_NOTIFICATION_CONTINUE;
    }

    void WriteEventLog(const char *format, ...) {
        va_list args;
        va_start(args, format);
        WriteEvent(format, args);
        va_end(args);
    }

    void WriteEvent(const char *format, va_list argList) {
        int count = _vscprintf(format, argList);
        if (count > 0) {
            char *formattedString = (char *) malloc(count + 1);
            if (formattedString != NULL) {
                vsprintf(formattedString, format, argList);
                if (eventLog != NULL) {
                    ReportEvent(eventLog, EVENTLOG_INFORMATION_TYPE, 0, 0,
                            NULL, 1, 0, (LPCTSTR *) & formattedString, NULL);
                }
                free(formattedString);
            }
        }
    }

    HANDLE eventLog;
    char *userName;
    wchar_t *userPassword;
    DWORD userPasswordSize;
    wchar_t *userPasswordCrypted;
    DWORD userPasswordCryptedSize;
    BOOL showPassword;
    BOOL doLogOn;
    IHttpContext *clonedContext;
};

static am_status_t set_user(am_request_t *rq, const char *user) {
    static const char *thisfunc = "set_user():";
    IHttpContext *r = (IHttpContext *) (rq != NULL ? rq->ctx : NULL);
    OpenAMHttpModule *m = rq != NULL && rq->ctx_class != NULL ?
            static_cast<OpenAMHttpModule *>(rq->ctx_class) : NULL;
    if (m == NULL || r == NULL) return AM_EINVAL;
    if (ISVALID(user)) {
        size_t usz = strlen(user);
        m->userName = (char *) alloc_request(r, usz + 1);
        if (m->userName != NULL) {
            memcpy(m->userName, user, usz);
            m->userName[usz] = 0;
        }
    }
    if (ISVALID(rq->user_password) && ISVALID(rq->conf->password_replay_key)) {
        char *user_passwd = NULL;
        if (des_decrypt(rq->user_password, rq->conf->password_replay_key, &user_passwd) == AM_SUCCESS) {
            m->userPassword = utf8_decode(r, user_passwd, (size_t *) & m->userPasswordSize);
        }
        am_free(user_passwd);
        m->userPasswordCrypted = utf8_decode(r, rq->user_password, (size_t *) & m->userPasswordCryptedSize);
    }
    m->doLogOn = rq->conf->logon_user_enable ? TRUE : FALSE;
    m->showPassword = rq->conf->password_header_enable ? TRUE : FALSE;
    return AM_SUCCESS;
}

static am_status_t set_custom_response(am_request_t *rq, const char *text, const char *cont_type) {
    static const char *thisfunc = "set_custom_response():";
    HRESULT hr;
    IHttpContext *r = (IHttpContext *) (rq != NULL ? rq->ctx : NULL);
    OpenAMHttpModule *m = rq != NULL && rq->ctx_class != NULL ?
            static_cast<OpenAMHttpModule *>(rq->ctx_class) : NULL;
    am_status_t status = AM_ERROR;
    if (r == NULL) {
        return AM_EINVAL;
    }
    status = rq->status;
    switch (status) {
        case AM_JSON_RESPONSE:
        {

        }
            break;
        case AM_INTERNAL_REDIRECT:
        case AM_REDIRECT:
        {
            hr = r->GetResponse()->Redirect(text, TRUE, FALSE);
            if (FAILED(hr)) {
                AM_LOG_ERROR(rq->instance_id, "set_custom_response(): failed to issue a redirect to %s (%d)",
                        text, hr_to_winerror(hr));
                rq->status = AM_ERROR;
                break;
            }
            rq->status = AM_DONE;
        }
            break;
        case AM_PDP_DONE:
        {
            r->GetResponse()->Redirect(rq->post_data_url, TRUE, FALSE);
            rq->status = AM_DONE;
        }
            break;
        default:
        {
            HTTP_DATA_CHUNK dc;
            DWORD sent;
            char tls[64];
            size_t tl = strlen(text);
            snprintf(tls, sizeof (tls), "%d", tl);
            r->GetResponse()->Clear();
            if (ISVALID(cont_type)) {
                hr = r->GetResponse()->SetHeader("Content-Type", cont_type,
                        (USHORT) strlen(cont_type), TRUE);
            }
            hr = r->GetResponse()->SetHeader("Content-Length", tls, (USHORT) strlen(tls), TRUE);
            if (rq->status == AM_SUCCESS || rq->status == AM_DONE) {
                hr = r->GetResponse()->SetStatus(200, "OK", 0, S_OK);
            } else {
                am_status_value(r, rq->status);
            }
            dc.DataChunkType = HttpDataChunkFromMemory;
            dc.FromMemory.pBuffer = (PVOID) text;
            dc.FromMemory.BufferLength = (USHORT) tl;
            hr = r->GetResponse()->WriteEntityChunks(&dc, 1, FALSE, TRUE, &sent);
            rq->status = AM_DONE;
        }
            break;
    }
    AM_LOG_INFO(rq->instance_id, "set_custom_response(): status: %s (exit: %s)",
            am_strerror(status), am_strerror(rq->status));
    return AM_SUCCESS;
}

class OpenAMHttpModuleFactory : public IHttpModuleFactory{
    public :

    OpenAMHttpModuleFactory() {
        eventLog = RegisterEventSource(NULL, "IISADMIN");
        am_init_worker();
    }

    ~OpenAMHttpModuleFactory() {
        if (eventLog != NULL) {
            DeregisterEventSource(eventLog);
            eventLog = NULL;
        }
    }

    virtual HRESULT GetHttpModule(CHttpModule **mm, IModuleAllocator * ma) {
        OpenAMHttpModule *mod = NULL;

        if (mm == NULL) {
            return HRESULT_FROM_WIN32(ERROR_INVALID_PARAMETER);
        }

        mod = new OpenAMHttpModule(eventLog);
        if (mod == NULL) {
            return HRESULT_FROM_WIN32(ERROR_NOT_ENOUGH_MEMORY);
        }

        *mm = mod;
        mod = NULL;

        return S_OK;
    }

    virtual void Terminate() {
        am_shutdown_worker();
        am_shutdown();
        delete this;
    }

private:

    HANDLE eventLog;
};

static VOID CALLBACK WaitProcessExitCallback(PVOID lpParameter, BOOLEAN TimerOrWaitFired) {
    struct process_wait *cb = (struct process_wait *) lpParameter;
    am_re_init_worker();
    CloseHandle(cb->proc);
    UnregisterWait(cb->wait);
    free(cb);
}

class OpenAMAppModule : public CGlobalModule{
    public :

    virtual GLOBAL_NOTIFICATION_STATUS OnGlobalApplicationPreload(
    IGlobalApplicationPreloadProvider * pProvider) {
        HRESULT status = S_OK;
        IGlobalApplicationPreloadProvider2 * prov = NULL;
        IHttpContext *ctx = NULL;
        status = pProvider->CreateContext(&ctx);
        if (SUCCEEDED(status)) {
            IHttpSite *site = ctx->GetSite();
            status = HttpGetExtendedInterface(server, pProvider, &prov);
            if (SUCCEEDED(status)) {
                if (prov->IsProcessRecycled()) {
                    struct process_wait *pw = (struct process_wait *)
                            malloc(sizeof (struct process_wait));
                    pw->pid = am_log_get_current_owner();
                    pw->proc = OpenProcess(PROCESS_ALL_ACCESS, FALSE, pw->pid);
                    RegisterWaitForSingleObject(&pw->wait, pw->proc,
                            WaitProcessExitCallback, pw, INFINITE, WT_EXECUTEONLYONCE);
                }
            }
        }
        if (ctx != NULL) {
            ctx->ReleaseClonedContext();
            ctx = NULL;
        }
        return GL_NOTIFICATION_CONTINUE;
    }

    virtual void Terminate() {
        delete this;
    }
};

HRESULT __stdcall RegisterModule(DWORD dwServerVersion,
        IHttpModuleRegistrationInfo *pModuleInfo, IHttpServer *pHttpServer) {
    HRESULT status = S_OK;
    OpenAMHttpModuleFactory *modf = NULL;
    OpenAMAppModule *app = NULL;
    UNREFERENCED_PARAMETER(dwServerVersion);

    do {

        if (pModuleInfo == NULL || pHttpServer == NULL) {
            status = HRESULT_FROM_WIN32(ERROR_INVALID_PARAMETER);
            break;
        }

        modctx = pModuleInfo->GetId();
        server = pHttpServer;

        modf = new OpenAMHttpModuleFactory();
        if (modf == NULL) {
            status = HRESULT_FROM_WIN32(ERROR_NOT_ENOUGH_MEMORY);
            break;
        }

        status = pModuleInfo->SetRequestNotifications(modf,
                RQ_BEGIN_REQUEST | RQ_AUTHENTICATE_REQUEST, 0);
        if (FAILED(status)) {
            break;
        }
        status = pModuleInfo->SetPriorityForRequestNotification(RQ_BEGIN_REQUEST,
                PRIORITY_ALIAS_HIGH);
        if (FAILED(status)) {
            break;
        }

        app = new OpenAMAppModule();
        if (app == NULL) {
            status = HRESULT_FROM_WIN32(ERROR_NOT_ENOUGH_MEMORY);
            break;
        }

        status = pModuleInfo->SetGlobalNotifications(app, GL_APPLICATION_PRELOAD);
        if (FAILED(status)) {
            break;
        }

        modf = NULL;
        app = NULL;

    } while (FALSE);

    if (modf != NULL) {
        delete modf;
        modf = NULL;
    }

    if (app != NULL) {
        delete app;
        modf = NULL;
    }

    return status;
}
