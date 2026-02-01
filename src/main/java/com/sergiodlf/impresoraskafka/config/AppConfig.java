package com.sergiodlf.impresoraskafka.config;

public class AppConfig {
	private AppConfig() {
	}

	public static final String BOOTSTRAP = "127.0.0.1:9092";

	public static final String TOPIC_REQUESTS = "print-requests";
	public static final String TOPIC_BW = "print-queue-bw";
	public static final String TOPIC_COLOR = "print-queue-color";

	public static final String GROUP_ARCHIVER = "archiver-group";
	public static final String GROUP_TRANSFORMER = "transformer-group";
	public static final String GROUP_PRINTER_BW = "printer-bw-group";
	public static final String GROUP_PRINTER_COLOR = "printer-color-group";

	// carpetas (relativas al proyecto)
	public static final String DIR_ARCHIVE = "data/archive";
	public static final String DIR_PRINTED_BW = "data/printed/bw";
	public static final String DIR_PRINTED_COLOR = "data/printed/color";

	public static final int PAGE_SIZE = 400;
}
