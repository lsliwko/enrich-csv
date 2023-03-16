package com.server.service.model;

import com.server.service.constraint.StrDateFormat;
import org.springframework.stereotype.Component;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.StrRegEx;
import org.supercsv.cellprocessor.ift.CellProcessor;

@Component
public class TradeCsvModel implements CsvModel {

    public static final String[] CSV_HEADER = new String[] {
            "date",
            "product_id",
            "currency",
            "price"
    };

    public static final CellProcessor[] CSV_PROCESSORS = new CellProcessor[] {
            new StrDateFormat("yyyyMMdd"), // date in 20221231 format
            new ParseLong(), // product_id in number format
            new StrRegEx("[A-Z]{3}"), // currency in three capital letters format
            new ParseDouble(), // price
    };

    @Override
    public String[] getHeader() {
        return CSV_HEADER;
    }

    @Override
    public CellProcessor[] getProcessors() {
        return CSV_PROCESSORS;
    }
}
