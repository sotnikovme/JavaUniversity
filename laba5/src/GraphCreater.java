
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import javax.swing.*;

public class GraphCreater {
    static final int[] SIZES = {10, 100, 1000, 10000, 100000};
    static double[][][] data = new double[2][4][5];

    public static void main(String[] args) throws Exception {
        String[] cols = {"ArrayList", "LinkedList"};
        for (int c = 0; c < 2; c++) {
            for (int s = 0; s < 5; s++) {
                String fileName = cols[c] + "_" + SIZES[s] + ".log";
                File f = new File("C:/main/MIET/sem3/JavaUniversity/logs", fileName);
                if (!f.exists()) {
                    System.out.println("Не найден: " + fileName);
                    continue;
                }

                long addCnt = 0, addTime = 0, remCnt = 0, remTime = 0;
                for (String line : Files.readAllLines(f.toPath())) {
                    if (line.startsWith("addTotalCount =")) addCnt = Long.parseLong(line.split("=")[1].trim());
                    else if (line.startsWith("addTotalTimeNs =")) addTime = Long.parseLong(line.split("=")[1].trim());
                    else if ((line.startsWith("removeTotalCount =") || line.startsWith("setTotalCount ="))) remCnt = Long.parseLong(line.split("=")[1].trim());
                    else if ((line.startsWith("removeTotalTimeNs =") || line.startsWith("setTotalTimeNs ="))) remTime = Long.parseLong(line.split("=")[1].trim());
                }
                data[c][0][s] = addCnt > 0 ? (double) addTime / addCnt : 0;
                data[c][1][s] = addTime;
                data[c][2][s] = remCnt > 0 ? (double) remTime / remCnt : 0;
                data[c][3][s] = remTime;
            }
        }

        JFrame frame = new JFrame("Графики: ArrayList (синий) vs LinkedList (красный)");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new GridLayout(2, 2));

        String[] titles = {"Среднее время add", "Общее время add", "Среднее время remove", "Общее время remove"};
        for (int m = 0; m < 4; m++) {
            final int metric = m;
            frame.add(new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    int w = getWidth(), h = getHeight(), p = 50;

                    g2.setColor(Color.BLACK);
                    g2.drawString(titles[metric], w / 2 - 60, 20);
                    g2.drawLine(p, p, p, h - p);
                    g2.drawLine(p, h - p, w - p, h - p);

                    double max = 1;
                    for (int c = 0; c < 2; c++)
                        for (double v : data[c][metric]) max = Math.max(max, v);

                    Color[] colors = {Color.BLUE, Color.RED};
                    for (int c = 0; c < 2; c++) {
                        g2.setColor(colors[c]);
                        for (int i = 0; i < 5; i++) {
                            int x1 = p + i * (w - 2 * p) / 4;
                            int y1 = h - p - (int) ((data[c][metric][i] / max) * (h - 2 * p));

                            g2.fillOval(x1 - 4, y1 - 4, 8, 8);
                            g2.drawString(String.format("%.0f", data[c][metric][i]), x1 - 15, y1 - 8);

                            if (i < 4) {
                                int x2 = p + (i + 1) * (w - 2 * p) / 4;
                                int y2 = h - p - (int) ((data[c][metric][i + 1] / max) * (h - 2 * p));
                                g2.drawLine(x1, y1, x2, y2);
                            }
                            if (c == 0) {
                                g2.setColor(Color.BLACK);
                                g2.drawString(String.valueOf(SIZES[i]), x1 - 10, h - p + 20);
                                g2.setColor(colors[c]);
                            }
                        }
                    }
                }
            });
        }
        frame.setVisible(true);
    }
}