package core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

public class AppLog {
    private final Path file;
    private final boolean debug;
    public AppLog(Path file, boolean debug) { this.file = file; this.debug = debug; }
    public void info(String message) { write("INFO", message); }
    public void error(String message) { write("ERROR", message); }
    public void debug(String message) { if (debug) write("DEBUG", message); }
    private void write(String level, String message) {
        try {
            Files.createDirectories(file.getParent());
            String line = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " [" + level + "] " + message;
            Files.write(file, Collections.singleton(line), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) { System.err.println("Не удалось записать лог: " + e.getMessage()); }
    }
}
