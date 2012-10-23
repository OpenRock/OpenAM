

package org.forgerock.openam.oauth2.rest;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.idm.*;
import com.sun.identity.security.AdminTokenAction;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.*;

import org.forgerock.json.resource.NotSupportedException;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.CollectionResourceProvider;
import org.forgerock.openam.oauth2.OAuth2;
import org.forgerock.openam.oauth2.exceptions.OAuthProblemException;
import org.restlet.data.Status;

import java.security.AccessController;
import java.util.*;

public class ClientResource  implements CollectionResourceProvider {

    SSOToken token = (SSOToken) AccessController.doPrivileged(AdminTokenAction.getInstance());

    public ClientResource() {
    }

    @Override
    public void actionCollection(ServerContext context, ActionRequest actionRequest, ResultHandler<JsonValue> handler){
        final ResourceException e =
                new NotSupportedException("Actions are not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void actionInstance(ServerContext context, String resourceId, ActionRequest request,
                               ResultHandler<JsonValue> handler){
        final ResourceException e =
                new NotSupportedException("Actions are not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void createInstance(ServerContext context, CreateRequest createRequest, ResultHandler<Resource> handler){

        Map<String, ArrayList<String>> client = (Map<String, ArrayList<String>>) createRequest.getContent().getObject();
        String realm = client.remove("realm").iterator().next();
        String id = client.remove("id").iterator().next();

        Map<String, Set<String>> attrs = new HashMap<String, Set<String>>();
        for (Map.Entry mapEntry : client.entrySet()){
            attrs.put((String)mapEntry.getKey(), new HashSet<String>((ArrayList)mapEntry.getValue()));
        }

        JsonValue response = null;
        Map< String, String> responseVal =new HashMap< String, String>();
        try {
            AMIdentityRepository repo = new AMIdentityRepository(token , realm);
            repo.createIdentity(IdType.AGENTONLY, id, attrs);
            responseVal.put("success", "true");
        } catch(Exception e){
            responseVal.put("success", "false");
            handler.handleError(new InternalServerErrorException("Unable to create client"));
        }
        response = new JsonValue(responseVal);

        Resource resource = new Resource("results", "1", response);
        handler.handleResult(resource);
    }

    @Override
    public void deleteInstance(ServerContext context, String resourceId, DeleteRequest request,
                               ResultHandler<Resource> handler){
        Map<String, String> responseVal = new HashMap<String, String>();
        JsonValue response = null;
        try {
            AMIdentityRepository repo = new AMIdentityRepository(token , null);
            Set<AMIdentity> ids = new HashSet<AMIdentity>();
            ids.add(getIdentity(resourceId, null));
            repo.deleteIdentities(ids);
            responseVal.put("success", "true");
        } catch(Exception e){
            responseVal.put("success", "false");
            handler.handleError(new InternalServerErrorException("Unable to create client"));
        }
        response = new JsonValue(responseVal);

        Resource resource = new Resource("results", "1", response);
        handler.handleResult(resource);
    }

    @Override
    public void patchInstance(ServerContext context, String resourceId, PatchRequest request,
                              ResultHandler<Resource> handler){
        final ResourceException e =
                new NotSupportedException("Patch is not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void queryCollection(ServerContext context, QueryRequest queryRequest, QueryResultHandler handler){
        final ResourceException e =
                new NotSupportedException("Query is not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void readInstance(ServerContext context, String resourceId, ReadRequest request,
                             ResultHandler<Resource> handler){
        final ResourceException e =
                new NotSupportedException("Read is not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void updateInstance(ServerContext context, String resourceId, UpdateRequest request,
                               ResultHandler<Resource> handler){
        final ResourceException e =
                new NotSupportedException("Update is not supported for resource instances");
        handler.handleError(e);
    }

    private AMIdentity getIdentity(String uName, String realm) throws ResourceException {
        AMIdentity theID = null;
        AMIdentityRepository amIdRepo = null;
        try{
            amIdRepo = new AMIdentityRepository(token , realm);
        } catch (IdRepoException e){
            throw new InternalServerErrorException("Unable to get idrepo", e);
        } catch (SSOException e){
            throw new InternalServerErrorException("Unable to get idrepo", e);
        }
        IdSearchControl idsc = new IdSearchControl();
        idsc.setRecursive(true);
        idsc.setAllReturnAttributes(true);
        // search for the identity
        Set<AMIdentity> results = Collections.EMPTY_SET;
        try {
            idsc.setMaxResults(0);
            IdSearchResults searchResults =
                    amIdRepo.searchIdentities(IdType.AGENTONLY, uName, idsc);
            if (searchResults != null) {
                results = searchResults.getSearchResults();
            }

            if (results == null || results.size() != 1) {
                throw new InternalServerErrorException("Too many results or not enough");
            }

            theID = results.iterator().next();
        } catch (IdRepoException e) {
            throw new InternalServerErrorException("Unable to get search results", e);
        } catch (SSOException e) {
            throw new InternalServerErrorException("Unable to get search results", e);
        }
        return theID;
    }
}
