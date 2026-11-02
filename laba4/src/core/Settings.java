package core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Settings {
    public final String login;
    public final String password;
    public final Group group;
    public final boolean debug;
    public final boolean autotests;

    private Settings(Properties p) {
        login = required(p, "login");
        password = required(p, "password");
        group = Group.valueOf(required(p, "group").toUpperCase());
        debug = flag(p, "debug");
        autotests = flag(p, "autotests");
    }

    public static Settings load(Path file) throws IOException {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        }
        try {
            return new Settings(p);
        } catch (IllegalArgumentException e) {
            throw new IOException("Settings error: " + e.getMessage(), e);
        }
    }

    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("missing " + key);
        return value.trim();
    }

    private static boolean flag(Properties p, String key) {
        String value = required(p, key);
        if (!value.equals("true") && !value.equals("false")) throw new IllegalArgumentException(key + " must be true or false");
        return Boolean.parseBoolean(value);
    }
}
