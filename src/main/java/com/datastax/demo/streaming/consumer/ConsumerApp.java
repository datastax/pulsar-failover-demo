package com.datastax.demo.streaming.consumer;

import com.datastax.demo.streaming.StreamUtil;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.SubscriptionType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ConsumerApp {

    /**
     * This application initializes a new pulsar client using {@link org.apache.pulsar.client.impl.ControlledClusterFailover ControlledClusterFailover} and creates a Consumer application which is then used to consume messages from the pulsar topic.
     *
     * @param args Expected CLI/Runtime arguments. See below for definition:
     *             <ol>
     *                 <li> @Mandatory consumerName {args[0]} - A name for the consumer to be started</li>
     *                 <li> @Mandatory region {args[1]} - Region where this application is hosted/deployed</li>
     *                 <li> @Optional group {args[2]} - Name of the group this app should belong to. This will help in determining the cluster where the pulsar client will connect.</li>
     *             </ol>
     * @throws IOException          Exception thrown from the app
     * @throws InterruptedException Exception thrown from the app
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        StreamUtil util = new StreamUtil(args);
        PulsarClient client = util.getClient();

        // Create consumer on a topic with a subscription
        Consumer<?> consumer = client.newConsumer()
                .consumerName(util.getAppName())
                .topic(util.getConfig().getTopicFullPath())
                .subscriptionName(util.getConfig().getSubscription()).replicateSubscriptionState(true)
                .subscriptionType(SubscriptionType.Exclusive)
                .subscribe();
        System.out.println("Consumer started using Pulsar service at " + util.getCurrentServiceUrl());

		boolean receivedMsg;
		do { // Process message blacklog & wait CLIENT_WAIT_IN_SECONDS secs for new message
			receivedMsg = false;
			Message<?> msg = consumer.receive(util.getConfig().getConsumerWaitSeconds(), TimeUnit.SECONDS);

			if (msg != null) {
				receivedMsg = true;
				System.out.printf("Consuming: %s at %s on %s\n", new String(msg.getData()), util.getcurrentTime(),
						util.getCurrentServiceUrl());

				consumer.acknowledge(msg); // Remove consumed msg from backlog
				TimeUnit.MILLISECONDS.sleep(util.getConfig().getConsumerDelayMillis());
			}
		} while (receivedMsg);
		System.out.printf("Consumer: No new messages to process in the last %s seconds!\n",
				util.getConfig().getConsumerWaitSeconds());

        // Close Consumer & client
        System.out.println("Consumer Pulsar service at " + util.getCurrentServiceUrl() + " shutting down!");
        consumer.close();
        client.close();
    }
}