import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main implements ActionListener {
    private JFrame frame;
    private JTabbedPane tabs;
    private JComboBox<String> metric;
    private JCheckBox combined;
    private JCheckBox legend;
    private JCheckBox logX;
    private JCheckBox logY;
    private JLabel status;
    private JButton openButton;
    private JButton measureButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JButton saveButton;
    private ArrayList<LogData> data = new ArrayList<LogData>();
    private double zoom = 1.0;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Main program = new Main();
                program.show();
            }
        });
    }

    private void show() {
        frame = new JFrame("Лабораторная работа 5 — графики коллекций");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        tabs = new JTabbedPane();
        status = new JLabel("Откройте логи или проведите измерение.");
        metric = new JComboBox<String>(new String[] {
                "Среднее время добавления",
                "Общее время добавления",
                "Среднее время удаления / замены",
                "Общее время удаления / замены"
        });
        combined = new JCheckBox("Совместный просмотр", true);
        legend = new JCheckBox("Легенда", true);
        logX = new JCheckBox("Логарифмическая X");
        logY = new JCheckBox("Логарифмическая Y");
        openButton = new JButton("Открыть логи...");
        measureButton = new JButton("Измерить добавление и удаление");
        zoomInButton = new JButton("Масштаб +");
        zoomOutButton = new JButton("Масштаб -");
        saveButton = new JButton("Сохранить настройки");

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(openButton);
        toolbar.add(measureButton);
        toolbar.add(metric);
        toolbar.add(combined);
        toolbar.add(zoomInButton);
        toolbar.add(zoomOutButton);
        toolbar.add(legend);
        toolbar.add(logX);
        toolbar.add(logY);
        toolbar.add(saveButton);
        frame.add(toolbar, BorderLayout.NORTH);
        frame.add(tabs, BorderLayout.CENTER);
        frame.add(status, BorderLayout.SOUTH);

        openButton.addActionListener(this);
        measureButton.addActionListener(this);
        zoomInButton.addActionListener(this);
        zoomOutButton.addActionListener(this);
        saveButton.addActionListener(this);
        metric.addActionListener(this);
        combined.addActionListener(this);
        legend.addActionListener(this);
        logX.addActionListener(this);
        logY.addActionListener(this);

        updateCharts();
        frame.setMinimumSize(new Dimension(800, 500));
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == openButton) {
            openFiles();
        } else if (source == measureButton) {
            measure();
        } else if (source == saveButton) {
            saveSettings();
        } else if (source == zoomInButton) {
            zoom = zoom * 1.5;
            if (zoom > 64) zoom = 64;
            updateCharts();
        } else if (source == zoomOutButton) {
            zoom = zoom / 1.5;
            if (zoom < 1) zoom = 1;
            updateCharts();
        } else {
            updateCharts();
        }
    }

    private void measure() {
        try {
            File[] files = Benchmark.run(new File("logs"));
            for (int i = 0; i < files.length; i++) {
                addOrReplace(LogData.read(files[i]));
            }
            updateCharts();
        } catch (IOException exception) {
            showError("Не удалось провести измерение: " + exception.getMessage());
        }
    }

    private void openFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите логи ArrayList и LinkedList");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Файлы логов (*.log)", "log"));
        File logs = new File("logs");
        if (logs.isDirectory()) chooser.setCurrentDirectory(logs);
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        File[] files = chooser.getSelectedFiles();
        String errors = "";
        int loaded = 0;
        for (int i = 0; i < files.length; i++) {
            try {
                LogData log = LogData.read(files[i]);
                addOrReplace(log);
                if (loaded == 0) applySettings(log.settings);
                loaded++;
            } catch (Exception exception) {
                errors = errors + files[i].getName() + ": " + exception.getMessage() + "\n";
            }
        }
        if (!errors.isEmpty()) showError(errors);
        updateCharts();
    }

    private void addOrReplace(LogData log) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).file.equals(log.file)) {
                data.set(i, log);
                return;
            }
        }
        data.add(log);
    }

    private void applySettings(ViewSettings settings) {
        zoom = settings.zoom;
        legend.setSelected(settings.legend);
        logX.setSelected(settings.logX);
        logY.setSelected(settings.logY);
    }

    private void saveSettings() {
        if (data.isEmpty()) {
            showError("Сначала загрузите файлы логов.");
            return;
        }
        ViewSettings settings = new ViewSettings(zoom, legend.isSelected(),
                logX.isSelected(), logY.isSelected());
        String errors = "";
        for (int i = 0; i < data.size(); i++) {
            LogData log = data.get(i);
            try {
                settings.save(log.file);
                log.settings = settings;
            } catch (IOException exception) {
                errors = errors + log.file.getName() + ": " + exception.getMessage() + "\n";
            }
        }
        if (errors.isEmpty()) {
            status.setText("Настройки сохранены рядом с файлами логов.");
        } else {
            showError(errors);
        }
    }

    private void updateCharts() {
        int selected = tabs.getSelectedIndex();
        tabs.removeAll();
        if (data.isEmpty() || combined.isSelected()) {
            ChartPanel chart = new ChartPanel(data, metric.getSelectedIndex(), zoom,
                    legend.isSelected(), logX.isSelected(), logY.isSelected());
            tabs.addTab("Все файлы", chart);
        } else {
            for (int i = 0; i < data.size(); i++) {
                LogData log = data.get(i);
                ArrayList<LogData> oneFile = new ArrayList<LogData>();
                oneFile.add(log);
                ChartPanel chart = new ChartPanel(oneFile, metric.getSelectedIndex(), zoom,
                        legend.isSelected(), logX.isSelected(), logY.isSelected());
                tabs.addTab(log.file.getName(), chart);
            }
        }
        if (selected >= 0 && selected < tabs.getTabCount()) {
            tabs.setSelectedIndex(selected);
        }
        if (!data.isEmpty()) {
            double roundedZoom = Math.round(zoom * 100) / 100.0;
            status.setText("Загружено файлов: " + data.size() + ". Масштаб: " + roundedZoom);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
}
