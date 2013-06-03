/*
 * Copyright (C) Red Hat, Inc.
 * http://www.redhat.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.cartridgeDemo;

import java.io.IOException;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.*;

public class SimpleConsumer extends Thread implements MessageListener {
	Logger LOG = Logger.getLogger(SimpleConsumer.class);

    private static final String DESTINATION_NAME = "queue/simple";
    
    private String host;
    private String port;
    
    public SimpleConsumer(String host, String port) {
    	this.host = host;
    	this.port = port;
    }

    public void run() {
        Connection connection = null;

        try {
        	ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);
            connection = connectionFactory.createConnection();
            connection.start();

            // Create the session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(DESTINATION_NAME);

            MessageConsumer consumer = session.createConsumer(destination);

            TextMessage message = session.createTextMessage("Message" + System.currentTimeMillis());

            consumeMessagesAndClose(connection, session, consumer, 60);
        } catch (Throwable t) {
            LOG.error("Error receiving message", t);
        } finally {
            // Cleanup code
            // In general, you should always close producers, consumers,
            // sessions, and connections in reverse order of creation.
            // For this simple example, a JMS connection.close will
            // clean up all other resources.
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    LOG.error("Error closing connection", e);
                }
            }
        }
    }
    
    protected void consumeMessagesAndClose(Connection connection, Session session, MessageConsumer consumer, long timeout)
            throws JMSException, IOException {
        System.out.println("[" + this.getName() + "] We will consume messages while they continue to be delivered within: " + timeout
                + " ms, and then we will shutdown");

        Message message;
        while ((message = consumer.receive(timeout)) != null) {
            onMessage(message);
        }

        consumer.close();
        session.close();
        connection.close();
    }

    
    public void onMessage(Message message) {

        LOG.info("!!!! received message " + message);
    }
}
