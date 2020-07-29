package uk.nhs.adaptors.scr.utils;

import java.util.HashMap;

import org.springframework.stereotype.Component;

import uk.nhs.adaptors.scr.models.requests.EndpointMockData;

@Component
public class MockedEndpointsStorage {
    HashMap<String, EndpointMockData> map = new HashMap<>();

    public void add(EndpointMockData data) {
        String keyName = getKeyName(data.getHttpMethod(), data.getUrl());
        map.put(keyName, data);
    }

    public EndpointMockData get(String httpMethod, String endpoint) {
        String keyName = getKeyName(httpMethod, endpoint);
        return map.getOrDefault(keyName, null);
    }

    public void reset(){
        map.clear();
    }

    private String getKeyName(String httpMethod, String endpoint) {
        return httpMethod + ": " + endpoint;
    }
}
