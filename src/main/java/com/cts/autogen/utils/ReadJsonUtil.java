package com.cts.autogen.utils;

import com.jayway.jsonpath.JsonPath;

public class ReadJsonUtil {
    private final String meteringPointId;
    public ReadJsonUtil(String payload){
        meteringPointId = readMeteringPointId(payload);
    }

    private String readMeteringPointId(String payload) {
        return JsonPath.read(payload,"$['timeSeriesMessages'][0]['meteringPointId']");
    }

    public String getMeteringPointId() {
        return meteringPointId;
    }
}
