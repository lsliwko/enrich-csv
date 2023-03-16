package com.server.service.mapper;

import java.io.IOException;
import java.util.Optional;

public interface EnrichFieldMapper {

    //name of the field to be mapped from when enriching csv
    String getInputFieldName();

    //name of the field to be mapped into when enriching csv
    String getOutputFieldName();

    Optional<String> findOutputValueById(String id) throws IOException;

    String defaultValue() throws IOException;

}
