package com.sergiodlf.impresoraskafka.model;

public class PrintRequest {
	public String titulo;
	public String documento;
	public String tipo; // "B/N" o "Color"
	public String sender; // quien lo manda

	public PrintRequest() {
	}
}