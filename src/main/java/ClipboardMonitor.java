import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;

public class ClipboardMonitor implements Runnable {
    private final DownloadManager manager;

    public ClipboardMonitor(DownloadManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String lastContent = "";
        System.out.println("Following your Clipboard. Let copy any video/playlist link address...");

        while (true) {
            try {
                if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                    String data = (String) clipboard.getData(DataFlavor.stringFlavor);
                    
                    if (data != null && !data.equals(lastContent) && (data.startsWith("http") || data.startsWith("https"))) {
                        lastContent = data;
                        manager.processLink(data);
                    }
                }
                Thread.sleep(1500);
            } catch (Exception e) {
            }
        }
    }
}
