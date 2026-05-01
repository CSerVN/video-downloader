import java.util.Scanner;

public class Program {
	public static void main(String[] args) {
		System.out.println("Download essential tools...");
		DependencyManager.checkAndDownloadDependencies();

		DownloadManager manager = new DownloadManager();
		manager.setStrategy(new NeccessaryToolsAdapter());

		LocalHttpServer server = new LocalHttpServer(manager);
		server.start();

		System.out.println("\n=============================================");
		System.out.println("   VIDEO DOWNLOADER -- version 1.0.0");
		System.out.println("=============================================");
		System.out.println("1. Passive mode: Following Clipboard");
		System.out.println("2. Active mode: Auto catching");
		System.out.print("Choose mode (1/2): ");

		try (Scanner scanner = new Scanner(System.in)) {
			String choice = scanner.nextLine().trim();

			if (choice.equals("2")) {
				System.out.println("---------------------------------------------");
				
				String profilePath = System.getProperty("user.home") + java.io.File.separator + ".VideoDownloaderApp" + java.io.File.separator + "ChromeProfile";
				java.io.File setupFlag = new java.io.File(profilePath, "setup_done.txt");
				
				if (!setupFlag.exists()) {
					BrowserController.autoSetupExtension();
				}

				// MENU CHẾ ĐỘ 2 RÕ RÀNG HƠN
				System.out.println("\n[CATCHING LINK OPTION]");
				System.out.println("👉 METHOD 1: Enter your link here (YouTube, Facebook, popular video/playlist link...).");
				System.out.println("👉 METHOD 2: Leave it blank and press ENTER. The app will open Chrome so you can browse the web and find movies..");
				System.out.print("Enter your link or press Enter: ");
				
				String movieUrl = scanner.nextLine().trim();

				if (movieUrl.isEmpty()) {
					BrowserController.openCaptureBrowser("");
					System.out.println("=> [Video Hunter] It's show time!");
				} else {
					String lowerUrl = movieUrl.toLowerCase();
					
					if (lowerUrl.contains("youtube.com") || lowerUrl.contains("youtu.be") || 
					    lowerUrl.contains("tiktok.com")  || lowerUrl.contains("facebook.com")) {
						manager.processLink(movieUrl);
						
					} else {
						BrowserController.openCaptureBrowser(movieUrl);
					}
				}
				
			} else {
				Thread monitorThread = new Thread(new ClipboardMonitor(manager));
				monitorThread.start();
				System.out.println("Following your clipboard...! (Copy a link to start)");
			}
		}
	}
}