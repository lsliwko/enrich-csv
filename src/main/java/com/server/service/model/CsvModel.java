package com.server.service.model;

import org.supercsv.cellprocessor.ift.CellProcessor;

import java.nio.charset.Charset;

public interface CsvModel {

    String[] getHeader();
    CellProcessor[] getProcessors();

}
