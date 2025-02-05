package com.cts.autogen.importGriLossSeries.payloadArgs;

import com.cts.autogen.logger.BasicConfApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PayLoadArgs {
    private static final Logger logger = LoggerFactory.getLogger(BasicConfApp.class);

    public String sensorType;
    public String unit;
    public String resolution;
    public String direction;
    public String from;
    public String to;
    public String origin;
    public String meterPointId;
    public BigDecimal value;

    public PayLoadArgs(String meteringPoint, BigDecimal value, ZonedDateTime startTime){
        this.sensorType = "ActiveEnergy";
        this.unit = "kWh";
        this.resolution = "PT1H";
        this.direction = "Downstream";
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        this.from = dateFormat.format(startTime.minusHours(2));
        this.to = dateFormat.format(startTime.minusHours(1));
        this.origin = "Measured";
        this.meterPointId = meteringPoint;
        this.value = value;
    }
}

