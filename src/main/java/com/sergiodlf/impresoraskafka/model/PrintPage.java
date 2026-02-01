package com.sergiodlf.impresoraskafka.model;

public class PrintPage {
	  public String titulo;
	  public String tipo;        // "B/N" o "Color"
	  public int pageNumber;     // 1..N
	  public int totalPages;     // N
	  public String contenido;   // max 400 chars
	  public String sender;      // NO se imprime, pero nos sirve para routing/trace

	  public PrintPage() {}
	}