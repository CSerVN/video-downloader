package com.videohunter.controller;
import java.io.File;
import java.io.FileWriter;

public class ExtensionManager {

	public static String getExtensionPath() {
		String extDir = System.getProperty("user.home") + File.separator + ".VideoDownloaderApp" + File.separator
				+ "Extension";
		File dir = new File(extDir);

		if (!dir.exists()) {
			dir.mkdirs();
			System.out.println("Create secret extension...");

			try {
				// 1. Create manifest.json
				FileWriter manifestWriter = new FileWriter(new File(extDir, "manifest.json"));
				manifestWriter.write("{\n" 
						+ "  \"manifest_version\": 3,\n"
						+ "  \"name\": \"Video Hunter (Java Bridge)\",\n" 
						+ "  \"version\": \"1.0\",\n"
						+ "  \"permissions\": [\"webRequest\"],\n" 
						+ "  \"host_permissions\": [\"<all_urls>\"],\n"
						+ "  \"background\": { \"service_worker\": \"background.js\" }\n" 
						+ "}");
				manifestWriter.close();

				// 2. Create background.js
				FileWriter bgWriter = new FileWriter(new File(extDir, "background.js"));
				bgWriter.write("chrome.webRequest.onBeforeRequest.addListener(\n" 
				        + "    function(details) {\n"
				        + "        let url = details.url.toLowerCase();\n"
				        + "        // Filter video/playlist m3u8/mpd\n"
				        + "        if ((url.includes('.m3u8') || url.includes('.mpd')) && !url.includes('audio') && !url.includes('preview')) {\n"
				        + "            console.log('🎯 [Extension] Link catched: ', url);\n"
				        + "            fetch('http://localhost:8765/capture', {\n" 
				        + "                method: 'POST',\n"
				        + "                headers: { 'Content-Type': 'application/json' },\n"
				        + "                body: JSON.stringify({ url: details.url })\n" 
				        + "            })\n"
				        + "            .then(response => { if (response.ok) chrome.tabs.remove(details.tabId); })\n"
				        + "            .catch(e => console.log('Server is offline'));\n" 
				        + "        }\n" 
				        + "    },\n"
				        + "    // Catch domain and come into IFrame's XHR/Media threads\n"
				        + "    { urls: ['<all_urls>'], types: ['xmlhttprequest', 'media'] }\n" 
				        + ");");
				bgWriter.close();

			} catch (Exception e) {
				System.err.println("Error: Couldn't create extension! " + e.getMessage());
			}
		}
		return extDir;
	}
}