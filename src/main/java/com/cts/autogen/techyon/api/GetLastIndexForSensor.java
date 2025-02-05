package com.cts.autogen.techyon.api;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetLastIndexForSensor {
    Logger logger = LoggerFactory.getLogger(getClass());
    private final BigDecimal value;
    private String timestamp;
    public GetLastIndexForSensor(String pathToOpenAPIForEnvironment,String apiKey,String contentType,String meteringPoint, String sensorTypeAndDirection,String interval){
        String direction = getDirection(sensorTypeAndDirection);
        String sensorType = getSensorType(sensorTypeAndDirection);
        String payload = "https://" + pathToOpenAPIForEnvironment + "api/measurements?meteringPointId=" + meteringPoint + "&sensorType=" + sensorType + "&requestType=SurroundingIndexes" + "&resolution=PT1H&direction=" + direction;
        //TODO: For sending data back in time:
        //String payload = "https://" + pathToOpenAPIForEnvironment + "api/measurements?meteringPointId=" + meteringPoint + "&sensorType=" + sensorType + "&requestType=IndexesInPeriod" + "&resolution=PT1H&direction=" + direction + "&from=2024-11-01T00:00:00&to=2024-11-01T01:00:00";
        if (interval.equalsIgnoreCase("0")){
            payload = "https://" + pathToOpenAPIForEnvironment + "api/measurements?meteringPointId=" + meteringPoint + "&sensorType=" + sensorType + "&requestType=SurroundingIndexes" + "&direction=" + direction;
        }
        RestTemplate restClient = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set("Authorization", apiKey);
        var entity = new HttpEntity<>(headers);
        var response = restClient.exchange(payload, HttpMethod.GET, entity, String.class);
        List<BigDecimal> values = new ArrayList<>();
        int dayCounter = 0;
        int hourCounter = 0;
        try {
            var res = JsonPath.read(response.getBody(), "$['measurementMessages'][" + dayCounter + "]['indexes'][" + hourCounter + "]['dataPoint']['value']").toString();
            values.add(new BigDecimal(res).setScale(3, RoundingMode.HALF_EVEN));
        } catch (PathNotFoundException e) {
            values.add(BigDecimal.ZERO);
            logger = LoggerFactory.getLogger(getClass());
            logger.info("Metering point = {}, Sensor = {}, direction = {} has no previous index.",meteringPoint,sensorType,direction);
        }
        value = values.get(values.size() - 1);
        try {
            timestamp = JsonPath.read(response.getBody(), "$['measurementMessages'][" + dayCounter + "]['indexes'][" + hourCounter + "]['readTime']");
        } catch (PathNotFoundException ex) {
            logger.info("Metering point = {}, Sensor = {}, direction = {} has no previous timestamp.",meteringPoint,sensorType,direction);
            Date now = new Date();
            OffsetDateTime time = now.toInstant().truncatedTo(ChronoUnit.SECONDS).atZone(ZoneId.of("Europe/Oslo")).toOffsetDateTime();
            ZonedDateTime zdt = time.toZonedDateTime().minusHours(2).withMinute(0).withSecond(0);
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
            timestamp = dateFormat.format(zdt);
        }
    }

    public BigDecimal getLastIndex(){
        return value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    private String getDirection(String sensorTypeAndDirection){
        if (sensorTypeAndDirection.contains("Downstream")){
            return "Downstream";
        }
        else if (sensorTypeAndDirection.contains("Upstream")){
            return "Upstream";
        }
        return "Downstream"; //Default
    }

    private String getSensorType(String sensorTypeAndDirection) {
        if (sensorTypeAndDirection.startsWith("AE")){
            return "ActiveEnergy";
        }
        else if (sensorTypeAndDirection.startsWith("RE")){
            return "ReactiveEnergy";
        }
        return "ActiveEnergy"; //Default
    }
}
