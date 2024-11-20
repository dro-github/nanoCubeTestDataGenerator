package com.cts.autogen.techyon.api;

import com.cts.autogen.dbGateWay.GetSettlementMethodFromDB;
import com.cts.autogen.logger.BasicConfApp;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class GetSettlementMethodForMeteringPoint {
    private static final Logger logger = LoggerFactory.getLogger(BasicConfApp.class);
    private final String settlementMethod;
    private final GetSettlementMethodFromDB getSettlementMethodFromDB;

    public GetSettlementMethodForMeteringPoint(String url,JdbcTemplate jdbcTemplate, String meteringPoint) {
        getSettlementMethodFromDB = new GetSettlementMethodFromDB(jdbcTemplate);
        settlementMethod = getSettlementMethod(url,meteringPoint);
    }

    private String getSettlementMethod(String url,String meteringPoint) {
        if (hasDBEntry(meteringPoint)) {
            return getSettlementMethodFromDB.getSettlementMethod();
        } else {
            RestTemplate restTemplate = new RestTemplate();
            String fullUrl = url + "api/meteringpoints/" + meteringPoint + "/rules/SettlementMethod";
            String apiKey = "ApiKey DFXT9rz6OA";
            var headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.parseMediaType("application/vnd.techyon.rules-v1+json"));
            var entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response;
            try {
                response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
            } catch (Exception e) {
                logger.error("metering point = {}: {}", meteringPoint, e.getMessage());
                return null;
            }
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(response.toString());
            }
            String settlementMethodString = null;
            try {
                settlementMethodString = JsonPath.read(response.getBody(), "$['value']");
                getSettlementMethodFromDB.updateH2DB(meteringPoint,settlementMethodString);
            } catch (PathNotFoundException e) {
                logger.info(e.getMessage());
            }
            return settlementMethodString;
        }
    }

    private boolean hasDBEntry(String meteringPoint) {
        return getSettlementMethodFromDB.hasDBEntry(meteringPoint);
    }

    public String getSettlementMethodString() {
        return settlementMethod;
    }
}
