package com.jms.ibm.destination;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.springframework.jms.support.destination.DynamicDestinationResolver;

import com.ibm.mq.jms.MQDestination;
import com.ibm.msg.client.wmq.WMQConstants;

public class CustomDynamicDestinationResolver extends
		DynamicDestinationResolver {

	@Override
	protected Queue resolveQueue(Session session, String queueName) throws JMSException {
		Queue destination = super.resolveQueue(session, queueName);
		((MQDestination)destination).setIntProperty(WMQConstants.WMQ_MESSAGE_BODY,WMQConstants.WMQ_MESSAGE_BODY_MQ);		
		return destination;
	}
	
	@Override
	protected Topic resolveTopic(Session session, String topicName)	throws JMSException {
		Topic destination = super.resolveTopic(session, topicName);
		((MQDestination)destination).setIntProperty(WMQConstants.WMQ_MESSAGE_BODY,WMQConstants.WMQ_MESSAGE_BODY_MQ);		
		return destination;
	}
}
