package com.sergiodlf.impresoraskafka.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static <T> T fromJson(String json, Class<T> cls) {
		try {
			return MAPPER.readValue(json, cls);
		} catch (Exception e) {
			throw new RuntimeException("JSON parse error: " + e.getMessage(), e);
		}
	}

	public static String toJson(Object obj) {
		try {
			return MAPPER.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException("JSON write error: " + e.getMessage(), e);
		}
	}
}