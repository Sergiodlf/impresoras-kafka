package com.sergiodlf.impresoraskafka.consumer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.sergiodlf.impresoraskafka.config.AppConfig;
import com.sergiodlf.impresoraskafka.model.PrintPage;
import com.sergiodlf.impresoraskafka.model.PrintRequest;
import com.sergiodlf.impresoraskafka.util.JsonUtil;

public class TransformerConsumer {

	public static void main(String[] args) {
		// Consumer (lee originales)
		Properties cprops = new Properties();
		cprops.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BOOTSTRAP);
		cprops.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		cprops.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		cprops.setProperty(ConsumerConfig.GROUP_ID_CONFIG, AppConfig.GROUP_TRANSFORMER);
		cprops.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		// Producer (envía páginas transformadas)
		Properties pprops = new Properties();
		pprops.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BOOTSTRAP);
		pprops.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		pprops.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(cprops);
				KafkaProducer<String, String> producer = new KafkaProducer<>(pprops)) {

			consumer.subscribe(Collections.singleton(AppConfig.TOPIC_REQUESTS));
			System.out.println("Transformer escuchando " + AppConfig.TOPIC_REQUESTS);

			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
				for (ConsumerRecord<String, String> r : records) {
					PrintRequest req = JsonUtil.fromJson(r.value(), PrintRequest.class);

					List<String> pages = split(req.documento, AppConfig.PAGE_SIZE);
					int total = pages.size();

					String outTopic = isColor(req.tipo) ? AppConfig.TOPIC_COLOR : AppConfig.TOPIC_BW;

					for (int i = 0; i < total; i++) {
						PrintPage page = new PrintPage();
						page.titulo = req.titulo;
						page.tipo = req.tipo;
						page.pageNumber = i + 1;
						page.totalPages = total;
						page.contenido = pages.get(i);
						page.sender = req.sender; // NO para imprimir, sí para routing/traza

						String pageJson = JsonUtil.toJson(page);

						// KEY = sender => decide partición => decide "impresora"
						producer.send(new ProducerRecord<>(outTopic, req.sender, pageJson));
					}
					producer.flush();

					System.out.println("Transformado: '" + req.titulo + "' => " + total + " páginas a " + outTopic);
				}
			}
		}
	}

	private static boolean isColor(String tipo) {
		return tipo != null && tipo.toLowerCase().contains("color");
	}

	private static List<String> split(String text, int max) {
		List<String> out = new ArrayList<>();
		if (text == null)
			text = "";
		for (int i = 0; i < text.length(); i += max) {
			out.add(text.substring(i, Math.min(text.length(), i + max)));
		}
		if (out.isEmpty())
			out.add("");
		return out;
	}
}
