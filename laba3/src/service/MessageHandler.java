package service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MessageHandler implements AutoCloseable {
    private BufferedWriter writer;
    private int errorCount;

    public MessageHandler(Path logFile) {
        try {
            Path parentDirectory = logFile.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }
            writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            error("Cannot open log file: " + logFile, exception);
        }
    }

    public void info(String message) {
        if (writer == null) {
            return;
        }

        try {
            writer.write(message);
            writer.newLine();
        } catch (IOException exception) {
            handleWriteError(exception);
        }
    }

    public void error(String message, Throwable exception) {
        errorCount++;
        String details = exception == null ? "" : " " + exception.getMessage();
        String errorMessage = "ERROR: " + message + details;

        if (writer == null) {
            System.err.println(errorMessage);
            return;
        }

        try {
            writer.write(errorMessage);
            writer.newLine();
        } catch (IOException writeException) {
            handleWriteError(writeException);
        }
    }

    public int getErrorCount() {
        return errorCount;
    }

    @Override
    public void close() {
        if (writer == null) {
            return;
        }

        try {
            writer.close();
        } catch (IOException exception) {
            errorCount++;
            System.err.println("ERROR: Cannot close log file. " + exception.getMessage());
        }
    }

    private void handleWriteError(IOException exception) {
        errorCount++;
        System.err.println("ERROR: Cannot write to log file. " + exception.getMessage());
    }
}
