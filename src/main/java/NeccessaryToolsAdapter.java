import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

class NeccessaryToolsAdapter implements DownloadStrategy {
    private final Gson gson = new Gson();
    
    private String getToolPath(String toolType) {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        String dir = System.getProperty("user.dir");
        String fileName = "";

        if (toolType.equals("ytdlp")) {
            if (os.contains("win")) fileName = "yt-dlp.exe";
            else if (os.contains("mac")) fileName = "yt-dlp_macos";
            else {
                if (arch.contains("aarch64") || arch.contains("arm")) fileName = "yt-dlp_linux_aarch64";
                else fileName = "yt-dlp_linux";
            }
        } else if (toolType.equals("ffmpeg")) {
            if (os.contains("win")) fileName = "ffmpeg.exe";
            else if (os.contains("mac")) {
                if (arch.contains("aarch64") || arch.contains("arm")) fileName = "ffmpeg-darwin-arm64";
                else fileName = "ffmpeg-darwin-x64";
            } else {
                if (arch.contains("aarch64") || arch.contains("arm")) fileName = "ffmpeg-linux-arm64";
                else fileName = "ffmpeg-linux-x64";
            }
        }
        return dir + File.separator + fileName;
    }

    @Override
    public VideoInfo fetchMetadata(String url) throws Exception {
        String ytDlpCommand = getToolPath("ytdlp");
        ProcessBuilder pb = new ProcessBuilder(ytDlpCommand, "-j", url);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String jsonOutput = reader.readLine();

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorMsg = new StringBuilder();
        String line;
        while ((line = errorReader.readLine()) != null) {
            errorMsg.append(line).append("\n");
        }

        if (jsonOutput != null && !jsonOutput.trim().isEmpty()) {
            return gson.fromJson(jsonOutput, VideoInfo.class);
        } else {
            throw new RuntimeException("Analytics link error: \n" + errorMsg.toString());
        }
    }

    @Override
    public void startDownload(String url, String savePath, String format, Observer o) {
        try {
            System.out.println("Processing format: " + format.toUpperCase() + "...");
            
            String outputTemplate = savePath + File.separator + "%(title)s.%(ext)s";
            
            java.util.List<String> commandList = new java.util.ArrayList<>();
            commandList.add(getToolPath("ytdlp"));
            commandList.add("--ffmpeg-location");
            commandList.add(getToolPath("ffmpeg"));
            commandList.add("-N");
            commandList.add("16");
            commandList.add("-o");
            commandList.add(outputTemplate);
            commandList.add(url);

            if (format.equalsIgnoreCase("mp4")) {
                commandList.add(1, "bestvideo+bestaudio/best");
                commandList.add(1, "-f");
                commandList.add("--merge-output-format");
                commandList.add("mp4");
            } 
            else if (format.equalsIgnoreCase("mp3")) {
                commandList.add(1, "bestaudio/best");
                commandList.add(1, "-f");
                commandList.add("-x");
                commandList.add("--audio-format");
                commandList.add("mp3");
            }
            else {
                commandList.add(1, "bestvideo+bestaudio/best");
                commandList.add(1, "-f");
            }

            ProcessBuilder pb = new ProcessBuilder(commandList);
            pb.inheritIO(); 
            Process process = pb.start();
            process.waitFor();
            
            System.out.println("Download completed! Save at: " + savePath);
            
            o.onComplete(url, savePath);
            
        } catch (Exception e) {
            System.err.println("Error while downloading: " + e.getMessage());
            o.onError(url, e.getMessage());
        }
    }
}