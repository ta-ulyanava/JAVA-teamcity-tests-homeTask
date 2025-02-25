package com.example.teamcity.api.responses;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpStatus;

public class ResponseLogger {
    private static final Logger logger = LoggerFactory.getLogger(ResponseLogger.class);

    public static void logIfError(Response response) {
        int statusCode = response.getStatusCode();
        if (statusCode < HttpStatus.SC_OK || statusCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
            logger.error("❌ API ERROR: Status Code: {}", statusCode);
            logger.error("Response Body: {}", response.getBody().asString());
        }
    }
}
