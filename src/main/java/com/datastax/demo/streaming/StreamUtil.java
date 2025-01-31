package com.datastax.demo.streaming;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class StreamUtil {
	static final String SERVICE_URL_EAST1 = "<add your value>";
	static final String SERVICE_URL_EAST4 = "<add your value>";
	
	static final String PULSAR_AUTH_TOKEN_EAST1 = "<add your value>";
	static final String PULSAR_AUTH_TOKEN_EAST4 = "<add your value>";
	
	static final String PULSAR_TOPIC_FULLPATH = "<add your value>";
	
	static final int PRODUCING_DELAY_IN_MILLISECONDS = 500;
	static final int CONSUMING_DELAY_IN_MILLISECONDS = 300;
	
	static final int CLIENT_WAIT_IN_SECONDS = 60;
	
	static final String PULSAR_SUBSCRIPTION = "<add your value>";

	private static final DateTimeFormatter dtfDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	
	static String getcurrentTime() {
		return dtfDateTime.format(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
	}
}
