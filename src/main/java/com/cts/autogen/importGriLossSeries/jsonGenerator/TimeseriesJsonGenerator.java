package com.cts.autogen.importGriLossSeries.jsonGenerator;

import com.cts.autogen.importGriLossSeries.messageModel.TimeSeriesMessages;
import com.cts.autogen.importGriLossSeries.payloadArgs.PayLoadArgs;
import com.cts.autogen.logger.BasicConfApp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TimeseriesJsonGenerator {
    private static final Logger logger = LoggerFactory.getLogger(BasicConfApp.class);

    private final String jsonPayloadFromInput;
    public TimeseriesJsonGenerator(PayLoadArgs jsonPayload) throws JsonProcessingException {

        DateTimeFormatter jsonOutput = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");
        DateTimeFormatter input = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        TimeSeriesMessages msg = new TimeSeriesMessages();
        msg.timeSeriesMessages = new ArrayList<>();
        TimeSeriesMessages.TimeSeriesMessage m = new TimeSeriesMessages.TimeSeriesMessage();
        m.meteringPointId = jsonPayload.meterPointId;
        m.sensorType = jsonPayload.sensorType;
        m.unit = jsonPayload.unit;
        m.direction = jsonPayload.direction;
        m.resolution = jsonPayload.resolution;
        m.from = jsonPayload.from;
        m.to = jsonPayload.to;
        m.dataPoints = new ArrayList<>();

        TimeSeriesMessages.TimeSeries timeSeries = new TimeSeriesMessages.TimeSeries();
        timeSeries.from = jsonPayload.from;
        timeSeries.to = jsonPayload.to;
        TimeSeriesMessages.DataPoint dp = new TimeSeriesMessages.DataPoint();
        dp.createdTime = jsonOutput.format(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Europe/Oslo")));
        dp.origin = jsonPayload.origin;
        dp.tags = new ArrayList<>();
        Map<String,String> oneTag = new HashMap<>();
        oneTag.put("key","Comment");
        oneTag.put("value","Davids copied value for grid loss calculation (added 3.5%)");
        dp.tags.add(oneTag);
        dp.value = jsonPayload.value.multiply(BigDecimal.valueOf(1.035));// percentage for grid loss.
        timeSeries.dataPoint = dp;
        m.dataPoints.add(timeSeries);

        msg.timeSeriesMessages.add(m);
        ObjectMapper mpr = new ObjectMapper();
        jsonPayloadFromInput = mpr.writeValueAsString(msg);
    }

    public String getJsonPayloadFromInput() {
        return jsonPayloadFromInput;
    }
}

