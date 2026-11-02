import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JComponent;

public class ChartPanel extends JComponent {
    private ArrayList<LogData> data;
    private int metric;
    private double zoom;
    private boolean showLegend;
    private boolean logX;
    private boolean logY;
    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;
    private int left;
    private int top;
    private int chartWidth;
    private int chartHeight;

    public ChartPanel(ArrayList<LogData> data, int metric, double zoom,
                      boolean showLegend, boolean logX, boolean logY) {
        this.data = data;
        this.metric = metric;
        this.zoom = zoom;
        this.showLegend = showLegend;
        this.logX = logX;
        this.logY = logY;
        setToolTipText("");
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        left = 90;
        top = 55;
        chartWidth = getWidth() - 140;
        chartHeight = getHeight() - 125;
        if (chartWidth < 1 || chartHeight < 1) return;
        if (data.isEmpty()) {
            graphics.drawString("Откройте логи или проведите измерение.", left, top + 30);
            return;
        }
        calculateRange();
        drawGrid(graphics);
        drawCollection(graphics, "ArrayList", Color.BLUE);
        drawCollection(graphics, "LinkedList", Color.RED);
        if (showLegend) drawLegend(graphics);
        graphics.setColor(Color.BLACK);
        graphics.drawString("Количество элементов", left + chartWidth / 2 - 55, getHeight() - 15);
        graphics.drawString("Время, нс", 8, 25);
    }

    private void calculateRange() {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (int i = 0; i < data.size(); i++) {
            LogData log = data.get(i);
            double x = changeScale(log.count, logX);
            double y = changeScale(log.getValue(metric), logY);
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }
        double xPadding = (maxX - minX) * 0.12;
        double yPadding = (maxY - minY) * 0.15;
        if (xPadding < 0.15) xPadding = 0.15;
        if (yPadding < 0.15) yPadding = 0.15;
        if (!logX && xPadding < 1) xPadding = 1;
        if (!logY && yPadding < 1) yPadding = 1;
        xMin = minX - xPadding;
        xMax = maxX + xPadding;
        yMin = minY - yPadding;
        yMax = maxY + yPadding;
        double xCenter = (xMin + xMax) / 2;
        double yCenter = (yMin + yMax) / 2;
        double xHalf = (xMax - xMin) / 2 / zoom;
        double yHalf = (yMax - yMin) / 2 / zoom;
        xMin = xCenter - xHalf;
        xMax = xCenter + xHalf;
        yMin = yCenter - yHalf;
        yMax = yCenter + yHalf;
    }

    private void drawGrid(Graphics graphics) {
        for (int i = 0; i <= 5; i++) {
            int x = left + i * chartWidth / 5;
            int y = top + i * chartHeight / 5;
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.drawLine(x, top, x, top + chartHeight);
            graphics.drawLine(left, y, left + chartWidth, y);
            graphics.setColor(Color.BLACK);
            double xValue = restoreScale(xMin + (xMax - xMin) * i / 5, logX);
            double yValue = restoreScale(yMax - (yMax - yMin) * i / 5, logY);
            graphics.drawString(formatNumber(xValue), x - 20, top + chartHeight + 20);
            graphics.drawString(formatNumber(yValue), 8, y + 4);
        }
        graphics.drawRect(left, top, chartWidth, chartHeight);
    }

    private void drawCollection(Graphics graphics, String name, Color color) {
        ArrayList<LogData> series = new ArrayList<LogData>();
        for (int i = 0; i < data.size(); i++) {
            LogData log = data.get(i);
            if (log.collection.equals(name)) series.add(log);
        }
        // Сортируем по числу элементов обычными вложенными циклами.
        for (int i = 0; i < series.size(); i++) {
            for (int j = i + 1; j < series.size(); j++) {
                if (series.get(j).count < series.get(i).count) {
                    LogData temporary = series.get(i);
                    series.set(i, series.get(j));
                    series.set(j, temporary);
                }
            }
        }
        graphics.setColor(color);
        int previousX = -1;
        int previousY = -1;
        for (int i = 0; i < series.size(); i++) {
            LogData log = series.get(i);
            int x = screenX(log.count);
            int y = screenY(log.getValue(metric));
            if (previousX >= 0) graphics.drawLine(previousX, previousY, x, y);
            graphics.fillOval(x - 5, y - 5, 10, 10);
            previousX = x;
            previousY = y;
        }
    }

    private void drawLegend(Graphics graphics) {
        int x = left + 12;
        int y = top + 20;
        if (containsCollection("ArrayList")) {
            graphics.setColor(Color.BLUE);
            graphics.fillRect(x, y - 9, 16, 9);
            graphics.setColor(Color.BLACK);
            graphics.drawString("ArrayList", x + 23, y);
            y = y + 22;
        }
        if (containsCollection("LinkedList")) {
            graphics.setColor(Color.RED);
            graphics.fillRect(x, y - 9, 16, 9);
            graphics.setColor(Color.BLACK);
            graphics.drawString("LinkedList", x + 23, y);
        }
    }

    private boolean containsCollection(String name) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).collection.equals(name)) return true;
        }
        return false;
    }

    private int screenX(double value) {
        double changed = changeScale(value, logX);
        return left + (int) Math.round((changed - xMin) / (xMax - xMin) * chartWidth);
    }

    private int screenY(double value) {
        double changed = changeScale(value, logY);
        return top + chartHeight
                - (int) Math.round((changed - yMin) / (yMax - yMin) * chartHeight);
    }

    private double changeScale(double value, boolean logarithmic) {
        if (logarithmic) {
            if (value < 1) value = 1;
            return Math.log10(value);
        }
        return value;
    }

    private double restoreScale(double value, boolean logarithmic) {
        if (logarithmic) return Math.pow(10, value);
        return value;
    }

    private String formatNumber(double value) {
        if (value >= 1000000) return Math.round(value / 1000000) + "M";
        if (value >= 1000) return Math.round(value / 1000) + "k";
        return String.valueOf(Math.round(value));
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (chartWidth < 1 || chartHeight < 1) return null;
        for (int i = 0; i < data.size(); i++) {
            LogData log = data.get(i);
            int dx = screenX(log.count) - event.getX();
            int dy = screenY(log.getValue(metric)) - event.getY();
            if (dx * dx + dy * dy <= 100) {
                String operation = "добавление";
                if (metric >= 2) operation = log.secondOperation;
                return log.collection + ", " + log.count + " элементов, "
                        + operation + ": " + log.getValue(metric) + " нс ("
                        + log.file.getName() + ")";
            }
        }
        return null;
    }
}
