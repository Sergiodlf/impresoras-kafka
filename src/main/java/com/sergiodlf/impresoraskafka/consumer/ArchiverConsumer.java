package com.sergiodlf.impresoraskafka.consumer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.sergiodlf.impresoraskafka.config.AppConfig;
import com.sergiodlf.impresoraskafka.model.PrintRequest;
import com.sergiodlf.impresoraskafka.util.FileUtil;
import com.sergiodlf.impresoraskafka.util.JsonUtil;

public class ArchiverConsumer {

	public static void main(String[] args) {
		Properties props = new Properties();
		props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BOOTSTRAP);
		props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, AppConfig.GROUP_ARCHIVER);
		props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
			consumer.subscribe(Collections.singleton(AppConfig.TOPIC_REQUESTS));
			System.out.println("Archiver escuchando " + AppConfig.TOPIC_REQUESTS);

			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
				for (ConsumerRecord<String, String> r : records) {
					PrintRequest req = JsonUtil.fromJson(r.value(), PrintRequest.class);

					String senderDir = AppConfig.DIR_ARCHIVE + "/" + safe(req.sender);
					FileUtil.ensureDir(senderDir);

					String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
					String file = senderDir + "/" + ts + "_" + safe(req.titulo) + ".json";

					// guardamos el JSON ORIGINAL tal cual
					FileUtil.writeTextFile(file, r.value());
					System.out.println("Guardado original en: " + file);
				}
			}
		}
	}

	private static String safe(String s) {
		if (s == null)
			return "unknown";
		return s.replaceAll("[^a-zA-Z0-9._-]", "_");
	}
}
