package com.cts.autogen.techyon.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class UploadXMLToDropzone {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    static RestTemplate restTemplate = new RestTemplate();
    public String url;

    public UploadXMLToDropzone(String pathToDropzoneForEnvironment,String apiKey,String contentType,String xmlFile){
        url = "https://" + pathToDropzoneForEnvironment + "dropzone";//"https://queencontgorg01.staging.techyon.io/api/sink/store"; //https://queencontgorg01.staging.techyon.io/dropzone
        uploadFileToSinkApi(apiKey,contentType,xmlFile);
    }

    private void uploadFileToSinkApi(String apiKey,String contentType,String xmlFile) {
        logger.info("Uploading Aidon indexes file to {}.",url);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set("Authorization", apiKey);
        var entity = new HttpEntity<>(xmlFile, headers);
        var response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        logger.info("RESPONSE = {}",response.getStatusCode());
        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println(response);
            throw new RuntimeException(response.toString());
        }
    }
}
