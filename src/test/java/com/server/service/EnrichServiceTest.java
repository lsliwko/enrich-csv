package com.server.service;

import com.server.exception.CsvException;
import com.server.service.mapper.EnrichFieldMapper;
import com.server.service.mapper.ProductNameEnrichFieldMapper;
import com.server.service.model.TradeCsvModel;
import com.server.service.response.StreamingResponse;
import io.github.netmikey.logunit.api.LogCapturer;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@SpringBootTest
public class EnrichServiceTest {

    @Autowired
    private EnrichService enrichService;

    @Autowired
    private TradeCsvModel tradeCsvModel;

    @RegisterExtension
    private LogCapturer logs = LogCapturer.create().captureForType(EnrichService.class);

    @Test
    public void testEnrichCsvHappyPath() throws Exception {
        assertThat(execEnrichCsv(
                "date,product_id,currency,price\n" +
                        "20160101,1,EUR,10.0\n" +
                        "20160101,2,EUR,20.1\n" +
                        "20160101,3,EUR,30.34\n"
        )).isEqualToIgnoringNewLines(
                "date,product_name,currency,price\n" +
                        "20160101,test-value-1,EUR,10.0\n" +
                        "20160101,test-value-2,EUR,20.1\n" +
                        "20160101,test-value-3,EUR,30.34\n"
        );
    }

    @Test
    public void testEnrichCsvEmptyBody() throws Exception {
        assertThrows(CsvException.class, () -> execEnrichCsv(""));
    }

    @Test
    public void testEnrichCsvMissingProduct99() throws Exception {
        assertThat(execEnrichCsv(
                "date,product_id,currency,price\n" +
                        "20160101,1,EUR,10.0\n" +
                        "20160101,99,EUR,20.1\n" +
                        "20160101,99,EUR,30.34\n"
        )).isEqualToIgnoringNewLines(
                "date,product_name,currency,price\n" +
                        "20160101,test-value-1,EUR,10.0\n" +
                        "20160101,default-value,EUR,20.1\n" +
                        "20160101,default-value,EUR,30.34\n"
        );
   }

    @Test
    public void testEnrichCsvDiscardRowInvalidDate() throws Exception {
        assertThat(execEnrichCsv(
                "date,product_id,currency,price\n" +
                        "20160101,1,EUR,10.0\n" +
                        "01012016,2,EUR,20.1\n" +
                        "20160101,3,EUR,30.34\n"
        )).isEqualToIgnoringNewLines(
                "date,product_name,currency,price\n" +
                        "20160101,test-value-1,EUR,10.0\n" +
                        "20160101,test-value-3,EUR,30.34\n"
        );

        logs.assertContains("Error reading csv line no 3 [01012016,2,EUR,20.1]: the value '01012016' cannot be parsed as a date with format 'yyyyMMdd'");
    }

    @Test
    public void testEnrichCsvDiscardRowInvalidPrice() throws Exception {
        assertThat(execEnrichCsv(
                "date,product_id,currency,price\n" +
                        "20160101,1,EUR,10.0\n" +
                        "20160101,2,EUR,20.1\n" +
                        "20160101,3,EUR,AAA\n"
        )).isEqualToIgnoringNewLines(
                "date,product_name,currency,price\n" +
                        "20160101,test-value-1,EUR,10.0\n" +
                        "20160101,test-value-2,EUR,20.1\n"
        );

        logs.assertContains("Error reading csv line no 4 [20160101,3,EUR,AAA]: 'AAA' could not be parsed as a Double");
    }

    @Test
    public void testEnrichCsvDiscardRowInvalidCurrency() throws Exception {
        assertThat(execEnrichCsv(
                "date,product_id,currency,price\n" +
                        "20160101,1,EUR,10.0\n" +
                        "20160101,2,EUR,20.1\n" +
                        "20160101,3,x,30.34\n"
        )).isEqualToIgnoringNewLines(
                "date,product_name,currency,price\n" +
                        "20160101,test-value-1,EUR,10.0\n" +
                        "20160101,test-value-2,EUR,20.1\n"
        );

        logs.assertContains("Error reading csv line no 4 [20160101,3,x,30.34]: 'x' does not match the regular expression '[A-Z]{3}'");
    }

    private String execEnrichCsv(String inputCsv) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (
                Reader reader = new InputStreamReader(new ByteArrayInputStream(inputCsv.getBytes()), StandardCharsets.UTF_8);
        ) {
            StreamingResponse streamingResponse = enrichService.enrichCsvAsStream(reader, tradeCsvModel, testEnrichFieldMapper);
            streamingResponse.writeTo(outputStream, StandardCharsets.UTF_8);
        }
        return outputStream.toString();
    }


    private EnrichFieldMapper testEnrichFieldMapper = new ProductNameEnrichFieldMapper() {

        @Override
        public Optional<String> findOutputValueById(String id) {
            if ("99".equals(id)) return Optional.empty();
            else return Optional.of("test-value-" + id);
        }

        @Override
        public String defaultValue() {
            return "default-value";
        }
    };
}
