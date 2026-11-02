import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogData {
    public File file;
    public String collection;
    public int count;
    public long addAverage;
    public long addTotal;
    public long secondAverage;
    public long secondTotal;
    public String secondOperation;
    public ViewSettings settings;

    public static LogData read(File source) throws IOException {
        File file = source.getAbsoluteFile();
        String name = file.getName();
        int underscore = name.indexOf('_');
        int dot = name.lastIndexOf('.');
        if (underscore < 0 || dot < underscore || !name.endsWith(".log")) {
            throw new IllegalArgumentException("Ожидается имя ArrayList_N.log или LinkedList_N.log");
        }
        String collection = name.substring(0, underscore);
        if (!collection.equals("ArrayList") && !collection.equals("LinkedList")) {
            throw new IllegalArgumentException("Неизвестная коллекция: " + collection);
        }
        int count = Integer.parseInt(name.substring(underscore + 1, dot));
        if (count <= 0) throw new IllegalArgumentException("Число элементов должно быть больше нуля");

        LogData log = new LogData();
        log.file = file;
        log.collection = collection;
        log.count = count;
        log.addAverage = -1;
        log.addTotal = -1;
        log.secondAverage = -1;
        log.secondTotal = -1;
        log.secondOperation = "замена";
        int addCount = -1;

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"));
        try {
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("addAverageTimeNs = ")) {
                    log.addAverage = readNumber(line);
                } else if (line.startsWith("addTotalTimeNs = ")) {
                    log.addTotal = readNumber(line);
                } else if (line.startsWith("setAverageTimeNs = ")) {
                    log.secondAverage = readNumber(line);
                } else if (line.startsWith("setTotalTimeNs = ")) {
                    log.secondTotal = readNumber(line);
                } else if (line.startsWith("removeAverageTimeNs = ")) {
                    log.secondAverage = readNumber(line);
                    log.secondOperation = "удаление";
                } else if (line.startsWith("removeTotalTimeNs = ")) {
                    log.secondTotal = readNumber(line);
                    log.secondOperation = "удаление";
                } else if (line.startsWith("addTotalCount = ")) {
                    addCount = (int) readNumber(line);
                }
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }

        if (addCount != count) {
            throw new IllegalArgumentException("Число элементов в логе не совпадает с именем файла");
        }
        if (log.addAverage < 0 || log.addTotal < 0
                || log.secondAverage < 0 || log.secondTotal < 0) {
            throw new IllegalArgumentException("В файле нет всех итоговых показателей времени");
        }
        log.settings = ViewSettings.load(file);
        return log;
    }

    public long getValue(int metric) {
        if (metric == 0) return addAverage;
        if (metric == 1) return addTotal;
        if (metric == 2) return secondAverage;
        return secondTotal;
    }

    private static long readNumber(String line) {
        int equals = line.indexOf('=');
        String number = line.substring(equals + 1).trim();
        return Long.parseLong(number);
    }
}
