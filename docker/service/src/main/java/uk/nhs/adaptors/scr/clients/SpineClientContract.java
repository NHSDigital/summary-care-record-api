package uk.nhs.adaptors.scr.clients;

import uk.nhs.adaptors.scr.clients.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.models.ProcessingResult;

public interface SpineClientContract {
    Response sendAcsData(String requestBody, String nhsdAsid);
    Response sendScrData(String requestBody, String nhsdAsid, String nhsdIdentity);
    ProcessingResult getScrProcessingResult(String contentLocation, long initialWaitTime, String nhsdAsid, String nhsdIdentity);
    Response sendGetScrId(String requestBody, String nhsdAsid);
}
