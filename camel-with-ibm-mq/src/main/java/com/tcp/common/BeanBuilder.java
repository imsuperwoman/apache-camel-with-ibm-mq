package com.tcp.common;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.jms.JmsMessage;
import org.beanio.BeanReader;
import org.beanio.BeanWriter;
import org.beanio.StreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.asp.proxy.tcp.AscTcpRequest;
import com.tcp.message.MessageError;
import com.tcp.message.MessageHeader;
import com.tcp.message.MessageError.aspErrors;
import com.tcp.message.MessageError.indType;
import com.tcp.message.MessageHeader.mtiType;

public class BeanBuilder {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${ibm.Charset.Name}")
	private String mqCharsetName;
	
	public void jmsRequest(Exchange exchange) {
  	  JmsMessage bytes = exchange.getIn().getBody(JmsMessage.class);
	  log.debug("Received bytes: " + bytes.getJmsMessage());
	}

	public void tcpError(Exchange exchange) {

		StreamFactory factory = StreamFactory.newInstance();
		factory.loadResource("message.xml");
		AscTcpRequest request = (AscTcpRequest) exchange.getIn().getBody();

		Throwable caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,Throwable.class);
		MessageError msgErr = new MessageError();
		MessageHeader msg = new MessageHeader();

		String afterEncode = null;
		try {
			afterEncode = new String(request.getRawByte(), mqCharsetName);

			if (afterEncode.length() < 173) {
				msg.setRpcd("01");
				msg.setMti(mtiType.RESPONSE);
				msg.setMri("N");
				msg.setCount("0");

				msgErr.setErrcd1(aspErrors.MSG_LENGTH_TO_SHORT);
				msgErr.setErrid1(indType.ERROR);
				msgErr.setErrfd1("");
				msgErr.setErrds1(aspErrors.MSG_LENGTH_TO_SHORT.name());

			} else {
				BeanReader in = factory.createReader("message",	new StringReader(afterEncode.substring(0, 173)));
				msg = (MessageHeader) in.read();

				if (caused.getMessage().contains("No Available Connection")) {
					msg.setRpcd("01");
					msg.setMti(mtiType.RESPONSE);
					msg.setMri("N");
					msg.setCount("0");

					msgErr.setErrcd1(aspErrors.ASCCEND_FAIL_TO_CONNECT);
					msgErr.setErrid1(indType.ERROR);
					msgErr.setErrfd1("");
					msgErr.setErrds1(aspErrors.ASCCEND_FAIL_TO_CONNECT.name());
				} else if (caused.getMessage().contains("polling response timeout")) {
					msg.setRpcd("01");
					msg.setMti(mtiType.RESPONSE);
					msg.setMri("N");
					msg.setCount("0");

					msgErr.setErrcd1(aspErrors.ASP_TIMEOUT_ASCCEND);
					msgErr.setErrid1(indType.ERROR);
					msgErr.setErrfd1("");
					msgErr.setErrds1(aspErrors.ASP_TIMEOUT_ASCCEND.name());
				}
				in.close();
			}

		} catch (Exception e1) {
			log.error("UNKNOW FORMAT ", e1);
			msg.setRpcd("01");
			msg.setMti(mtiType.RESPONSE);
			msg.setMri("N");
			msg.setCount("0");

			msgErr.setErrcd1(aspErrors.MSG_UNKNOW_FORMAT);
			msgErr.setErrid1(indType.ERROR);
			msgErr.setErrfd1("");
			msgErr.setErrds1(aspErrors.MSG_UNKNOW_FORMAT.name());

		} finally {

			BeanWriter out = null;
			StringWriter responseString = new StringWriter();
			byte[] response;
			try {
				out = factory.createWriter("message", responseString);
				out.write(MessageHeader.RECORD_NAME, msg);
				out.write(MessageError.RECORD_NAME, msgErr);
				response = responseString.toString().getBytes(mqCharsetName);
				log.info("Response :{}", responseString);
				exchange.getOut().setBody(response);
				out.flush();
			} catch (UnsupportedEncodingException e) {
				log.error("UNKNOW FORMAT {}", e.toString());
			} finally {
				out.flush();
				out.close();
			}
		}
	}

}
