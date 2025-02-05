package com.cts.autogen.importGriLossSeries;

import com.cts.autogen.importGriLossSeries.api.PostTimeseriesForMeteringPoint;
import com.cts.autogen.importGriLossSeries.jsonGenerator.TimeseriesJsonGenerator;
import com.cts.autogen.importGriLossSeries.payloadArgs.PayLoadArgs;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateAndSendGridLossTimeSeries {
    public CreateAndSendGridLossTimeSeries(Map<String, BigDecimal> mp2Value, ZonedDateTime startTime) throws Exception {
        List<String> payLoadsToPost = new ArrayList<>();
        for (String transformer : mp2Value.keySet()){
            PayLoadArgs thisPayload = new PayLoadArgs(transformer,mp2Value.get(transformer),startTime);
            String jsonPayLoadAsString = new TimeseriesJsonGenerator(thisPayload).getJsonPayloadFromInput();
            payLoadsToPost.add(jsonPayLoadAsString);
        }
        new PostTimeseriesForMeteringPoint(payLoadsToPost);
    }
}
