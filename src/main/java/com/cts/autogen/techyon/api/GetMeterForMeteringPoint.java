package com.cts.autogen.techyon.api;

import com.cts.autogen.dbGateWay.GetMeteringPointFromDB;
import com.cts.autogen.logger.BasicConfApp;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetMeterForMeteringPoint {
    private static final Logger logger = LoggerFactory.getLogger(BasicConfApp.class);
    private final Map<String,String> mpToDevice = new HashMap<>();
    private final GetMeteringPointFromDB getMeteringPointFromDB;
    private final String url;

    public GetMeterForMeteringPoint(JdbcTemplate jdbcTemplate,String url) {
        this.url = url;
        getMeteringPointFromDB = new GetMeteringPointFromDB(jdbcTemplate);
        getMeteringPointDeviceConnection();
    }

    private void getMeteringPointDeviceConnection() {

        logger.info("Initiating meter to metering point for transformer DRO_NC_KP1");
        getMeteringPointDeviceConnectionForMeter(0,75,"DRO_SUB_TS_KP1-0","DRO_SUB_TS_KP1-00");
        logger.info("Map meter to metering point initiated for {} metering points.",mpToDevice.size());

        logger.info("Initiating meter to metering point for transformer DRO_NC_KP2");
        getMeteringPointDeviceConnectionForMeter(0,75,"DRO_SUB_TS_KP2-0","DRO_SUB_TS_KP2-00");
        logger.info("Map meter to metering point initiated for {} metering points.",mpToDevice.size());

        logger.info("Initiating meter to metering point for transformer HYV_TRAFO_TEST");
        getMeteringPointDeviceConnectionForMeter(0,75,"DRO_SUB_TS_HYV-0","DRO_SUB_TS_HYV-00");
        logger.info("Map meter to metering point initiated for {} metering points.",mpToDevice.size());

        getMeteringPointDeviceConnectionForMeter(75,5,"DRO_NAER_KP1-0","N/A");
        logger.info("Map meter to metering point initiated for {} metering points.",mpToDevice.size());

        getMeteringPointDeviceConnectionForMeter(75,10,"DRO_NAER_KP2-0","N/A");
        logger.info("Map meter to metering point initiated for {} metering points.",mpToDevice.size());
    }

    private void getMeteringPointDeviceConnectionForMeter(int startIndexForMeterID,int numOfMeters, String prefix1, String prefix2) {
        logger.info("Initiating map meter to metering point.");
        String meterPreFix;
        for (int i = startIndexForMeterID; i < startIndexForMeterID + numOfMeters; i++) {
            meterPreFix = prefix1;
            if (i < 10) {
                meterPreFix = prefix2;
            }
            String meter = meterPreFix + (i);
            if (hasDBEntry(meter)){
                mpToDevice.put(getMeteringPont(),meter);
            }
            else {
                initiateMapEntry(meter);
            }
        }
    }

    private boolean hasDBEntry(String meter) {
        return getMeteringPointFromDB.hasDBEntry(meter);
    }

    private String getMeteringPont() {
        return getMeteringPointFromDB.getEan();
    }

    private void initiateMapEntry(String meter){
        RestTemplate restTemplate = new RestTemplate();
        String fullUrl = url + "api/devicemeteringpointconnections";
        String apiKey = "ApiKey DFXT9rz6OA";
        String payload = "device_id=" + meter;
        var headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.parseMediaType("application/vnd.techyon.devicemeteringpointconnection-v1+json"));
        var entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange("https://" + fullUrl + "?" + payload, HttpMethod.GET, entity, String.class);
        } catch (Exception e) {
            logger.error("meter = {}: {}", meter, e.getMessage());
            return;
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(response.toString());
        }
        String ean = null;
        try {
            ean = JsonPath.read(response.getBody(), "$['meteringPointId']");
            mpToDevice.put(ean,meter);
            insertEntryToDB(ean,meter);
        } catch (PathNotFoundException e) {
                logger.info(e.getMessage());
        }
    }

    private void insertEntryToDB(String ean, String meter) {
        getMeteringPointFromDB.updateH2DB(meter,ean);
    }

    public Map<String, String> getMpToDevice() {
        return mpToDevice;
    }
}

