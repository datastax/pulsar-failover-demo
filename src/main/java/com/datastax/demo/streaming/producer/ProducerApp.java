package com.datastax.demo.streaming.producer;

import com.datastax.demo.streaming.StreamUtil;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ProducerApp {

    /**
     * This application initializes a new pulsar client using {@link org.apache.pulsar.client.impl.ControlledClusterFailover ControlledClusterFailover} and creates a Producer application which is then used to produce messages to the pulsar topic.
     *
     * @param args Expected CLI/Runtime arguments. See below for definition:
     *             <ol>
     *                 <li> @Mandatory producerName {args[0]} - A name for the producer to be started</li>
     *                 <li> @Mandatory region {args[1]} - Region where this application is hosted/deployed</li>
     *                 <li> @Optional group {args[2]} - Name of the group this app should belong to. This will help in determining the cluster where the pulsar client will connect.</li>
     *             </ol>
     * @throws IOException Exception thrown from the app
     */
	public static void main(String[] args) throws IOException {
        StreamUtil util = new StreamUtil(args);
		PulsarClient client = util.getClient();
        Producer<byte[]> producer = client.newProducer()
                .producerName(util.getAppName())
                .topic(util.getConfig().getTopicFullPath())
                //.sendTimeout(0, TimeUnit.SECONDS) //This is a required setting when deduplication is enabled
                .create();
		System.out.println("Producer started using Pulsar service at " + util.getCurrentServiceUrl());

		IntStream.iterate(1, n -> n + 1).forEach(i -> {
			try {
				String msg = "Message " + i + " at " + util.getcurrentTime() + " from " + util.getCurrentServiceUrl();
				producer.send(msg.getBytes());
				System.out.printf("Producing: %s \n", msg);
				TimeUnit.MILLISECONDS.sleep(util.getConfig().getProducerDelayMillis());
			} catch (PulsarClientException | InterruptedException e) {
				e.printStackTrace();
			}
		});

		// Close Producer & client
        System.out.println("Producer Pulsar service at " + util.getCurrentServiceUrl() + " shutting down!");
		producer.close();
		client.close();
	}

}