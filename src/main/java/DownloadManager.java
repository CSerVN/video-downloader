import java.io.File;
import java.util.Scanner;

public class DownloadManager implements Observer {
	private DownloadStrategy strategy;

	public void setStrategy(DownloadStrategy strategy) {
		this.strategy = strategy;
	}

	public void processLink(String url) {
		try {
			System.out.println("\n[Analyzing link] " + url);
			VideoInfo info = strategy.fetchMetadata(url);
			System.out.println("Found video/playlist: " + info.title);

			String savePath = FolderSelector.chooseSaveDirectory();

			System.out.println("\n--- CHOOSE DOWNLOADED FORMATTER ---");
			System.out.println("1. MP4 - Popular video formatter (Default)");
			System.out.println("2. MP3 - Just audio");
			System.out.println("3. WebM/MKV - Original quality");
			System.out.print("Enter your choice (1, 2, 3, Enter = Default): ");

			Scanner sc = new Scanner(System.in);
			String choice = sc.nextLine().trim();
			String format;

			if (choice.equals("2")) {
				format = "mp3";
			} else if (choice.equals("3")) {
				format = "webm/mkv";
			} else {
				format = "mp4";
			}

			strategy.startDownload(url, savePath, format, this);

		} catch (Exception e) {
			System.err.println("Analytics error! Error: " + e.getMessage());
		}
	}

	@Override
	public void onProgressUpdate(String videoId, double percent, String speed) {
		int width = 50; // Length of process bar
		int progress = (int) (percent / 100 * width);

		StringBuilder bar = new StringBuilder("[");
		for (int i = 0; i < width; i++) {
			if (i < progress)
				bar.append("=");
			else if (i == progress)
				bar.append(">");
			else
				bar.append(" ");
		}

		String output = String.format("\r%s] %.1f%% | speed: %s", bar.toString(), percent, speed);
		System.out.print(output);
	}

	@Override
	public void onComplete(String videoId, String savedPath) {
		System.out.println("\n>> Video download completed successfully!");
		System.out.println(">> Saved at: " + savedPath);
		System.out.println("--------------------------------------------------");
	}

	@Override
	public void onError(String videoId, String errorMessage) {
		System.err.println("\n>> Error: failed to download. Details: " + errorMessage);
	}
	public void processAutoCapture(String url) {
        try {
            System.out.println("\n[Auto-Capture] Analyzing: " + url);
            
            // Auto save at Downloads
            String autoSavePath = System.getProperty("user.home") + File.separator + "Downloads";
            
            // Auto download MP4
            String format = "mp4"; 
            
            strategy.startDownload(url, autoSavePath, format, this);
            
        } catch (Exception e) {
            System.err.println("Lỗi Auto-Capture: " + e.getMessage());
        }
    }
}
