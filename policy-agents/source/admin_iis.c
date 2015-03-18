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

#ifdef _WIN32

#define WIN32_LEAN_AND_MEAN
#define COBJMACROS

#include "platform.h"
#include <objbase.h>
#include <oleauto.h>
#include <ahadmin.h>

typedef enum {
    MODE_UNKNOWN,
    MODE_X86,
    MODE_X64
} app_mode_t;

#define IIS_SCHEMA_CONF_FILE "\\System32\\inetsrv\\config\\schema\\mod_iis_openam_schema.xml"
#define AM_IIS_APPHOST L"MACHINE/WEBROOT/APPHOST"
#define AM_IIS_SITES L"system.applicationHost/sites"
#define AM_IIS_GLOBAL L"system.webServer/globalModules"
#define AM_IIS_MODULES L"system.webServer/modules"
#define AM_IIS_MODULE_CONF L"system.webServer/OpenAmModule"
#define AM_IIS_ENAME L"name"
#define AM_IIS_EADD L"add"
#define AM_IIS_EID L"id"

static BSTR module_name = L"OpenAmModule";
static BSTR system_webserver = L"system.webServer";

char *utf8_encode(const wchar_t *wstr, size_t *outlen) {
    char *tmp = NULL;
    size_t out_len = WideCharToMultiByte(CP_UTF8, 0, wstr, -1, NULL, 0, NULL, NULL);
    if (outlen) *outlen = 0;
    if (out_len > 0) {
        tmp = (char *) malloc(out_len);
        WideCharToMultiByte(CP_UTF8, 0, wstr, -1, tmp, (DWORD) out_len, NULL, NULL);
        tmp[out_len - 1] = 0;
        if (outlen) *outlen = out_len - 1;
        return tmp;
    }
    return NULL;
}

wchar_t *utf8_decode(const char *str, size_t *outlen) {
    wchar_t *tmp = NULL;
    size_t out_len = MultiByteToWideChar(CP_UTF8, 0, str, -1, NULL, 0);
    if (outlen) *outlen = 0;
    if (out_len > 0) {
        tmp = (wchar_t *) malloc(sizeof (wchar_t) * out_len);
        MultiByteToWideChar(CP_UTF8, 0, str, -1, tmp, (DWORD) out_len);
        tmp[out_len - 1] = 0;
        if (outlen) *outlen = out_len - 1;
        return tmp;
    }
    return NULL;
}

static app_mode_t get_app_mode() {
    SYSTEM_INFO sys_info;
    GetNativeSystemInfo(&sys_info);
    if (sys_info.wProcessorArchitecture == PROCESSOR_ARCHITECTURE_INTEL) {
        return MODE_X86;
    } else if (sys_info.wProcessorArchitecture == PROCESSOR_ARCHITECTURE_AMD64) {
        return MODE_X64;
    }
    return MODE_UNKNOWN;
}

static char *get_property_value_byname(IAppHostElement* ahe, VARIANT* value, BSTR* name, VARTYPE type) {
    IAppHostProperty *property = NULL;
    HRESULT hresult;
    char *ret = NULL;
    hresult = IAppHostElement_GetPropertyByName(ahe, *name, &property);
    if (FAILED(hresult) || property == NULL) {
        fwprintf(stderr, L"get_property_value_byname(%s) failed. Property not found.\n", *name);
        return NULL;
    }
    hresult = IAppHostProperty_get_Value(property, value);
    if (FAILED(hresult)) {
        fwprintf(stderr, L"get_property_value_byname(%s) failed. Value not set.\n", *name);
        if (property != NULL) IAppHostProperty_Release(property);
        return NULL;
    }
    if (value->vt != type) {
        fwprintf(stderr, L"get_property_value_byname(%s) failed. Property type %d differs from type expected %d.",
                *name, value->vt, type);
        if (property != NULL) IAppHostProperty_Release(property);
        return NULL;
    }

    if (type == VT_UI4) {
        char dec[256];
        sprintf_s(dec, sizeof (dec), "%d", value->ulVal);
        ret = strdup(dec);
    } else {
        UINT l = SysStringLen(value->bstrVal);
        if (l > 0) {
            wchar_t *tmp = (LPWSTR) malloc(l * sizeof (wchar_t) + 2);
            if (tmp) {
                memcpy(tmp, value->bstrVal, l * sizeof (wchar_t));
                tmp[l] = 0;
                ret = utf8_encode(tmp, NULL);
                free(tmp);
            }
        }
    }
    if (property != NULL) IAppHostProperty_Release(property);
    return ret;
}

void list_iis_sites(int argc, char **argv) {
    IAppHostWritableAdminManager *admin_manager = NULL;
    IAppHostElement *he = NULL, *ce = NULL;
    IAppHostElementCollection *hec = NULL;
    DWORD num;
    UINT i;
    BSTR bstr_config_path = SysAllocString(AM_IIS_APPHOST);
    BSTR bstr_sites = SysAllocString(AM_IIS_SITES);
    BSTR bstr_name = SysAllocString(AM_IIS_ENAME);
    BSTR bstr_id = SysAllocString(AM_IIS_EID);
    VARIANT value;
    HRESULT hresult = S_OK;
    char *name, *id;

    fprintf(stdout, "\nIIS Server Site configuration:\n");

    do {
        hresult = CoInitializeEx(NULL, COINIT_MULTITHREADED);
        if (FAILED(hresult)) {
            break;
        }
        hresult = CoCreateInstance(&CLSID_AppHostWritableAdminManager, NULL,
                CLSCTX_INPROC_SERVER, &IID_IAppHostWritableAdminManager, (LPVOID*) & admin_manager);
        if (FAILED(hresult)) {
            break;
        }
        hresult = IAppHostWritableAdminManager_GetAdminSection(admin_manager, bstr_sites, bstr_config_path, &he);
        if (FAILED(hresult) || he == NULL) {
            break;
        }
        hresult = IAppHostElement_get_Collection(he, &hec);
        if (FAILED(hresult)) {
            break;
        }
        hresult = IAppHostElementCollection_get_Count(hec, &num);
        if (FAILED(hresult)) {
            break;
        }

        fprintf(stdout, "\nNumber of Sites: %d\n\n", num);

        for (i = 0; i < num; i++) {
            VARIANT index;
            index.vt = VT_UINT;
            index.uintVal = i;
            hresult = IAppHostElementCollection_get_Item(hec, index, &ce);
            if (SUCCEEDED(hresult)) {
                name = get_property_value_byname(ce, &value, &bstr_name, VT_BSTR);
                id = get_property_value_byname(ce, &value, &bstr_id, VT_UI4);
                if (name != NULL && id != NULL) {
                    char *p;
                    for (p = name; *p != '\0'; ++p) {
                        *p = toupper(*p);
                    }
                    fprintf(stdout, "id: %s\tname: \"%s\"\n", id, name);
                }
                if (name != NULL) free(name);
                if (id != NULL) free(id);
                name = NULL;
                id = NULL;
            }
            if (ce != NULL) IAppHostElement_Release(ce);
        }

    } while (FALSE);

    if (he != NULL) IAppHostElement_Release(he);
    if (hec != NULL) IAppHostElementCollection_Release(hec);
    if (admin_manager != NULL) IAppHostWritableAdminManager_Release(admin_manager);
    SysFreeString(bstr_config_path);
    SysFreeString(bstr_sites);
    SysFreeString(bstr_name);
    SysFreeString(bstr_id);
    CoUninitialize();
}

static BOOL set_property(IAppHostElement* element, BSTR name, BSTR value) {
    IAppHostProperty* property = NULL;
    BSTR bstr_name = SysAllocString(name);
    BSTR bstr_value = SysAllocString(value);
    HRESULT hresult = S_OK;
    do {
        VARIANT value_variant;
        hresult = IAppHostElement_GetPropertyByName(element, bstr_name, &property);
        if (FAILED(hresult)) {
            fprintf(stderr, "Failed to get property.\n");
            break;
        }

        value_variant.vt = VT_BSTR;
        value_variant.bstrVal = bstr_value;
        hresult = IAppHostProperty_put_Value(property, value_variant);
        if (FAILED(hresult)) {
            fprintf(stderr, "Failed to set property value.\n");
            break;
        }
    } while (FALSE);

    if (property != NULL) IAppHostProperty_Release(property);
    SysFreeString(bstr_name);
    SysFreeString(bstr_value);

    return SUCCEEDED(hresult);
}

static BOOL get_property(IAppHostElement* element, BSTR name, VARIANT* value) {
    IAppHostProperty* property = NULL;
    BSTR bstr_name = SysAllocString(name);
    HRESULT hresult = S_OK;
    do {
        hresult = IAppHostElement_GetPropertyByName(element, bstr_name, &property);
        if (FAILED(hresult)) {
            fprintf(stderr, "Failed to get property.\n");
            break;
        }
        hresult = IAppHostProperty_get_Value(property, value);
        if (FAILED(hresult)) {
            fprintf(stderr, "Failed to get property value.\n");
            break;
        }
    } while (FALSE);

    if (property != NULL) IAppHostProperty_Release(property);
    SysFreeString(bstr_name);

    return SUCCEEDED(hresult);
}

static BOOL get_from_collection_idx(IAppHostElementCollection* collection,
        BSTR property_key, BSTR property_value, short* index) {
    IAppHostProperty* property = NULL;
    IAppHostElement* element = NULL;
    USHORT i;
    HRESULT hresult = S_OK;
    *index = -1;
    do {
        DWORD count;
        hresult = IAppHostElementCollection_get_Count(collection, &count);
        if (FAILED(hresult)) {
            fprintf(stderr, "Unable to get the count of collection.\n");
            break;
        }

        for (i = 0; i < count && SUCCEEDED(hresult); ++i) {
            VARIANT var_value;
            VARIANT idx;
            idx.vt = VT_I2;
            idx.iVal = i;
            hresult = IAppHostElementCollection_get_Item(collection, idx, &element);
            if (FAILED(hresult)) {
                fprintf(stderr, "Unable to get item (%d).\n", i);
                break;
            }

            if (!get_property(element, property_key, &var_value)) {
                fprintf(stderr, "Failed to get property value.\n");
                hresult = S_FALSE;
                break;
            }

            if (wcscmp(property_value, var_value.bstrVal) == 0) {
                *index = i;
                break;
            }

            IAppHostElement_Release(element);
            element = NULL;
        }
    } while (FALSE);

    if (property != NULL) IAppHostProperty_Release(property);
    if (element != NULL) IAppHostElement_Release(element);

    return SUCCEEDED(hresult);
}

static BOOL get_from_collection(IAppHostElementCollection* collection,
        BSTR property_key, BSTR property_value,
        IAppHostElement** element) {
    short idx;
    HRESULT hresult;
    if (!get_from_collection_idx(collection, property_key, property_value, &idx)) {
        fprintf(stderr, "Failed to get child from collection.\n");
    }

    if (idx != -1) {
        VARIANT idx_var;
        idx_var.vt = VT_I2;
        idx_var.iVal = idx;
        hresult = IAppHostElementCollection_get_Item(collection, idx_var, element);
        if (FAILED(hresult)) {
            fprintf(stderr, "Failed to get element from collection.\n");
            return FALSE;
        } else {
            return TRUE;
        }
    } else {
        *element = NULL;
        return TRUE;
    }
}

static BOOL update_config_sections(IAppHostWritableAdminManager* manager, BOOL remove) {
    IAppHostConfigManager *cmgr = NULL;
    IAppHostConfigFile *cfile = NULL;
    IAppHostSectionGroup *root = NULL;
    IAppHostSectionGroup *swsg = NULL;
    IAppHostSectionDefinitionCollection *swsgcol = NULL;
    IAppHostSectionDefinition *modsec = NULL;
    HRESULT hresult = S_OK;
    BOOL result = FALSE;
    VARIANT sys_ws_gn;
    VARIANT msn;

    sys_ws_gn.vt = VT_BSTR;
    sys_ws_gn.bstrVal = system_webserver;
    msn.vt = VT_BSTR;
    msn.bstrVal = module_name;

    do {
        hresult = IAppHostWritableAdminManager_get_ConfigManager(manager, &cmgr);
        if (FAILED(hresult) || &cmgr == NULL) {
            fprintf(stderr, "Unable to get config manager.\n");
            break;
        }

        hresult = IAppHostConfigManager_GetConfigFile(cmgr, AM_IIS_APPHOST, &cfile);
        if (FAILED(hresult) || &cfile == NULL) {
            fprintf(stderr, "Unable to get config file.\n");
            break;
        }

        hresult = IAppHostConfigFile_get_RootSectionGroup(cfile, &root);
        if (FAILED(hresult) || &root == NULL) {
            fprintf(stderr, "Unable to get root section group.\n");
            break;
        }

        hresult = IAppHostSectionGroup_get_Item(root, sys_ws_gn, &swsg);
        if (FAILED(hresult) || &swsg == NULL) {
            fprintf(stderr, "Unable to get system.webServer section group.\n");
            break;
        }

        hresult = IAppHostSectionGroup_get_Sections(swsg, &swsgcol);
        if (FAILED(hresult) || &swsgcol == NULL) {
            fprintf(stderr, "Unable to get system.webServer section group collection.\n");
            break;
        }

        hresult = IAppHostSectionDefinitionCollection_get_Item(swsgcol, msn, &modsec);
        if (FAILED(hresult) || modsec == NULL) {
            hresult = IAppHostSectionDefinitionCollection_AddSection(swsgcol, module_name, &modsec);
            if (FAILED(hresult) || modsec == NULL) {
                fprintf(stderr, "Unable to add module section entry.\n");
            } else {
                result = TRUE;
            }
        } else {
            result = TRUE;
            if (remove == TRUE) {
                hresult = IAppHostSectionDefinitionCollection_DeleteSection(swsgcol, msn);
                if (FAILED(hresult)) {
                    result = FALSE;
                }
            }
        }

    } while (FALSE);

    if (modsec != NULL) IAppHostSectionDefinitionCollection_Release(modsec);
    if (swsgcol != NULL) IAppHostSectionGroup_Release(swsgcol);
    if (swsg != NULL) IAppHostSectionGroup_Release(swsg);
    if (root != NULL) IAppHostConfigFile_Release(root);
    if (cfile != NULL) IAppHostConfigManager_Release(cfile);
    if (cmgr != NULL) IAppHostWritableAdminManager_Release(cmgr);

    return result;
}

static BOOL add_to_global_modules(IAppHostWritableAdminManager* manager, BSTR image) {
    IAppHostElement* parent = NULL;
    IAppHostElementCollection* collection = NULL;
    IAppHostElement* element = NULL;
    HRESULT hresult = S_OK;
    BOOL result = FALSE;
    do {
        hresult = IAppHostWritableAdminManager_GetAdminSection(manager, AM_IIS_GLOBAL, AM_IIS_APPHOST, &parent);
        if (FAILED(hresult) || &parent == NULL) {
            fprintf(stderr, "Unable to get globalModules configuration.\n");
            break;
        }

        hresult = IAppHostElement_get_Collection(parent, &collection);
        if (FAILED(hresult) || &collection == NULL) {
            fprintf(stderr, "Unable to get globalModules child collection.\n");
            break;
        }

        /* create a global modules child element, like:
         * <add name="ModuleName", image="module.dll" />
         **/

        if (!get_from_collection(collection, AM_IIS_ENAME, module_name, &element)) {
            fprintf(stderr, "Failed to try detect old modules.\n");
            break;
        }

        if (element == NULL) {
            hresult = IAppHostElementCollection_CreateNewElement(collection, AM_IIS_EADD, &element);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to create globalModules/add element.\n");
                break;
            }

            if (!set_property(element, AM_IIS_ENAME, module_name)) {
                fprintf(stderr, "Failed to set name property.\n");
                break;
            }

            if (get_app_mode() == MODE_X64) {
                if (!set_property(element, L"preCondition", L"bitness64")) {
                    fprintf(stderr, "Failed to set name property.\n");
                    break;
                }
            } else {
                if (!set_property(element, L"preCondition", L"bitness32")) {
                    fprintf(stderr, "Failed to set name property.\n");
                    break;
                }
            }

            hresult = IAppHostElementCollection_AddElement(collection, element, -1);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to add globalModule/add element.\n");
                break;
            }
        }

        if (!set_property(element, L"image", image)) {
            fprintf(stderr, "Failed to set image property.\n");
            break;
        }

        result = TRUE;
    } while (FALSE);

    if (element != NULL) IAppHostElement_Release(element);
    if (collection != NULL) IAppHostElementCollection_Release(collection);
    if (parent != NULL) IAppHostElement_Release(parent);
    return result;
}

static BOOL add_to_modules(IAppHostWritableAdminManager* manager, BSTR config_path) {
    IAppHostElement* parent = NULL;
    IAppHostElementCollection* collection = NULL;
    IAppHostElement* element = NULL;

    BSTR bstr_config_path = SysAllocString(config_path);

    HRESULT hresult = S_OK;
    BOOL result = FALSE;
    do {
        hresult = IAppHostWritableAdminManager_GetAdminSection(manager, AM_IIS_MODULES,
                bstr_config_path, &parent);
        if (FAILED(hresult) || &parent == NULL) {
            fprintf(stderr, "Unable to get modules configuration.\n");
            break;
        }

        hresult = IAppHostElement_get_Collection(parent, &collection);
        if (FAILED(hresult) || &collection == NULL) {
            fprintf(stderr, "Unable to get modules child collection.\n");
            break;
        }

        if (!get_from_collection(collection, AM_IIS_ENAME, module_name, &element)) {
            fprintf(stderr, "Failed to try detect old modules.\n");
            break;
        }

        if (element == NULL) {
            hresult = IAppHostElementCollection_CreateNewElement(collection, AM_IIS_EADD, &element);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to create modules/add element.\n");
                break;
            }

            if (!set_property(element, AM_IIS_ENAME, module_name)) {
                fprintf(stderr, "Failed to set name property.\n");
                break;
            }

            if (get_app_mode() == MODE_X64) {
                if (!set_property(element, L"preCondition", L"bitness64")) {
                    fprintf(stderr, "Failed to set name property.\n");
                    break;
                }
            } else {
                if (!set_property(element, L"preCondition", L"bitness32")) {
                    fprintf(stderr, "Failed to set name property.\n");
                    break;
                }
            }

            hresult = IAppHostElementCollection_AddElement(collection, element, -1);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to add modules/add element.\n");
                break;
            }
        }

        result = TRUE;
    } while (FALSE);

    if (element != NULL) IAppHostElement_Release(element);
    if (collection != NULL) IAppHostElementCollection_Release(collection);
    if (parent != NULL) IAppHostElement_Release(parent);

    SysFreeString(bstr_config_path);
    return result;
}

static BOOL update_module_site_config(IAppHostWritableAdminManager* manager, BSTR config_path, BSTR mod_config_path, BOOL enabled) {
    IAppHostElement *element = NULL;
    IAppHostPropertyCollection *properties = NULL;
    IAppHostProperty *enabled_prop = NULL;
    IAppHostProperty *cfile_prop = NULL;
    HRESULT hresult = S_OK;
    BOOL result = FALSE;

    BSTR bstr_value = SysAllocString(mod_config_path);

    VARIANT a, av, b, bv;
    av.vt = VT_BOOL;
    av.boolVal = enabled ? VARIANT_TRUE : VARIANT_FALSE;
    a.vt = VT_BSTR;
    a.bstrVal = L"enabled";
    b.vt = VT_BSTR;
    b.bstrVal = L"configFile";
    bv.vt = VT_BSTR;
    bv.bstrVal = bstr_value;

    do {
        hresult = IAppHostWritableAdminManager_GetAdminSection(manager,
                AM_IIS_MODULE_CONF, config_path, &element);
        if (FAILED(hresult) || &element == NULL) {
            fprintf(stderr, "Unable to get module configuration element.\n");
            break;
        }

        hresult = IAppHostElement_get_Properties(element, &properties);
        if (FAILED(hresult) || &properties == NULL) {
            fprintf(stderr, "Unable to get module element properties.\n");
            break;
        }

        hresult = IAppHostPropertyCollection_get_Item(properties, a, &enabled_prop);
        if (FAILED(hresult) || &enabled_prop == NULL) {
            fprintf(stderr, "Unable to get module element properties item (enabled).\n");
            break;
        }
        hresult = IAppHostProperty_put_Value(enabled_prop, av);
        if (FAILED(hresult)) {
            fprintf(stderr, "Unable to set module element properties item (enabled).\n");
            break;
        }

        hresult = IAppHostPropertyCollection_get_Item(properties, b, &cfile_prop);
        if (FAILED(hresult) || &cfile_prop == NULL) {
            fprintf(stderr, "Unable to get module element properties item (configFile).\n");
            break;
        }
        hresult = IAppHostProperty_put_Value(cfile_prop, bv);
        if (FAILED(hresult)) {
            fprintf(stderr, "Unable to set module element properties item (configFile).\n");
            break;
        }
        result = TRUE;
    } while (FALSE);

    if (cfile_prop != NULL) IAppHostPropertyCollection_Release(cfile_prop);
    if (enabled_prop != NULL) IAppHostPropertyCollection_Release(enabled_prop);
    if (properties != NULL) IAppHostElement_Release(properties);
    if (element != NULL) IAppHostElement_Release(element);
    SysFreeString(bstr_value);
    return result;
}

static BOOL remove_from_modules(IAppHostWritableAdminManager* manager, BSTR config_path, BSTR section, BOOL test_only) {
    IAppHostElement* parent = NULL;
    IAppHostElementCollection* collection = NULL;
    IAppHostElement* element = NULL;
    IAppHostProperty* name_property = NULL;

    BSTR bstr_section_name = SysAllocString(section);
    BSTR bstr_config_path = SysAllocString(config_path);

    HRESULT hresult = S_OK;
    BOOL result = FALSE;
    short sitemap_index;
    DWORD count;
    do {
        hresult = IAppHostWritableAdminManager_GetAdminSection(manager, bstr_section_name,
                bstr_config_path, &parent);
        if (FAILED(hresult) || &parent == NULL) {
            fprintf(stderr, "Unable to get section to remove module.\n");
            break;
        }

        hresult = IAppHostElement_get_Collection(parent, &collection);
        if (FAILED(hresult) || &collection == NULL) {
            fprintf(stderr, "Unable to get collection to remove module.\n");
            break;
        }

        hresult = IAppHostElementCollection_get_Count(collection, &count);
        if (FAILED(hresult)) {
            fprintf(stderr, "Unable to get collection element count.\n");
            break;
        }

        if (!get_from_collection_idx(collection, AM_IIS_ENAME, module_name, &sitemap_index)) {
            fprintf(stderr, "Failed to find OpenAM module.\n");
            break;
        }

        if (test_only) {
            result = sitemap_index != -1 ? TRUE : FALSE;
        } else {
            if (sitemap_index != -1) {
                VARIANT var_index;
                var_index.vt = VT_I2;
                var_index.iVal = sitemap_index;
                hresult = IAppHostElementCollection_DeleteElement(collection, var_index);
                if (FAILED(hresult)) {
                    fprintf(stderr, "Failed to remove OpenAM module.\n");
                }
            } else {
                fprintf(stderr, "No OpenAM module is found.\n");
            }
            result = SUCCEEDED(hresult);
        }
    } while (FALSE);

    if (name_property != NULL) IAppHostProperty_Release(name_property);
    if (element != NULL) IAppHostElement_Release(element);
    if (collection != NULL) IAppHostElementCollection_Release(collection);
    if (parent != NULL) IAppHostElement_Release(parent);
    SysFreeString(bstr_section_name);
    SysFreeString(bstr_config_path);
    return result;
}

int install_module(const char *modpath, const char *schema) {
    IAppHostWritableAdminManager *admin_manager = NULL;
    int rv = 0;
    BSTR module_wpath;
    HRESULT hresult = S_OK;
    wchar_t *location = NULL;
    char schema_sys_file[MAX_PATH];

    memset(&schema_sys_file[0], 0, sizeof (schema_sys_file));
    GetEnvironmentVariableA("SYSTEMROOT", schema_sys_file, sizeof (schema_sys_file));
    strcat(schema_sys_file, IIS_SCHEMA_CONF_FILE);

    location = utf8_decode(modpath, NULL);
    if (location == NULL) return rv;
    module_wpath = SysAllocString(location);

    do {
        hresult = CoInitializeEx(NULL, COINIT_MULTITHREADED);
        if (FAILED(hresult)) {
            fprintf(stderr, "Failed to initialize COM.\n");
            break;
        }
        hresult = CoCreateInstance(&CLSID_AppHostWritableAdminManager, NULL,
                CLSCTX_INPROC_SERVER, &IID_IAppHostWritableAdminManager, (LPVOID*) & admin_manager);
        if (FAILED(hresult)) {
            fprintf(stderr, "Failed to create AppHostWritableAdminManager.\n");
            break;
        }
        hresult = IAppHostWritableAdminManager_put_CommitPath(admin_manager, AM_IIS_APPHOST);
        if (FAILED(hresult)) {
            fprintf(stderr, "Failed to put commit path.\n");
            break;
        }

        if (!add_to_global_modules(admin_manager, module_wpath)) {
            fprintf(stderr, "Failed to add entry to globalModules.\n");
            break;
        } else {
            if (CopyFileA(schema, schema_sys_file, FALSE) != 0) {
                if (update_config_sections(admin_manager, FALSE)) {
                    rv = 1;
                } else {
                    fprintf(stderr, "Failed to update configuration schema.\n");
                }
            } else {
                fprintf(stderr, "Failed to copy module schema file (%d).\n", GetLastError());
            }
        }

        hresult = IAppHostWritableAdminManager_CommitChanges(admin_manager);
        if (FAILED(hresult)) {
            break;
        }
    } while (FALSE);

    free(location);
    if (admin_manager != NULL) IAppHostWritableAdminManager_Release(admin_manager);
    SysFreeString(module_wpath);
    CoUninitialize();

    return rv;
}

int remove_module() {
    IAppHostWritableAdminManager *admin_manager = NULL;
    int rv = 0;
    HRESULT hresult = S_OK;
    do {
        hresult = CoInitializeEx(NULL, COINIT_MULTITHREADED);
        if (FAILED(hresult)) {
            fprintf(stderr, "Failed to initialize COM.\n");
            break;
        }

        hresult = CoCreateInstance(&CLSID_AppHostWritableAdminManager, NULL,
                CLSCTX_INPROC_SERVER, &IID_IAppHostWritableAdminManager, (LPVOID*) & admin_manager);
        if (FAILED(hresult)) {
            fprintf(stderr, "Failed to create AppHostWritableAdminManager.\n");
            break;
        }

        hresult = IAppHostWritableAdminManager_put_CommitPath(admin_manager, AM_IIS_APPHOST);
        if (FAILED(hresult)) {
            fprintf(stderr, "Failed to put commit path.\n");
            break;
        }

        if (!remove_from_modules(admin_manager, AM_IIS_APPHOST, AM_IIS_GLOBAL, FALSE)) {
            fprintf(stderr, "Failed to remove entry from globalModules.\n");
            break;
        } else {
            rv = 1;
        }

        hresult = IAppHostWritableAdminManager_CommitChanges(admin_manager);
        if (FAILED(hresult)) {
            fprintf(stderr, "Failed to save changes to remove module.\n");
            break;
        }

    } while (FALSE);

    if (admin_manager != NULL) IAppHostWritableAdminManager_Release(admin_manager);
    CoUninitialize();

    return rv;
}

static char *get_site_name(const char *sid) {
    IAppHostWritableAdminManager *admin_manager = NULL;
    IAppHostElement *he = NULL, *ce = NULL;
    IAppHostElementCollection *hec = NULL;
    DWORD num;
    UINT i;
    BSTR bstr_config_path = SysAllocString(AM_IIS_APPHOST);
    BSTR bstr_sites = SysAllocString(AM_IIS_SITES);
    BSTR bstr_name = SysAllocString(AM_IIS_ENAME);
    BSTR bstr_id = SysAllocString(AM_IIS_EID);
    VARIANT value;
    HRESULT hresult = S_OK;
    char *name, *id, *ret = NULL;

    do {
        hresult = CoInitializeEx(NULL, COINIT_MULTITHREADED);
        if (FAILED(hresult)) {
            break;
        }
        hresult = CoCreateInstance(&CLSID_AppHostWritableAdminManager, NULL,
                CLSCTX_INPROC_SERVER, &IID_IAppHostWritableAdminManager, (LPVOID*) & admin_manager);
        if (FAILED(hresult)) {
            break;
        }
        hresult = IAppHostWritableAdminManager_GetAdminSection(admin_manager, bstr_sites, bstr_config_path, &he);
        if (FAILED(hresult) || he == NULL) {
            break;
        }
        hresult = IAppHostElement_get_Collection(he, &hec);
        if (FAILED(hresult)) {
            break;
        }
        hresult = IAppHostElementCollection_get_Count(hec, &num);
        if (FAILED(hresult)) {
            break;
        }

        for (i = 0; i < num; i++) {
            VARIANT index;
            index.vt = VT_UINT;
            index.uintVal = i;
            hresult = IAppHostElementCollection_get_Item(hec, index, &ce);
            if (SUCCEEDED(hresult)) {
                name = get_property_value_byname(ce, &value, &bstr_name, VT_BSTR);
                id = get_property_value_byname(ce, &value, &bstr_id, VT_UI4);
                if (strcmp(id, sid) == 0) {
                    if (name != NULL && id != NULL) {
                        char cpath[2048];
                        char *p;
                        for (p = name; *p != '\0'; ++p) {
                            *p = toupper(*p);
                        }
                        sprintf_s(cpath, sizeof (cpath), "MACHINE/WEBROOT/APPHOST/%s", name);
                        ret = strdup(cpath);
                        i = num;
                    }
                }
                if (name != NULL) free(name);
                if (id != NULL) free(id);
                name = NULL;
                id = NULL;
            }
            if (ce != NULL) IAppHostElement_Release(ce);
        }

    } while (FALSE);

    if (he != NULL) IAppHostElement_Release(he);
    if (hec != NULL) IAppHostElementCollection_Release(hec);
    if (admin_manager != NULL) IAppHostWritableAdminManager_Release(admin_manager);
    SysFreeString(bstr_config_path);
    SysFreeString(bstr_sites);
    SysFreeString(bstr_name);
    SysFreeString(bstr_id);
    CoUninitialize();
    return ret;
}

int enable_module(const char *siteid, const char *modconf) {
    IAppHostWritableAdminManager *admin_manager = NULL;
    HRESULT hresult = S_OK;
    int rv = 0;
    wchar_t *config_path_w = NULL;
    wchar_t *modconf_w = NULL;
    if (siteid != NULL) {
        char *config_path = get_site_name(siteid);
        if (config_path != NULL) {
            config_path_w = utf8_decode(config_path, NULL);
            free(config_path);
        }

        modconf_w = utf8_decode(modconf, NULL);

        do {
            if (config_path_w == NULL || modconf_w == NULL) {
                fprintf(stderr, "Failed to allocate memory.\n");
                break;
            }

            hresult = CoInitializeEx(NULL, COINIT_MULTITHREADED);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to initialize COM.\n");
                break;
            }
            hresult = CoCreateInstance(&CLSID_AppHostWritableAdminManager, NULL,
                    CLSCTX_INPROC_SERVER, &IID_IAppHostWritableAdminManager, (LPVOID*) & admin_manager);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to create AppHostWritableAdminManager.\n");
                break;
            }
            hresult = IAppHostWritableAdminManager_put_CommitPath(admin_manager, config_path_w);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to put commit path.\n");
                break;
            }

            if (!add_to_modules(admin_manager, config_path_w)) {
                fprintf(stderr, "Failed to add entry to modules.\n");
                break;
            } else {
                if (!update_module_site_config(admin_manager, config_path_w, modconf_w, TRUE)) {
                    fprintf(stderr, "Failed to add module configuration entry.\n");
                } else {
                    rv = 1;
                }
            }

            hresult = IAppHostWritableAdminManager_CommitChanges(admin_manager);
            if (FAILED(hresult)) {
                break;
            }
        } while (FALSE);

        if (modconf_w != NULL) free(modconf_w);
        if (config_path_w != NULL) free(config_path_w);

        if (admin_manager != NULL) IAppHostWritableAdminManager_Release(admin_manager);
        CoUninitialize();
    }
    return rv;
}

int disable_module(const char *siteid, const char *modconf) {
    IAppHostWritableAdminManager *admin_manager = NULL;
    HRESULT hresult = S_OK;
    int rv = 0;
    wchar_t *config_path_w = NULL;
    wchar_t *modconf_w = NULL;
    if (siteid != NULL) {
        char *config_path = get_site_name(siteid);
        if (config_path != NULL) {
            config_path_w = utf8_decode(config_path, NULL);
            free(config_path);
        }

        modconf_w = utf8_decode(modconf, NULL);

        do {
            if (config_path_w == NULL) {
                fprintf(stderr, "Failed to allocate memory.\n");
                break;
            }

            hresult = CoInitializeEx(NULL, COINIT_MULTITHREADED);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to initialize COM.\n");
                break;
            }
            hresult = CoCreateInstance(&CLSID_AppHostWritableAdminManager, NULL,
                    CLSCTX_INPROC_SERVER, &IID_IAppHostWritableAdminManager, (LPVOID*) & admin_manager);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to create AppHostWritableAdminManager.\n");
                break;
            }
            hresult = IAppHostWritableAdminManager_put_CommitPath(admin_manager, config_path_w);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to put commit path.\n");
                break;
            }

            if (!remove_from_modules(admin_manager, config_path_w, AM_IIS_MODULES, FALSE)) {
                fprintf(stderr, "Failed to remove entry from modules.\n");
                break;
            } else {
                if (!update_module_site_config(admin_manager, config_path_w, modconf_w, FALSE)) {
                    fprintf(stderr, "Failed to add module configuration entry.\n");
                } else {
                    rv = 1;
                }
            }

            hresult = IAppHostWritableAdminManager_CommitChanges(admin_manager);
            if (FAILED(hresult)) {
                break;
            }
        } while (FALSE);

        if (modconf_w != NULL) free(modconf_w);
        if (config_path_w != NULL) free(config_path_w);

        if (admin_manager != NULL) IAppHostWritableAdminManager_Release(admin_manager);
        CoUninitialize();
    }
    return rv;
}

int test_module(const char *siteid) {
    BOOL local = FALSE;
    BOOL global = FALSE;
    int rv = -1;
    IAppHostWritableAdminManager *admin_manager = NULL;
    HRESULT hresult = S_OK;
    wchar_t *config_path_w = NULL;
    if (siteid != NULL) {
        char *config_path = get_site_name(siteid);
        if (config_path != NULL) {
            config_path_w = utf8_decode(config_path, NULL);
            free(config_path);
        }

        do {
            if (config_path_w == NULL) {
                fprintf(stderr, "Failed to allocate memory.\n");
                break;
            }

            hresult = CoInitializeEx(NULL, COINIT_MULTITHREADED);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to initialize COM.\n");
                break;
            }
            hresult = CoCreateInstance(&CLSID_AppHostWritableAdminManager, NULL,
                    CLSCTX_INPROC_SERVER, &IID_IAppHostWritableAdminManager, (LPVOID*) & admin_manager);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to create AppHostWritableAdminManager.\n");
                break;
            }
            hresult = IAppHostWritableAdminManager_put_CommitPath(admin_manager, config_path_w);
            if (FAILED(hresult)) {
                fprintf(stderr, "Failed to put commit path.\n");
                break;
            }

            global = remove_from_modules(admin_manager, config_path_w, AM_IIS_GLOBAL, TRUE);
            local = remove_from_modules(admin_manager, config_path_w, AM_IIS_MODULES, TRUE);

            if (global == FALSE && local == FALSE) {
                rv = 0;
            } else {
                if (global == TRUE) {
                    rv = 1 << 0;
                }
                if (local == TRUE) {
                    rv = 1 << 1;
                }
            }

            hresult = IAppHostWritableAdminManager_CommitChanges(admin_manager);
            if (FAILED(hresult)) {
                break;
            }
        } while (FALSE);

        if (config_path_w != NULL) free(config_path_w);
        if (admin_manager != NULL) IAppHostWritableAdminManager_Release(admin_manager);
        CoUninitialize();
    }
    return rv;
}

#else 

/*no-ops on this platform*/

void list_iis_sites(int argc, char **argv) {

}

int enable_module(const char *siteid, const char *modconf) {
    return 0;
}

int disable_module(const char *siteid, const char *modconf) {
    return 0;
}

int test_module(const char *siteid) {
    return 0;
}

int install_module(const char *modpath, const char *modconf) {
    return 0;
}

int remove_module() {
    return 0;
}

#endif
