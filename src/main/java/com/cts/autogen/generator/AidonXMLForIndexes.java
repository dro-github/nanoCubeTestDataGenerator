package com.cts.autogen.generator;

import com.cts.autogen.logger.BasicConfApp;
import com.cts.autogen.techyon.api.GetLastIndexForSensor;
import com.cts.autogen.techyon.api.GetSettlementMethodForMeteringPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AidonXMLForIndexes {
    private final String fileToPrint;
    public static final String XMLHEADERS;
    public static final String SEG_0;
    public static final String SEG_1;
    public static final String SEG_2;
    ;
    BigDecimal AEDCurrentIndex;
    BigDecimal AEUCurrentIndex;
    BigDecimal REDCurrentIndex;
    BigDecimal REUCurrentIndex;
    private final ArrayList<String> sensorTypes = new ArrayList<>(Arrays.asList("AEDownstream", "AEUpstream", "REDownstream", "REUpstream"));
    private final Map<String, String> productName = Map.of("AEDownstream", "000000 Aplus 60 min ES1", "AEUpstream", "000002 AMinus 60  min ES3", "REDownstream", "000001 RPlus 60  min ES2", "REUpstream", "000003 RMinus 60 min ES4");
    private final Map<String, String> productNameProfile = Map.of("AEDownstream", "000000 Aplus profile ES1", "AEUpstream", "000002 AMinus profile ES3", "REDownstream", "000001 RPlus profile ES2", "REUpstream", "000003 RMinus profile ES4");
    private final Map<String, String> sourceRegister = Map.of("AEDownstream", "ActivePlus", "AEUpstream", "ActiveMinus", "REDownstream", "ReactivePlus", "REUpstream", "ReactiveMinus");
    private final Map<String, String> valueID = Map.of("AEDownstream", "A+", "AEUpstream", "A-", "REDownstream", "R+", "REUpstream", "R-");

    private ZonedDateTime startDateTime;
    private static final Logger logger = LoggerFactory.getLogger(BasicConfApp.class);

    private final String pathToDropzoneForEnvironment;
    private final String apiKey;
    private final String contentType;
    private final boolean filGapsSinceLastIndex;
    private final JdbcTemplate jdbcTemplate;

    private BigDecimal gridLossKP1 = BigDecimal.ZERO;
    private BigDecimal gridLossKP2 = BigDecimal.ZERO;
    private BigDecimal gridLossHYV = BigDecimal.ZERO;


    static {
        String Headers_content = null;
        String SEG_0_content = null;
        String SEG_1_content = null;
        String SEG_2_content = null;

        InputStream resourceStreamHeaders = AidonXMLForIndexes.class.getClassLoader().getResourceAsStream("AidonXML_Headers.txt");
        InputStream resourceStreamSEG0 = AidonXMLForIndexes.class.getClassLoader().getResourceAsStream("AidonXML_SEG0.txt");
        InputStream resourceStreamSEG1 = AidonXMLForIndexes.class.getClassLoader().getResourceAsStream("AidonXML_SEG1.txt");
        InputStream resourceStreamSEG2 = AidonXMLForIndexes.class.getClassLoader().getResourceAsStream("AidonXML_SEG2.txt");

        assert resourceStreamHeaders != null;
        Headers_content = new Scanner(resourceStreamHeaders).useDelimiter("\\Z").next();
        assert resourceStreamSEG0 != null;
        SEG_0_content = new Scanner(resourceStreamSEG0).useDelimiter("\\Z").next();
        assert resourceStreamSEG1 != null;
        SEG_1_content = new Scanner(resourceStreamSEG1).useDelimiter("\\Z").next();
        assert resourceStreamSEG2 != null;
        SEG_2_content = new Scanner(resourceStreamSEG2).useDelimiter("\\Z").next();

        XMLHEADERS = Headers_content;
        SEG_0 = SEG_0_content;
        SEG_1 = SEG_1_content;
        SEG_2 = SEG_2_content;
    }

    public AidonXMLForIndexes(String url,JdbcTemplate jdbcTemplate, String pathToDropzoneForEnvironment, String apiKey, String contentType, Map<String, String> meteringPointToMeter, boolean filGapsSinceLastIndex) throws ParseException {
        this.jdbcTemplate = jdbcTemplate;
        this.pathToDropzoneForEnvironment = pathToDropzoneForEnvironment;
        this.apiKey = apiKey;
        this.contentType = contentType;
        //TODO: Change to 'true' for forcing resend of data for a period with existing data:
        this.filGapsSinceLastIndex = filGapsSinceLastIndex;
        fileToPrint = makeTimeSeries(url,meteringPointToMeter);
    }

    private String makeTimeSeries(String url,Map<String, String> meteringPointToMeter) throws ParseException {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        StringBuilder headers = new StringBuilder();
        LocalDateTime ldt = LocalDateTime.now();
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("Europe/Oslo"));
        String created = dateFormat.format(zdt);

        StringBuilder seg_0 = new StringBuilder();
        int mpCounter = 0;
        Random r = new Random();
        int low = 100;
        int high = 375;
        int result = r.nextInt(high - low) + low;
        int profileMeteringPointsCounter = 0;
        for (String key : meteringPointToMeter.keySet()) {
            String meter = meteringPointToMeter.get(key);
            String settlementMethod = new GetSettlementMethodForMeteringPoint(url,jdbcTemplate, key).getSettlementMethodString();
            if (settlementMethod.equalsIgnoreCase("Profiled") && ZonedDateTime.now().getHour() != 1) {
                profileMeteringPointsCounter++;
                continue;
            }
            mpCounter++;
            if (mpCounter % result == 0) {
                logger.info("Omitting value for metering point {}, for the current hour.", key);
                if (meter.contains("KP1")){
                    gridLossKP1 = gridLossKP1.add(BigDecimal.valueOf(3.5));
                }
                else if (meter.contains("KP2")){
                    gridLossKP2 = gridLossKP2.add(BigDecimal.valueOf(3.5));
                }
                else if (meter.contains("HYV")) {
                    gridLossHYV = gridLossHYV.add(BigDecimal.valueOf(3.5));
                }
                continue;
            }
            seg_0.append(getSeg0(key, meter, settlementMethod));
            if (mpCounter % 10 == 0) {
                logger.info("Aidon transaction completed for {} metering points. Transaction contains hourly indexes on 4 sensors AE/RE Down/Upstream.", mpCounter);
            }
            if (mpCounter < meteringPointToMeter.keySet().size()) {
                seg_0.append("\n");
            }
        }
        logger.info("Aidon transaction completed for {} metering points. Transaction contains hourly indexes on 4 sensors AE/RE Down/Upstream.", mpCounter);
        headers.append(String.format(XMLHEADERS, created, seg_0));
        logger.info("Profile readings will be created for {} profile metering points only once per day, between 01:00 and 02:00 each night, for the previous day.", profileMeteringPointsCounter);
        return headers.toString();
    }

    private String getSeg0(String meteringPoint, String device, String settlementMethod) throws ParseException {
        StringBuilder seg_0 = new StringBuilder();
        String seg1 = getSeg1(meteringPoint, settlementMethod,device);
        seg_0.append(String.format(SEG_0, meteringPoint, device, seg1));
        return seg_0.toString();
    }

    private String getInterval(String settlementMethod) {
        if (settlementMethod.equalsIgnoreCase("Profiled")) {
            return "0";
        }
        return "60";
    }

    private String getSeg1(String meteringPoint, String settlementMethod,String device) throws ParseException {
        StringBuilder seg_1 = new StringBuilder();
        String interval = getInterval(settlementMethod);
        int sensorCounter = 0;
        for (String sensorType : sensorTypes) {
            sensorCounter ++;
            BigDecimal lastIndexForSensor = getLastIndexForSensor(meteringPoint, sensorType, getInterval(settlementMethod));
            assignLastIndex(sensorType, lastIndexForSensor);
            String productName = getProductName(settlementMethod,sensorType);
            String sourceRegister = this.sourceRegister.get(sensorType);
            if (filGapsSinceLastIndex) {
                startDateTime = getLastTimestampForSensor(meteringPoint, sensorType, interval).toInstant().atZone(ZoneId.of("Europe/Oslo"));
                if (settlementMethod.equalsIgnoreCase("Profiled")) {
                    startDateTime = startDateTime.plusDays(1).withHour(0).withMinute(0).withSecond(0);
                } else {
                    startDateTime = startDateTime.plusHours(1);
                }
                while (startDateTime.isBefore(ZonedDateTime.now().withMinute(0).withSecond(0).withNano(0))) {
                    seg_1.append(String.format(SEG_1, interval, productName, sourceRegister,getSeg2(sensorType,settlementMethod,device)));

                    if (startDateTime.isBefore(ZonedDateTime.now().minusHours(1)) && sensorCounter < 4) {
                        seg_1.append("\n");
                    }
                    if (settlementMethod.equalsIgnoreCase("Profiled")) {
                        startDateTime = startDateTime.plusDays(1);
                    } else {
                        startDateTime = startDateTime.plusHours(1);
                    }
                }
            } else {
                startDateTime = ZonedDateTime.now().minusHours(1).withMinute(0).withSecond(0).withNano(0);
                seg_1.append(String.format(SEG_1, interval, productName, sourceRegister,getSeg2(sensorType,settlementMethod,device)));
            }
        }
        return seg_1.toString();
    }

    private String getProductName(String settlementMethod, String sensorType) {
        if (settlementMethod.equalsIgnoreCase("Profiled")){
            return this.productNameProfile.get(sensorType);
        }
        return this.productName.get(sensorType);
    }

    private Date getLastTimestampForSensor(String meteringPoint, String sensorType, String interval) throws ParseException {
        String timestamp = new GetLastIndexForSensor(pathToDropzoneForEnvironment, apiKey, contentType, meteringPoint, sensorType, interval).getTimestamp();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        return formatter.parse(timestamp);
    }

    private BigDecimal getLastIndexForSensor(String meteringPoint, String sensorType, String interval) {
        return new GetLastIndexForSensor(pathToDropzoneForEnvironment, apiKey, contentType, meteringPoint, sensorType, interval).getLastIndex();
    }

    private void assignLastIndex(String sensorType, BigDecimal lastIndexForSensor) {
        if (sensorType.equalsIgnoreCase("AEDownstream")) {
            AEDCurrentIndex = lastIndexForSensor;
        } else if (sensorType.equalsIgnoreCase("AEUpstream")) {
            AEUCurrentIndex = lastIndexForSensor;
        } else if (sensorType.equalsIgnoreCase("REDownstream")) {
            REDCurrentIndex = lastIndexForSensor;
        } else if (sensorType.equalsIgnoreCase("REUpstream")) {
            REUCurrentIndex = lastIndexForSensor;
        }
    }

    private String getSeg2(String sensorType, String settlementMethod,String device) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        StringBuilder seg_2 = new StringBuilder();
        String value_id = valueID.get(sensorType);
        String timestamp = dateFormat.format(startDateTime);//getTimestampForThisTransaction();
        String value = setValue(sensorType, settlementMethod,device);
        String quality = "0";
        String unit = "kWh";
        seg_2.append(String.format(SEG_2, value_id, timestamp, value, quality, unit));
        return seg_2.toString();
    }

    private String setValue(String sensorType, String settlementMethod,String device) {
        BigDecimal low = BigDecimal.valueOf(0.5);
        BigDecimal high = BigDecimal.valueOf(3.75);
        if (device.contains("_NAER_")){
            low = low.add(BigDecimal.valueOf(4.5));
            high = high.add(BigDecimal.valueOf(7.0));
        }
        BigDecimal nextVal = low.add(BigDecimal.valueOf(Math.random()).multiply(high.subtract(low))).setScale(3, RoundingMode.CEILING);
        if (settlementMethod.equalsIgnoreCase("Profiled")) {
            nextVal = nextVal.multiply(new BigDecimal(24));
        }

        if (sensorType.equalsIgnoreCase("AEDownstream")) {
            //TODO: Change the added value to nextVal for "regular" run, or a higher value for overFuseCapacity:
            AEDCurrentIndex = AEDCurrentIndex.add(nextVal);
            if (device.contains("KP1")){
                gridLossKP1 = gridLossKP1.add(nextVal);
            }
            else if (device.contains("KP2")){
                gridLossKP2 = gridLossKP2.add(nextVal);
            }
            else if (device.contains("HYV")) {
                gridLossHYV = gridLossHYV.add(nextVal);
            }
            return String.valueOf(AEDCurrentIndex.setScale(3, RoundingMode.HALF_EVEN));
        } else if (sensorType.equalsIgnoreCase("AEUpstream")) {
            AEUCurrentIndex = BigDecimal.ZERO;
            return String.valueOf(AEUCurrentIndex.setScale(3, RoundingMode.FLOOR));
        } else if (sensorType.equalsIgnoreCase("REDownstream")) {
            REDCurrentIndex = REDCurrentIndex.add(nextVal.divide(BigDecimal.TEN, RoundingMode.FLOOR));
            return String.valueOf(REDCurrentIndex.setScale(3, RoundingMode.HALF_EVEN));
        }
        REUCurrentIndex = REUCurrentIndex.add(nextVal.divide(new BigDecimal(1000), RoundingMode.HALF_UP));
        return String.valueOf(REUCurrentIndex.setScale(3, RoundingMode.HALF_EVEN));
    }

    private String getTimestampForThisTransaction() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        LocalDateTime ldt = LocalDateTime.now();
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("Europe/Oslo"));
        ZonedDateTime thisTimestamp = zdt.minusHours(1).withMinute(0).withSecond(0).withNano(0);
        return dateFormat.format(thisTimestamp);
    }

    public String getFileToPrint() {
        return fileToPrint;
    }

    public BigDecimal getGridLossKP1() {
        return gridLossKP1;
    }

    public BigDecimal getGridLossKP2() {
        return gridLossKP2;
    }

    public BigDecimal getGridLossHYV() {
        return gridLossHYV;
    }

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }
}
