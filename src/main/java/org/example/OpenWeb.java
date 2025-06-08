import java.io.IOException;

public class OpenWeb {

    // Метод для открытия ссылки через команду в зависимости от ОС
    public static boolean openLink(String link) {
        if (link == null || link.isEmpty()) {
            System.err.println("Provided link is empty or null.");
            return false;
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;

            if (os.contains("win")) {
                // Для Windows
                processBuilder = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", link);
            } else if (os.contains("mac")) {
                // Для macOS
                processBuilder = new ProcessBuilder("open", link);
            } else if (os.contains("nix") || os.contains("nux")) {
                // Для Linux
                processBuilder = new ProcessBuilder("xdg-open", link);
            } else {
                System.err.println("Unsupported OS for opening links.");
                return false;
            }

            // Запускаем процесс
            processBuilder.start();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to open link via system command: " + link);
            return false;
        }
    }
}
