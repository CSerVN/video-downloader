import javax.swing.JOptionPane;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BrowserController {

	private static final String PROFILE_PATH = System.getProperty("user.home") + File.separator + ".VideoDownloaderApp"
			+ File.separator + "ChromeProfile";

	private static ProcessBuilder getChromiumProcess(String... extraArgs) {
		String os = System.getProperty("os.name").toLowerCase();
		List<String> command = new ArrayList<>();

		if (os.contains("win")) {
			command.add("cmd");
			command.add("/c");
			command.add("start");
			command.add("\"\"");
			command.add("chrome");
		} else if (os.contains("mac")) {
			command.add("open");
			command.add("-a");
			command.add("Google Chrome");
			command.add("--args");
		} else {
			command.add("google-chrome");
		}

		command.addAll(Arrays.asList(extraArgs));
		return new ProcessBuilder(command);
	}

	public static void autoSetupExtension() {
		String extPath = ExtensionManager.getExtensionPath();

		try {
			System.out.println("\n[System] Preparing for first setup...");
			StringSelection stringSelection = new StringSelection(extPath);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
			String preMessage = "This is the first time you use catching link address automatically!\n\n"
					+ "When the browser pop up. You are compulsory to do:\n"
					+ "👉 Look at the TOP RIGHT CORNER on your browser.\n"
					+ "👉 Switching the Developer mode toggle on.\n\n" + "Press OK to start open the browser.";
			JOptionPane.showMessageDialog(null, preMessage, "Step 1, setting up", JOptionPane.WARNING_MESSAGE);

			ProcessBuilder pb = getChromiumProcess("--user-data-dir=" + PROFILE_PATH, "--load-extension=" + extPath,
					"chrome://extensions/");
			pb.start();

			String postMessage = "After you have successfully enabled 'Developer mode',\n"
					+ "Please check if the 'Video Hunter' extension has appeared.\n\n"
					+ "If you see it, click OK here to start downloading the video/playlist!";
			JOptionPane.showMessageDialog(null, postMessage, "Thiết lập bước 2", JOptionPane.INFORMATION_MESSAGE);

			new File(PROFILE_PATH, "setup_done.txt").createNewFile();

			System.out.println("Set up completed! Ready status.");

		} catch (Exception e) {
			System.err.println("Error setup: " + e.getMessage());
		}
	}

	public static void openCaptureBrowser(String url) {
		String extPath = ExtensionManager.getExtensionPath();
		try {
			String targetUrl = url.isEmpty() ? "https://www.google.com" : url;
			System.out.println("Deploying Auto-Capture Browser to: " + targetUrl);
			
			ProcessBuilder pb = getChromiumProcess(
					"--user-data-dir=" + PROFILE_PATH, 
					"--load-extension=" + extPath,
					"--no-first-run",             
					"--no-default-browser-check", 
					targetUrl                           
			);
			pb.start();
		} catch (Exception e) {
			System.err.println("Error: Couldn't call to browser " + e.getMessage());
		}
	}
}