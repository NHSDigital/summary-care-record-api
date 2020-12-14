package uk.nhs.adaptors.scr.clients;

import uk.nhs.adaptors.scr.clients.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.models.ProcessingResult;

public interface SpineClientContract {
    Response sendAcsData(String requestBody);
    Response sendScrData(String requestBody);
    ProcessingResult getScrProcessingResult(String contentLocation, long initialWaitTime, String nhsdAsid);
    Response sendGetScrId(String requestBody, String nhsdAsid);
}
