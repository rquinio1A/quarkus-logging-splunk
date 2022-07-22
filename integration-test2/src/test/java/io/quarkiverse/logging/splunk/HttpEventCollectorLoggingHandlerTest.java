package io.quarkiverse.logging.splunk;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;

public class HttpEventCollectorLoggingHandlerTest {

    static SplunkResource resource;

    @BeforeAll
    public static void setupOnce() throws IOException {
        resource = new SplunkResource();
        Map<String, String> splunkInfo = resource.start();

        StringBuffer config = new StringBuffer();
        config.append("handlers = com.splunk.logging.HttpEventCollectorLoggingHandler").append("\n");
        config.append("com.splunk.logging.HttpEventCollectorLoggingHandler.url=").append(splunkInfo.get("quarkus.log.handler.splunk.url")).append("\n");
        config.append("com.splunk.logging.HttpEventCollectorLoggingHandler.token=29fe2838-cab6-4d17-a392-37b7b8f41f75").append("\n");
        config.append("com.splunk.logging.HttpEventCollectorLoggingHandler.batch_interval = 10").append("\n");
        config.append("com.splunk.logging.HttpEventCollectorLoggingHandler.batch_size_count = 10").append("\n");

        System.out.println(config);
        LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(config.toString().getBytes()));

        assertNotNull(LogManager.getLogManager().getProperty("com.splunk.logging.HttpEventCollectorLoggingHandler.url"));
    }

    @AfterAll
    public static void tearDownOnce() {
        LogManager.getLogManager().reset();
        resource.stop();
    }

    @Test
    void test() throws InterruptedException {
        Logger logger = Logger.getLogger(HttpEventCollectorLoggingHandlerTest.class.getName());
        logger.info("log message");
        Thread.sleep(10000);
    }
}
