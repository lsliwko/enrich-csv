package com.server.service.constraint;

import org.apache.commons.validator.routines.DateValidator;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.exception.SuperCsvConstraintViolationException;
import org.supercsv.util.CsvContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//custom version of date format constraint
public class StrDateFormat extends CellProcessorAdaptor implements StringCellProcessor {
    private final DateValidator dateValidator   = new DateValidator();
    private final String pattern;

    public StrDateFormat(String pattern) {
        this.pattern = pattern;
        checkPreconditions(pattern);
    }

    public StrDateFormat(String pattern, CellProcessor next) {
        super(next);
        this.pattern = pattern;
        checkPreconditions(pattern);
    }

    private static void checkPreconditions(String pattern) {
        if (pattern == null) {
            throw new NullPointerException("pattern should not be null");
        } else if (pattern.length() == 0) {
            throw new IllegalArgumentException("pattern should not be empty");
        }
    }

    public Object execute(Object value, CsvContext context) {
        this.validateInputNotNull(value, context);
        String stringValue = value.toString();
        if (dateValidator.validate(stringValue, pattern) == null) {
            throw new SuperCsvConstraintViolationException(String.format("the value '%s' cannot be parsed as a date with format '%s'", stringValue, pattern), context, this);
        }
        return this.next.execute(stringValue, context);
    }
}
