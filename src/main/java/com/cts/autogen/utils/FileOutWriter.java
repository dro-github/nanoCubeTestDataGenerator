package com.cts.autogen.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileOutWriter {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String pathToOutput;
    public FileOutWriter(Document xmlDoc, String consumer) throws TransformerException, IOException {
        String currentDirectory = String.valueOf(new File(System.getProperty("user.dir")).toPath());
        pathToOutput = currentDirectory + "\\XMLPayload\\" + consumer + ".xml";
        writeFileToFolder(xmlDoc);
        logger.info("Output written to {}.",pathToOutput);
    }

    private void writeFileToFolder(Document doc) throws TransformerException, IOException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        DOMSource source = new DOMSource(doc);
        FileWriter writer = new FileWriter(pathToOutput);
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
    }
}
