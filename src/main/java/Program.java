
public class Program {
    public static void main(String[] args) {
        DependencyManager.checkAndDownloadDependencies();

        DownloadManager manager = new DownloadManager();
        manager.setStrategy(new NeccessaryToolsAdapter());

        Thread monitorThread = new Thread(new ClipboardMonitor(manager));
        monitorThread.start();
    }
}