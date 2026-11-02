package core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;

public class AppLog {
    private final Path file;
    private final boolean debug;
    public AppLog(Path file, boolean debug) {
        this.file = file;
        this.debug = debug;
    }
    public void info(String message) {
        write("INFO", message);
    }

    public void error(String message) {
        write("ERROR", message);
    }

    public void debug(String message) {
        if (debug) write("DEBUG", message);
    }
    private void write(String level, String message) {
        try {
            Files.createDirectories(file.getParent());
            String line = new Date() + " [" + level + "] " + message;
            ArrayList<String> lines = new ArrayList<String>();
            lines.add(line);
            Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
}
