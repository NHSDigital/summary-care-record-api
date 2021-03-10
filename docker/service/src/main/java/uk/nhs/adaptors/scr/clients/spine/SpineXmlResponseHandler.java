package uk.nhs.adaptors.scr.clients.spine;

import lombok.SneakyThrows;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.logging.LogExecutionTime;

import static uk.nhs.adaptors.scr.utils.XmlUtils.documentBuilder;

@Component
public class SpineXmlResponseHandler implements ResponseHandler<Response<Document>> {
    @SneakyThrows
    @Override
    @LogExecutionTime
    public Response<Document> handleResponse(HttpResponse response) {
        var statusCode = response.getStatusLine().getStatusCode();
        var headers = response.getAllHeaders();
        return new Response<>(statusCode, headers, documentBuilder().parse(response.getEntity().getContent()));
    }
}
