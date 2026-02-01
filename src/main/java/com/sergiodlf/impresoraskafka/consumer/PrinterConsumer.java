package com.sergiodlf.impresoraskafka.consumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.sergiodlf.impresoraskafka.config.AppConfig;
import com.sergiodlf.impresoraskafka.model.PrintPage;
import com.sergiodlf.impresoraskafka.util.FileUtil;
import com.sergiodlf.impresoraskafka.util.JsonUtil;

public class PrinterConsumer {

	public static void main(String[] args) {
		// args[0] = tipo ("bw" o "color")
		// args[1] = printerId ("1","2","3"...)
		if (args.length < 2) {
			System.out.println("Uso: PrinterConsumer <bw|color> <printerId>");
			return;
		}

		String tipo = args[0].toLowerCase();
		String printerId = args[1];

		String topic;
		String group;
		String outDirBase;

		if (tipo.equals("color")) {
			topic = AppConfig.TOPIC_COLOR;
			group = AppConfig.GROUP_PRINTER_COLOR;
			outDirBase = AppConfig.DIR_PRINTED_COLOR;
		} else {
			topic = AppConfig.TOPIC_BW;
			group = AppConfig.GROUP_PRINTER_BW;
			outDirBase = AppConfig.DIR_PRINTED_BW;
		}

		String printerDir = outDirBase + "/printer-" + printerId;
		FileUtil.ensureDir(printerDir);

		Properties props = new Properties();
		props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BOOTSTRAP);
		props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, group);
		props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
			consumer.subscribe(Collections.singleton(topic));
			System.out.println("Printer " + tipo + "-" + printerId + " escuchando " + topic + " (group=" + group + ")");

			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
				for (ConsumerRecord<String, String> r : records) {
					PrintPage page = JsonUtil.fromJson(r.value(), PrintPage.class);

					// Simulación de impresión: guardo cada página como archivo .json
					String fileName = safe(page.titulo) + "_p" + page.pageNumber + "-of-" + page.totalPages + ".json";
					String path = printerDir + "/" + fileName;

					FileUtil.writeTextFile(path, r.value());
					System.out.println("IMPRESO en " + printerDir + ": " + fileName + " (sender=" + page.sender + ")");
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
