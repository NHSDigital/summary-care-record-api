package uk.nhs.adaptors.scr.clients.spine;

import org.w3c.dom.Document;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.models.ProcessingResult;

public interface SpineClientContract {
    Response<Document> sendAcsData(String requestBody, String nhsdAsid);
    Response<String> sendScrData(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid);
    ProcessingResult getScrProcessingResult(String contentLocation, long initialWaitTime, String nhsdAsid,
                                            String nhsdIdentity, String nhsdSessionUrid);
    Response<Document> sendGetScrId(String requestBody, String nhsdAsid);
    Response<Document> sendGetScr(String requestBody, String nhsdAsid);
    Response<String> sendAlert(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid);
}
