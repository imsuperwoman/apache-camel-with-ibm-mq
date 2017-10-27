package com.asp.proxy;

import java.io.IOException;
import java.io.InputStream;

public interface ResponseFrameDecoder {

    byte[] onDecodeResponseFrame(byte[] requestFrame, InputStream stream)
            throws IOException;

    void dispose();

}
