package com.worldline.asp.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.base.Throwables;
import com.google.common.primitives.Bytes;

public class AsccendTCPMsg {

    private DateFormat asccendDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
    private final Charset CHARSET = Charset.forName("Cp1047");
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;

    @BeforeTest
    public void beforeTest() throws UnknownHostException, IOException {

        // Make connection and initialize streams
        socket = new Socket("127.0.0.1", 1010);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(),CHARSET));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    private byte[] encodeMessage(String message) {
        try {
            int msgLenth = message.length();
            byte[] lenBytes = new byte[] { (byte) (msgLenth >> 8),
                    (byte) msgLenth };
            return Bytes.concat(lenBytes, message.getBytes("Cp1047"));
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
    }

    public String sendMessage(Socket requestSocket, String message)
            throws UnknownHostException, IOException {
        OutputStream toServer = requestSocket.getOutputStream();
        toServer.write(encodeMessage(message));

        return in.readLine();
    }

    @Test(threadPoolSize = 1, invocationCount = 1)
    public void testASPINST() throws Exception {
        String source  ="06001234567890000000000ASPINSTRASPNODE1                                                                  AS00000000000                                                                                                                     START                       ASV1";
        System.out.println("message:" + source);
        final long sendMessageStartTime = System.currentTimeMillis();
        System.out.println(sendMessage(socket, source));
        final long sendMessageStopTime = System.currentTimeMillis();
        System.out.println("sendMessageTime:"
                + (sendMessageStopTime - sendMessageStartTime));
    }

    @Test(threadPoolSize = 1, invocationCount = 1)
    public void testPaymentInquiry() throws Exception {
        String internalID = UUID.randomUUID().toString().substring(0, 12);
        String asccendDateTime = asccendDateFormat.format(new Date());

        /**
         * responseCode must be blank due to this is ASCCEND request message
         * should not have any response code returned.
         */
        String responseCode = StringUtils.leftPad("", 8, "");
        String source = "0600123456789012703004        "
                + asccendDateTime
                + "3200000538               "
                + responseCode
                + "0121                                                            160819  10573170SCADCOUS            "
                + internalID
                + "                      1608191110541410    WDHM411720355000000102110410005579490000000001000000000                   FD                         "
                + "OASV1";
        // toServer.write(tNettyClient.sendMessage(source));
        System.out.println("message:" + source);
        final long sendMessageStartTime = System.currentTimeMillis();
        System.out.println(sendMessage(socket, source));
        final long sendMessageStopTime = System.currentTimeMillis();
        System.out.println("sendMessageTime:"+ (sendMessageStopTime - sendMessageStartTime));

    }

}
