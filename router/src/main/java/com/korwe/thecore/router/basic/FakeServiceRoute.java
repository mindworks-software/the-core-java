package com.korwe.thecore.router.basic;

import com.korwe.thecore.api.MessageQueue;
import com.korwe.thecore.router.AmqpUriPart;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
 */
@Component
public class FakeServiceRoute extends SpringRouteBuilder {

    @Value("${amqp.host}")
    private String hostname;

    @Value("${amqp.port}")
    private String port;

    private static final String SERVICE_NAME = "SyndicationService";

    @Override
    public void configure() throws Exception {
        from(String.format(
                "rabbitmq://%s:%s/%s?connectionFactory=#rabbitConnectionFactory&exchangeType=direct&declare=false&queue=%s&%s",
                hostname, port,
                MessageQueue.DIRECT_EXCHANGE,
                MessageQueue.ClientToCore.getQueueName(),
                AmqpUriPart.Options.getValue()))
                .to(String.format(
                        "rabbitmq://%s:%s/%s?connectionFactory=#rabbitConnectionFactory&exchangeType=direct&declare=false&queue=%s&%s",
                        hostname, port,
                        MessageQueue.DIRECT_EXCHANGE,
                        MessageQueue.ServiceToCore.getQueueName(),
                        AmqpUriPart.Options.getValue()));
    }

}
