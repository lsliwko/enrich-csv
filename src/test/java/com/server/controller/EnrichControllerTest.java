package com.server.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EnrichControllerTest {

    final static private MediaType TEXT_CSV  = new MediaType("text", "csv");

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void testApiV1EnrichHappyPath() throws Exception {
        ResponseEntity<String> response = execPostApiV1Enrich(
                "date,product_id,currency,price\n" +
                "20160101,1,EUR,10.0\n" +
                "20160101,2,EUR,20.1\n" +
                "20160101,3,EUR,30.34\n" +
                "20160101,11,EUR,35.34\n",
                TEXT_CSV);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getBody()).isEqualToIgnoringNewLines(
                "date,product_name,currency,price\n" +
                        "20160101,Treasury Bills Domestic,EUR,10.0\n" +
                        "20160101,Corporate Bonds Domestic,EUR,20.1\n" +
                        "20160101,REPO Domestic,EUR,30.34\n" +
                        "20160101,Missing Product Name,EUR,35.34\n"
        );
    }

    @Test
    public void testApiV1EnrichDiscardRowInvalidDate() throws Exception {
        ResponseEntity<String> response = execPostApiV1Enrich(
                "date,product_id,currency,price\n" +
                        "20160101,1,EUR,10.0\n" +
                        "20160101,2,EUR,20.1\n" +
                        "01012016,3,EUR,30.34\n" +
                        "20160101,11,EUR,35.34\n",
                TEXT_CSV);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getBody()).isEqualToIgnoringNewLines(
                "date,product_name,currency,price\n" +
                        "20160101,Treasury Bills Domestic,EUR,10.0\n" +
                        "20160101,Corporate Bonds Domestic,EUR,20.1\n" +
                        "20160101,Missing Product Name,EUR,35.34\n"
        );
    }

    @Test
    public void testApiV1EnrichDiscardRowInvalidHeader() throws Exception {
        ResponseEntity<String> response = execPostApiV1Enrich(
                "date,xxx,currency,price\n" +
                        "20160101,1,EUR,10.0\n",
                TEXT_CSV);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testApiV1EnrichDisallowNoMediaType() throws Exception {
        ResponseEntity<String> response = execPostApiV1Enrich("", null);
        assertThat(response.getStatusCode().value()).isEqualTo(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testApiV1EnrichDisallowTextPlainMediaType() {
        ResponseEntity<String> response = execPostApiV1Enrich("", MediaType.TEXT_PLAIN);
        assertThat(response.getStatusCode().value()).isEqualTo(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    private ResponseEntity<String> execPostApiV1Enrich(String body, MediaType mediaType) {

        HttpHeaders headers = new HttpHeaders();

        if (mediaType!=null) {
            headers.setContentType(mediaType);
            headers.setAccept(Collections.singletonList(mediaType));
        }

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = this.restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/enrich",
                request,
                String.class);

        return response;
    }
}
