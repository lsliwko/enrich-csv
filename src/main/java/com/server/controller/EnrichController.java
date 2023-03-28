package com.server.controller;

import com.server.controller.media.MediaTypeExt;
import com.server.exception.CsvException;
import com.server.service.EnrichService;
import com.server.service.mapper.ProductNameEnrichFieldMapper;
import com.server.service.model.TradeCsvModel;
import com.server.service.response.StreamingResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
    public ResponseEntity<StreamingResponseBody> enrichTradeCsvAsync(
            HttpServletRequest request
    ) {

        AtomicReference<Charset> referenceCharset   = new AtomicReference<>(StandardCharsets.UTF_8);    //we need to provide default charset for error handling (and also final val for lambdas)
        try {
            referenceCharset.setPlain(Charset.forName(request.getCharacterEncoding()));  //supports: text/csv; charset=utf-8, throws UnsupportedEncodingException

            //NOTE: request's input-stream shouldn't be closed here (it's managed by Spring)
            InputStreamReader csvReader = new InputStreamReader(request.getInputStream(), referenceCharset.get());

            //first validate header
            StreamingResponse streamingResponse = enrichService.enrichCsvAsStreamingResponse(csvReader, tradeCsvModel, productNameEnrichFieldMapper);

            //after validations, start streaming response
            return ResponseEntity
                    .ok()
                    .contentType(MediaTypeExt.TEXT_CSV.forCharset(referenceCharset.get()))
                    .body(outputStream -> {
                        try {
                            streamingResponse.writeTo(outputStream, referenceCharset.get());
                            //TODO add end-of-data marker (or checksum) to notify requester that all data has been sent (in case connection has been cut or processing failed)
                        } catch (IOException exception) {
                            //report an error, because we cannot do anything else, http protocol doesn't allow to report error while streaming
                            logger.error(String.format("Error streaming enriched trade csv: %s", exception.getMessage()), exception);
                            throw exception;
                        }
                    });
        } catch (CsvException | UnsupportedEncodingException exception) {
            //reportable error (show error message)
            logger.error("Error enriching trade csv (bad request)", exception);
            return ResponseEntity.badRequest().body(outputStream ->
                    outputStream.write(exception.getMessage().getBytes(referenceCharset.get()))
            );
        } catch (Exception exception) {
            //internal error, e.g. IOException
            logger.error("Error enriching trade csv (internal server error)", exception);
            return ResponseEntity.internalServerError().body(outputStream ->
                    outputStream.write("Internal Server Error".getBytes(referenceCharset.get()))
            );
        }
    }

}
