
public interface DownloadStrategy {
    VideoInfo fetchMetadata(String url) throws Exception;
    public void startDownload(String url, String savePath, String format, Observer observer);
}