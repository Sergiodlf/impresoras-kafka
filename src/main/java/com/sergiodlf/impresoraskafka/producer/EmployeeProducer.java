package com.sergiodlf.impresoraskafka.producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import com.sergiodlf.impresoraskafka.config.AppConfig;

public class EmployeeProducer {

	public static void main(String[] args) {
		// Uso:
		// args[0] = sender
		// args[1] = tipo ("B/N" o "Color")
		// args[2] = titulo
		// args[3] = documento (puede tener espacios -> en Eclipse ponlo entre comillas)
		if (args.length < 4) {
			System.out.println("Uso: EmployeeProducer <sender> <tipo> <titulo> <documento>");
			return;
		}

		String sender = args[0];
		String tipo = args[1];
		String titulo = args[2];
		String documento = args[3];

		String json = "{" + "\"titulo\":\"" + escape(titulo) + "\"," + "\"documento\":\"" + escape(documento) + "\","
				+ "\"tipo\":\"" + escape(tipo) + "\"," + "\"sender\":\"" + escape(sender) + "\"" + "}";

		Properties props = new Properties();
		props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BOOTSTRAP);
		props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
			// key = sender
			ProducerRecord<String, String> record = new ProducerRecord<>(AppConfig.TOPIC_REQUESTS, sender, json);
			producer.send(record);
			producer.flush();
			System.out.println("Enviado a " + AppConfig.TOPIC_REQUESTS + ": " + json);
		}
	}

	private static String escape(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
