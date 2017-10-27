package com.asp.proxy;

import java.io.IOException;
import java.io.InputStream;

public interface RequestFrameDecoder {

	byte[] onDecodeRequestFrame(InputStream stream) throws IOException;

	void dispose();
	
}
