package com.server.controller.media;

import org.springframework.http.MediaType;

import java.nio.charset.Charset;

public class MediaTypeExt extends MediaType {

    //Spring doesn't provide text/csv in MediaType
    public static final MediaTypeExt TEXT_CSV   =  new MediaTypeExt("text","csv");
    public static final String TEXT_CSV_VALUE   = TEXT_CSV.toString();

    public MediaTypeExt(String type, String subtype) {
        super(type, subtype);
    }

    public MediaTypeExt(String type, String subtype, Charset charset) {
        super(type, subtype, charset);
    }

    public MediaTypeExt forCharset(Charset charset) {
        return new MediaTypeExt(this.getType(), this.getSubtype(), charset);
    }
}
