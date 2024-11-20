package com.cts.autogen.controller;

import com.cts.autogen.generator.AidonXMLForIndexes;
import com.cts.autogen.logger.BasicConfApp;
import com.cts.autogen.techyon.api.GetMeterForMeteringPoint;
import com.cts.autogen.techyon.api.UploadXMLToDropzone;
import com.cts.autogen.utils.FileOutWriter;
import com.cts.autogen.utils.XMLFromString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class Controller {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${printFileToFolder}")
    private boolean printFileToFolder;

    @Value("${filGapsSinceLastIndex}")
    private boolean filGapsSinceLastIndex;

    @Value("${pathToDropzoneForEnvironment}")
    private String pathToDropzoneForEnvironment;

    @Value("${apiKey}")
    private String apiKey;

    @Value("${contentType}")
    private String contentType;

    private final Logger logger = LoggerFactory.getLogger(BasicConfApp.class);
    private final String scanningInterval = "3600000";
    private boolean running = false;

    @Scheduled(fixedRateString = scanningInterval)
    public void execute() {
        if (running) {
            return;
        }
        try {
            running = true;
            Thread.sleep(500);
            startProgram();
        } catch (IOException | ParseException | TransformerException e) {
            logger.error("Error occurred. Message: {}", e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            running = false;
        }
    }

    private void startProgram() throws IOException, InterruptedException, ParseException, TransformerException {
        Map<String, String> meterToMeteringPoint = initiateMap();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        LocalDateTime ldt = LocalDateTime.now();
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("Europe/Oslo"));
        ZonedDateTime nextRun = zdt;
        nextRun = nextRun.plusHours(1);
        String nextRunAt = dateFormat.format(nextRun);
        ZonedDateTime nextIndexTimestamp = zdt;
        nextIndexTimestamp = nextIndexTimestamp.withMinute(0).withSecond(0);
        String nextIndexAt = dateFormat.format(nextIndexTimestamp);
        zdt = zdt.minusHours(1).withMinute(0).withSecond(0);
        String created = dateFormat.format(zdt);
        logger.info("Starting creation of Aidon hourly indexes until {}.",created);
        String xmlAsString = new AidonXMLForIndexes(pathToDropzoneForEnvironment,jdbcTemplate,pathToDropzoneForEnvironment,apiKey,contentType, meterToMeteringPoint,filGapsSinceLastIndex).getFileToPrint();
        //TODO: UploadToDropzone is removed for test purposes. The below line must be uncommented for actual send data and for build for deployment:
        new UploadXMLToDropzone(pathToDropzoneForEnvironment,apiKey,contentType,xmlAsString);
        Document xmlFile = createXMLFromString(xmlAsString);
        if (printFileToFolder) {
            writeFilesToOutputFolder(xmlFile);
        }
        else {
            logger.info("Print XML file to folder is false.");
        }
        logger.info("Next scheduled run within 1 hour @timestamp = {}.",nextRunAt);
        logger.info("Next run will create hourly index for all metering points / sensors @timestamp = {}.",nextIndexAt);
    }

    private Map<String,String> initiateMap() {
        return new GetMeterForMeteringPoint(jdbcTemplate,pathToDropzoneForEnvironment).getMpToDevice();
    }

    private Document createXMLFromString(String xmlAsString) {
        return XMLFromString.convertStringToXMLDocument(xmlAsString);
    }

    private void writeFilesToOutputFolder(Document xmlFileToPrint) throws IOException, TransformerException {
        new FileOutWriter(xmlFileToPrint,System.currentTimeMillis() + "_nanoCubeTestData_AidonIndex");
    }
}
