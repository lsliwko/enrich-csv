package com.server.service.response;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

@FunctionalInterface
public interface StreamingResponse {
    void writeTo(OutputStream outputStream, Charset characterEncoding) throws IOException;
}
