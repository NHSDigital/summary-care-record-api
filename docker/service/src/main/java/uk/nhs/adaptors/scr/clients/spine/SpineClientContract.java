package uk.nhs.adaptors.scr.clients.spine;

import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.models.ProcessingResult;

public interface SpineClientContract {
    Response sendAcsData(String requestBody, String nhsdAsid);
    Response sendScrData(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid);
    ProcessingResult getScrProcessingResult(String contentLocation, long initialWaitTime, String nhsdAsid,
                                            String nhsdIdentity, String nhsdSessionUrid);
    Response sendGetScrId(String requestBody, String nhsdAsid);
    Response sendGetScr(String requestBody, String nhsdAsid);
    Response sendAlert(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid);
}
