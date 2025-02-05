package com.cts.autogen.importGriLossSeries.api;

import com.cts.autogen.logger.BasicConfApp;
import com.cts.autogen.utils.ReadJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PostTimeseriesForMeteringPoint {
    private static final Logger logger = LoggerFactory.getLogger(BasicConfApp.class);
    static HttpClient client = HttpClient.newBuilder().build();

    public PostTimeseriesForMeteringPoint(List<String> payloadToPost) throws Exception {
        for (String onePayload : payloadToPost) {
            String meteringPointId = new ReadJsonUtil(onePayload).getMeteringPointId();
            sendPostRequest(onePayload, meteringPointId);
        }
    }

    private void sendPostRequest(String payload, String meteringPointId) throws Exception {
        String contentType = "application/vnd.techyon.measurements-v1+json";
        var request = HttpRequest.newBuilder()
                .uri(new URI("https://queengorg17.staging.techyon.io/api/timeseries"))
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .header("Accept", MediaType.APPLICATION_JSON.toString())
                .header("Content-Type", contentType)
                .header("Authorization", "ApiKey DFXT9rz6OA")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("Posting meter data for meteringPoint {}.",meteringPointId);
        logger.info("API response:" + "{}", response);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(response.statusCode() + ":" + response.body());
        }
    }
}
