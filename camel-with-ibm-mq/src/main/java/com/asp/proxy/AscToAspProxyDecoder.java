package com.asp.proxy;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Hex;

public class AscToAspProxyDecoder implements RequestFrameDecoder, ResponseFrameDecoder{

    @Override
    public byte[] onDecodeRequestFrame(InputStream stream) throws IOException,
    EOFException {

        ByteBuffer lenBuff = ByteBuffer.allocate(2);
        while (lenBuff.hasRemaining()){
            int b = stream.read();
            if (b == -1) {
                return null;
            }
            lenBuff.put((byte) b);
        }

        //interpret the first two byte as the message length
        int msgLen = getHeaderLength(lenBuff.array());

        ByteBuffer msgBuff = ByteBuffer.allocate(2 + msgLen);
        msgBuff.put(lenBuff.array(), 0, 2);
        while (msgBuff.hasRemaining()){
            int b = stream.read();
            if (b == -1) {
                return null;
            }
            msgBuff.put((byte) b);
        }

        return msgBuff.array();
    }

    private int getHeaderLength(byte[] bytes){
        int frameLength = -1;
        int first = Integer.parseInt(Hex.encodeHexString(new byte[]{bytes[0]}), 16);
        int second = Integer.parseInt(Hex.encodeHexString(new byte[]{bytes[1]}), 16);
        first = first << 8; // first * 256
        frameLength = first + second;
        return frameLength;
    }

    @Override
    public byte[] onDecodeResponseFrame(byte[] requestFrame, InputStream stream)
            throws IOException {
        return onDecodeRequestFrame(stream);
    }

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
