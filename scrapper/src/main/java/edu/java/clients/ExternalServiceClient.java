package edu.java.clients;

import edu.java.clients.entities.UpdateResponse;
import edu.java.clients.exceptions.EmptyResponseBodyException;
import edu.java.clients.exceptions.FieldNotFoundException;

public interface ExternalServiceClient {
    boolean checkURLSupportedByService(String url);

    UpdateResponse fetchUpdate(String url) throws EmptyResponseBodyException, FieldNotFoundException;

    String getBodyJSONContent(String url);

    String getServiceNameInDatabase();

    String getChangeDescriptionFromResponseBodies(String jsonStringBodyBefore, String jsonStringBodyAfter);
}
