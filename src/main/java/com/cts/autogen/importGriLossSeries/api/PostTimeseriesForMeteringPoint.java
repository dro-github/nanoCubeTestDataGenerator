package com.cts.autogen.importGriLossSeries.api;

import com.cts.autogen.logger.BasicConfApp;
import com.cts.autogen.utils.ReadJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PostTimeseriesForMeteringPoint {
    private static final Logger logger = LoggerFactory.getLogger(BasicConfApp.class);
    static HttpClient client = HttpClient.newBuilder().build();
    static RestTemplate restTemplate = new RestTemplate();

    public PostTimeseriesForMeteringPoint(String habitatUrl,String apiKey,List<String> payloadToPost) throws Exception {
        for (String onePayload : payloadToPost) {
            String meteringPointId = new ReadJsonUtil(onePayload).getMeteringPointId();
            sendPostRequest(habitatUrl,apiKey,onePayload, meteringPointId);
        }
    }

    /**
    private void sendPostRequest(String habitat,String apiKey, String payload, String meteringPointId) throws Exception {
        String contentType = "application/vnd.techyon.measurements-v1+json";
        var request = HttpRequest.newBuilder()
                .uri(new URI("https://" + habitat + "api/timeseries"))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .header("Accept", MediaType.APPLICATION_JSON.toString())
                .header("Content-Type", contentType)
                .header("Authorization", apiKey)
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("Posting meter data for meteringPoint {}.",meteringPointId);
        logger.info("API response:" + "{}", response);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(response.statusCode() + ":" + response.body());
        }
    }
     */

    private void sendPostRequest(String habitat,String apiKey,String payload,String meteringPointId ) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.techyon.measurements-v1+json"));
        headers.set("Authorization", apiKey);
        var entity = new HttpEntity<>(payload, headers);
        var response = restTemplate.exchange("https://" + habitat + "api/timeseries", HttpMethod.POST, entity, String.class);
        logger.info("Posting meter data for meteringPoint {}.",meteringPointId);
        logger.info("API response:" + "{}", response);
        logger.info("RESPONSE = {}",response.getStatusCode());
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.error(response.getBody());
            throw new RuntimeException(response.toString());
        }
    }
}
