package com.datastax.demo.streaming;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.ServiceUrlProvider;
import org.apache.pulsar.client.impl.ControlledClusterFailover;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StreamUtil {
	private StreamConfig config = null;
	private ServiceUrlProvider provider = null;
	private PulsarClient client = null;
	private DateTimeFormatter dtfDateTime = null;
    private Map<String, String> providerHeaders = null;
    private String name = null;

    public StreamUtil(String[] args) {
		config = new StreamConfig();
        providerHeaders = new HashMap<>();
        validateArgs(args);
		// Initialize Provider & Client
		getControlledFailoverClient();
		try {
			TimeUnit.SECONDS.sleep(6);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
		dtfDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	}

    public String getcurrentTime() {
        return dtfDateTime.format(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
    }
	public PulsarClient getClient() {
		return client;
	}

	public PulsarClient getControlledFailoverClient() {
		try {
            provider = ControlledClusterFailover.builder()
                    .defaultServiceUrl(config.getDefaultCluster().getServiceUrl())
                    .checkInterval(5, TimeUnit.SECONDS)
                    .urlProvider(config.getProviderUrl())
                    .urlProviderHeader(providerHeaders)
                    .build();
			client = PulsarClient.builder().serviceUrlProvider(provider).build();
			provider.initialize(client);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error initializing Pulsar client: " + e.getMessage());
		}

		return client;
	}

	public StreamConfig getConfig() {
		return config;
	}

	public String getCurrentServiceUrl() {
		int schemeEnd = provider.getServiceUrl().indexOf("://");
		if (schemeEnd == -1) {
			throw new IllegalArgumentException("Invalid URL format: missing '://'");
		}

		// Get the part after the scheme, e.g.,
		// "pulsar-gcp-useast1.streaming.datastax.com:6651"
		String remainder = provider.getServiceUrl().substring(schemeEnd + 3);

		// Remove the port part if it exists (after the colon)
		int colonIndex = remainder.indexOf(':');
		if (colonIndex != -1) {
			remainder = remainder.substring(0, colonIndex);
		}

		// Extract the first part of the domain (up to the first period)
		int dotIndex = remainder.indexOf('.');
		if (dotIndex != -1) {
			return remainder.substring(0, dotIndex);
		}

		// If no period is found, return the whole remainder
		return remainder;
	}

    public void validateArgs(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Mandatory arguments missing!");
        } else if (args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException("Incorrect number of arguments!");
        }

        //CLI args
        name = args[0];
        String region = args[1];
        String group = (args.length == 3 ? args[2] : "");
        System.out.printf("Producer: %s,%nRegion: %s,%nGroup: %s%n", name, region, group);

        //Setting provider headers
        providerHeaders.put("name", name);
        providerHeaders.put("region", region);
        providerHeaders.put("group", group);
    }

    public String getAppName() {
        return name;
    }

}
