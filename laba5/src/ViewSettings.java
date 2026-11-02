import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ViewSettings {
    public double zoom;
    public boolean legend;
    public boolean logX;
    public boolean logY;

    public ViewSettings(double zoom, boolean legend, boolean logX, boolean logY) {
        this.zoom = zoom;
        this.legend = legend;
        this.logX = logX;
        this.logY = logY;
    }

    public static ViewSettings load(File logFile) throws IOException {
        ViewSettings settings = new ViewSettings(1.0, true, false, false);
        File file = getSettingsFile(logFile);
        if (!file.exists()) return settings;

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"));
        try {
            String line = reader.readLine();
            while (line != null) {
                int equals = line.indexOf('=');
                if (equals > 0) {
                    String key = line.substring(0, equals).trim();
                    String value = line.substring(equals + 1).trim();
                    if (key.equals("zoom")) {
                        try {
                            settings.zoom = Double.parseDouble(value);
                        } catch (NumberFormatException exception) {
                            settings.zoom = 1.0;
                        }
                    } else if (key.equals("legend")) {
                        settings.legend = Boolean.parseBoolean(value);
                    } else if (key.equals("logX")) {
                        settings.logX = Boolean.parseBoolean(value);
                    } else if (key.equals("logY")) {
                        settings.logY = Boolean.parseBoolean(value);
                    }
                }
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        if (Double.isNaN(settings.zoom) || Double.isInfinite(settings.zoom)
                || settings.zoom < 1 || settings.zoom > 64) {
            settings.zoom = 1.0;
        }
        return settings;
    }

    public void save(File logFile) throws IOException {
        File file = getSettingsFile(logFile);
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        try {
            writer.write("zoom=" + zoom);
            writer.newLine();
            writer.write("legend=" + legend);
            writer.newLine();
            writer.write("logX=" + logX);
            writer.newLine();
            writer.write("logY=" + logY);
            writer.newLine();
        } finally {
            writer.close();
        }
    }

    private static File getSettingsFile(File logFile) {
        return new File(logFile.getParentFile(), logFile.getName() + ".chart.properties");
    }
}
