package com.korwe.thecore.router.basic;

import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.router.AmqpUriPart;
import org.apache.camel.component.rabbitmq.RabbitMQConstants;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:nithia.govender@korwe.com">Nithia Govender</a>
 */
@Component
public class ServiceToCoreRoute extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(String.format("rabbitmq:%s?connectionFactory=#rabbitConnectionFactory&exchangeType=direct&declare=true&queue=%s&%s",
                           MessageQueue.DIRECT_EXCHANGE,
                           MessageQueue.ServiceToCore.getQueueName(),
                           AmqpUriPart.Options.getValue()))
                .setHeader(RabbitMQConstants.ROUTING_KEY).simple(String.format("%s.${in.header.sessionId}",
                                                                               MessageQueue.CoreToClient.getQueueName()))
                .removeHeader(RabbitMQConstants.EXCHANGE_NAME)
                .recipientList(simple(String.format("rabbitmq:%s?connectionFactory=#rabbitConnectionFactory&exchangeType=topic&queue=%s.${in.header.sessionId}&%s,rabbitmq:%s?connectionFactory=#rabbitConnectionFactory&exchangeType=direct&queue=%s&%s",
                                                    MessageQueue.TOPIC_EXCHANGE,
                                                    MessageQueue.CoreToClient.getQueueName(),
                                                    AmqpUriPart.Options.getValue(),
                                                    MessageQueue.DIRECT_EXCHANGE,
                                                    MessageQueue.Trace.getQueueName(),
                                                    AmqpUriPart.Options.getValue())));
    }

}
