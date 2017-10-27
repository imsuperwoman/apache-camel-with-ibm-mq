package com.worldline.asp.tcp;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.commons.lang.math.RandomUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ibm.jms.JMSBytesMessage;
import com.ibm.jms.JMSTextMessage;
import com.ibm.mq.jms.MQDestination;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;


public class SSLTest {

    QueueSender qs ;
    QueueSession s ;
    QueueConnection qc;

    @BeforeTest
    public void beforeTest() throws UnknownHostException, IOException, JMSException, Exception {
        System.out.println("javax.net.ssl.keyStore:"+System.getProperty("javax.net.ssl.keyStore"));
        System.out.println("javax.net.ssl.trustStore:"+System.getProperty("javax.net.ssl.trustStore"));

        // Set up the W-MQ QueueConnectionFactory
        MQQueueConnectionFactory qcf = new MQQueueConnectionFactory();

        // Host and port settings have their usual meanings
        qcf.setHostName ("127.0.0.1");
        qcf.setPort (1434);
        qcf.setQueueManager ("TPP01");
        qcf.setChannel ("SSL.SVRCONN");
        qcf.setTransportType (WMQConstants.WMQ_CM_CLIENT);
        qcf.setIntProperty(WMQConstants.WMQ_RECEIVE_CONVERSION, WMQConstants.WMQ_RECEIVE_CONVERSION_CLIENT_MSG);
        
        //qcf.setSSLFipsRequired (false);
       // qcf.setSSLCipherSuite ("TLS_RSA_WITH_AES_256_CBC_SHA");
        
        qc = qcf.createQueueConnection ("MUSR_MQADMIN", "");

        Queue destination = new MQQueue ("TPP.F1I.CCTS.REQ");
        ((MQDestination)destination).setIntProperty(
                WMQConstants.WMQ_MESSAGE_BODY, 
                WMQConstants.WMQ_MESSAGE_BODY_MQ);
        s = qc.createQueueSession (false, Session.AUTO_ACKNOWLEDGE);        
        qs = s.createSender (destination);
        
    }


    @Test(threadPoolSize = 1, invocationCount = 1)
    public void testMQtest() throws Exception {
    	int index = RandomUtils.nextInt(30);
        String source  ="06001234567890123456789703004"+index+"7890112345678901234567890060012345678901234567897030041234561234561234561234561234567890112345678901234567890000000";
        System.out.print ( "ORIG :"+ source +"\n");
      
       
        Charset.forName("CP037").encode(source);

        /*        byte[] bytes = source.getBytes("CP037");
        for (int i = 0; i < bytes.length; i++) {
            System.out.printf("%s %X%n", source.charAt(i), bytes[i]);
        }

      	Message m = s.createTextMessage (source);
        m.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, 37);      
        qs.send (m);*/

        Message m = s.createTextMessage (source);
        BytesMessage byteMessage = s.createBytesMessage();
        byteMessage.writeBytes(Charset.forName("CP037").encode(source).array());
        m.setStringProperty(WMQConstants.JMS_IBM_FORMAT, "MQSTR");
        m.setIntProperty(WMQConstants.JMS_IBM_MSGTYPE, 1);
        qs.send (byteMessage);
    }


}
