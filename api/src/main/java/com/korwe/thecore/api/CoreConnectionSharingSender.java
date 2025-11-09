package com.korwe.thecore.api;

import com.korwe.thecore.messages.*;
import com.rabbitmq.client.*;
import org.slf4j.*;

import java.io.*;
import java.util.concurrent.*;

/**
 * @author <a href="mailto:dario.matonicki@korwe.com>Dario Matonicki</a>
 */
public class CoreConnectionSharingSender extends CoreSender {

    private static final Logger LOG = LoggerFactory.getLogger(CoreConnectionSharingSender.class);

    protected CoreConnectionSharingSender(MessageQueue queue, Connection sharedConnection) {
        serializer = new CoreMessageXmlSerializer();
        this.connection = sharedConnection;
        initialise(queue);
    }

    @Override
    public void close() {
        LOG.info("Not closing sender channel");
//        try {
//            channel.close();
//        }
//        catch (TimeoutException | IOException e) {
//            LOG.warn("Error closing channel", e);
//        }
    }
}
