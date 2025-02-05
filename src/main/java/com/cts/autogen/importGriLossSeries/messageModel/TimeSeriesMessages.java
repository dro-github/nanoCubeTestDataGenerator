package com.cts.autogen.importGriLossSeries.messageModel;

import com.cts.autogen.logger.BasicConfApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimeSeriesMessages {
    private static final Logger logger = LoggerFactory.getLogger(BasicConfApp.class);
    public boolean applyScalingFactor = true;
    public List<TimeSeriesMessage> timeSeriesMessages;

    public static class TimeSeriesMessage {
        public String meteringPointId;
        public String sensorType;
        public String unit;
        public String direction;
        public String resolution;
        public String from;
        public String to;
        public boolean profiled;
        public List<TimeSeries> dataPoints = new ArrayList<>();
    }

    public static class TimeSeries {
        public String from;
        public String to;
        public DataPoint dataPoint;
    }

    public static class DataPoint {
        public String origin;
        public BigDecimal value;
        public String createdTime;
        public List<Map<String,String>> tags;
    }
}
