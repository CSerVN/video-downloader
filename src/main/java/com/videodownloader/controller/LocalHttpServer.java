package com.videodownloader.controller;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class LocalHttpServer {
	public static final int DEFAULT_PORT = 8765;
	private final DownloadManager manager;
	private HttpServer server;

	public LocalHttpServer(DownloadManager manager) {
		this.manager = manager;
	}

	public void start() {
		try {
			server = HttpServer.create(new InetSocketAddress("localhost", DEFAULT_PORT), 0);
			server.createContext("/capture", new CaptureHandler());
			server.setExecutor(Executors.newCachedThreadPool());
			server.start();
			System.out.println("Open at " + DEFAULT_PORT + "port. Extension had already listened!");
		} catch (IOException e) {
			System.err.println("Couldn't start Local Server: " + e.getMessage());
		}
	}

	private class CaptureHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange ex) throws IOException {
			ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
			ex.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
			ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

			if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
				ex.sendResponseHeaders(204, -1);
				return;
			}

			if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
				String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
				try {
					JsonObject json = JsonParser.parseString(body).getAsJsonObject();
					String streamUrl = json.get("url").getAsString();

					System.out.println("\n[Extension] Captured URL: " + streamUrl);

					String response = "{\"status\":\"ok\"}";
					ex.getResponseHeaders().set("Content-Type", "application/json");
					ex.sendResponseHeaders(200, response.getBytes().length);
					try (OutputStream os = ex.getResponseBody()) {
						os.write(response.getBytes());
					}

					manager.processAutoCapture(streamUrl);

				} catch (Exception e) {
					System.err.println("[Error] Failed to process request: " + e.getMessage());
					String errorResponse = "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
					ex.getResponseHeaders().set("Content-Type", "application/json");
					ex.sendResponseHeaders(400, errorResponse.getBytes().length);
					try (OutputStream os = ex.getResponseBody()) {
						os.write(errorResponse.getBytes());
					}
				}
			} else {
				ex.sendResponseHeaders(405, -1); // Method Not Allowed
			}
		}
	}
}