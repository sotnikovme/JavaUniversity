import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Benchmark {
    private static final int[] SIZES = {10, 100, 1000, 10000, 100000};

    public static File[] run(File directory) throws IOException {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Не удалось создать папку " + directory);
        }
        File[] files = new File[SIZES.length * 2];
        for (int i = 0; i < SIZES.length; i++) {
            int size = SIZES[i];
            files[i * 2] = measure(directory, "ArrayList", size, new ArrayList<Integer>());
            files[i * 2 + 1] = measure(directory, "LinkedList", size, new LinkedList<Integer>());
        }
        return files;
    }

    private static File measure(File directory, String name, int size, List<Integer> list)
            throws IOException {
        long addTotal = 0;
        long removeTotal = 0;
        for (int i = 0; i < size; i++) {
            long start = System.nanoTime();
            list.add(i);
            addTotal = addTotal + System.nanoTime() - start;
        }
        for (int i = size - 1; i >= 0; i--) {
            long start = System.nanoTime();
            list.remove(i);
            removeTotal = removeTotal + System.nanoTime() - start;
        }

        File file = new File(directory, name + "_" + size + ".log");
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        try {
            writer.write(name);
            writer.newLine();
            write(writer, "addTotalCount", size);
            write(writer, "addTotalTimeNs", addTotal);
            write(writer, "addAverageTimeNs", addTotal / size);
            write(writer, "removeTotalCount", size);
            write(writer, "removeTotalTimeNs", removeTotal);
            write(writer, "removeAverageTimeNs", removeTotal / size);
        } finally {
            writer.close();
        }
        return file;
    }

    private static void write(BufferedWriter writer, String name, long value) throws IOException {
        writer.write(name + " = " + value);
        writer.newLine();
    }
}
