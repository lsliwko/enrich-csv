package com.server.controller;

import com.server.controller.media.MediaTypeExt;
import com.server.exception.CsvException;
import com.server.service.EnrichService;
import com.server.service.mapper.ProductNameEnrichFieldMapper;
import com.server.service.model.TradeCsvModel;
import com.server.service.response.StreamingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.Charset;

@RestController
@RequestMapping("/api/v1")
public class EnrichController {

    final static private Logger logger = LoggerFactory.getLogger(EnrichController.class);

    @Autowired
    private ProductNameEnrichFieldMapper productNameEnrichFieldMapper;

    @Autowired
    private TradeCsvModel tradeCsvModel;

    @Autowired
    private EnrichService enrichService;

    @RequestMapping(
            value = "/enrich",
            consumes = MediaTypeExt.TEXT_CSV_VALUE,
            produces = MediaTypeExt.TEXT_CSV_VALUE,
            method = RequestMethod.POST
    )
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> enrichTradeCsv(
            HttpServletRequest request
    ) {

        try (
                InputStreamReader csvReader = new InputStreamReader(request.getInputStream(), request.getCharacterEncoding())
        ) {
            //enrich service first reads the header to validate it and then returns streaming interface
            StreamingResponse streamingResponse = enrichService.enrichCsvAsStream(csvReader, tradeCsvModel, productNameEnrichFieldMapper);

            Charset charset = Charset.forName(request.getCharacterEncoding());
            return ResponseEntity
                    .ok()
                    .contentType(MediaTypeExt.TEXT_CSV.forCharset(charset))
                    .body(outputStream -> streamingResponse.writeTo(outputStream, charset));
        } catch (CsvException e) {
            //reportable error
            logger.error("Error enriching trade csv (bad request)", e);
            return ResponseEntity.badRequest().body(outputStream -> outputStream.write(e.getMessage().getBytes()));
        } catch (IOException e) {
            //internal error
            logger.error("Error enriching trade csv (internal server error)", e);
            return ResponseEntity.internalServerError().body(outputStream -> outputStream.write("Internal Server Error".getBytes()));
        }
    }

}
