// import java.awt.*;
// import java.awt.event.*;
// import java.io.*;
// import java.util.*;
// import javax.swing.*;
// import javax.swing.filechooser.FileNameExtensionFilter;

// public class Main implements ActionListener {
//     static final File LOGS = new File("C:\\main\\MIET\\sem3\\JavaUniversity\\logs");
//     static final String[] NAMES = {"Среднее добавление", "Общее добавление", "Среднее удаление / замена", "Общее удаление / замена"};

//     JFrame frame = new JFrame("Лабораторная работа 5");
//     JTabbedPane tabs = new JTabbedPane();
//     JComboBox<String> metric = new JComboBox<String>(NAMES);
//     JCheckBox together = new JCheckBox("Совместно", true);
//     JCheckBox legend = new JCheckBox("Легенда", true);
//     JCheckBox logX = new JCheckBox("Логарифмическая X");
//     JCheckBox logY = new JCheckBox("Логарифмическая Y");
//     ArrayList<Log> logs = new ArrayList<Log>();
//     double zoom = 1;

//     public static void main(String[] args) {
//         EventQueue.invokeLater(new Runnable() { public void run() { new Main().start(); } });
//     }

//     void start() {
//         JPanel panel = new JPanel();
//         addButton(panel, "Открыть логи");
//         addButton(panel, "+");
//         addButton(panel, "-");
//         addButton(panel, "Сохранить настройки");
//         panel.add(metric); panel.add(together); panel.add(legend); panel.add(logX); panel.add(logY);
//         metric.addActionListener(this); together.addActionListener(this); legend.addActionListener(this);
//         logX.addActionListener(this); logY.addActionListener(this);
//         frame.add(panel, BorderLayout.NORTH);
//         frame.add(tabs, BorderLayout.CENTER);
//         frame.setSize(1000, 650);
//         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         frame.setLocationRelativeTo(null);
//         frame.setVisible(true);
//         redraw();
//     }

//     void addButton(JPanel panel, String text) {
//         JButton button = new JButton(text); button.addActionListener(this); panel.add(button);
//     }

//     public void actionPerformed(ActionEvent e) {
//         if (e.getSource() instanceof JButton) {
//             String command = ((JButton)e.getSource()).getText();
//             if (command.equals("Открыть логи")) open();
//             else if (command.equals("+")) { zoom *= 1.5; redraw(); }
//             else if (command.equals("-")) { zoom = Math.max(1, zoom / 1.5); redraw(); }
//             else save();
//         } else redraw();
//     }

//     void open() {
//         JFileChooser chooser = new JFileChooser(LOGS);
//         chooser.setMultiSelectionEnabled(true);
//         chooser.setFileFilter(new FileNameExtensionFilter("Логи", "log"));
//         if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
//         String errors = "";
//         for (File file : chooser.getSelectedFiles()) try {
//             Log log = new Log(file);
//             if (logs.isEmpty()) useSettings(log);
//             logs.add(log);
//         } catch (Exception ex) { errors += file.getName() + "\n"; }
//         if (!errors.equals("")) JOptionPane.showMessageDialog(frame, "Не удалось прочитать:\n" + errors);
//         redraw();
//     }

//     void useSettings(Log log) {
//         File file = new File(log.file.getPath() + ".settings");
//         if (!file.exists()) return;
//         try {
//             BufferedReader in = new BufferedReader(new FileReader(file));
//             zoom = Double.parseDouble(in.readLine());
//             legend.setSelected(Boolean.parseBoolean(in.readLine()));
//             logX.setSelected(Boolean.parseBoolean(in.readLine()));
//             logY.setSelected(Boolean.parseBoolean(in.readLine()));
//             in.close();
//         } catch (Exception ignored) { zoom = 1; }
//     }

//     void save() {
//         for (Log log : logs) try {
//             PrintWriter out = new PrintWriter(new FileWriter(log.file.getPath() + ".settings"));
//             out.println(zoom); out.println(legend.isSelected()); out.println(logX.isSelected()); out.println(logY.isSelected());
//             out.close();
//         } catch (IOException ex) { JOptionPane.showMessageDialog(frame, "Не удалось сохранить настройки"); }
//     }

//     void redraw() {
//         tabs.removeAll();
//         int m = metric.getSelectedIndex();
//         if (logs.isEmpty() || together.isSelected()) tabs.add("Все файлы", new Chart(logs, m));
//         else for (Log log : logs) {
//             ArrayList<Log> one = new ArrayList<Log>(); one.add(log);
//             tabs.add(log.file.getName(), new Chart(one, m));
//         }
//     }

//     class Chart extends JPanel {
//         ArrayList<Log> data;
//         int metric;
//         int left = 70, top = 45, width, height;
//         double minX, maxX, minY, maxY;

//         Chart(ArrayList<Log> data, int metric) {
//             this.data = data; this.metric = metric;
//             setToolTipText("");
//         }

//         protected void paintComponent(Graphics g) {
//             super.paintComponent(g);
//             width = getWidth() - 110; height = getHeight() - 100;
//             if (data.isEmpty()) { g.drawString("Откройте логи из папки JavaUniversity\\logs", left, top); return; }
//             range();
//             g.drawString(NAMES[metric], left, 20);
//             for (int i = 0; i <= 5; i++) {
//                 int x = left + width * i / 5, y = top + height * i / 5;
//                 g.setColor(Color.LIGHT_GRAY); g.drawLine(x, top, x, top + height); g.drawLine(left, y, left + width, y);
//                 g.setColor(Color.BLACK);
//                 g.drawString(number(back(minX + (maxX-minX)*i/5, logX.isSelected())), x-15, top+height+18);
//                 g.drawString(number(back(maxY - (maxY-minY)*i/5, logY.isSelected())), 3, y+4);
//             }
//             g.drawRect(left, top, width, height);
//             draw(g, "ArrayList", Color.BLUE); draw(g, "LinkedList", Color.RED);
//             if (legend.isSelected()) { g.setColor(Color.BLUE); g.drawString("ArrayList", left+10, top+20); g.setColor(Color.RED); g.drawString("LinkedList", left+10, top+38); }
//         }

//         void range() {
//             minX = minY = Double.MAX_VALUE; maxX = maxY = -Double.MAX_VALUE;
//             for (Log log : data) {
//                 double x = change(log.count, logX.isSelected()), y = change(log.value(metric), logY.isSelected());
//                 minX = Math.min(minX,x); maxX = Math.max(maxX,x); minY = Math.min(minY,y); maxY = Math.max(maxY,y);
//             }
//             double dx = Math.max(.2, (maxX-minX)*.1), dy = Math.max(.2, (maxY-minY)*.1);
//             minX -= dx; maxX += dx; minY -= dy; maxY += dy;
//             double cx = (minX+maxX)/2, cy = (minY+maxY)/2;
//             double hx = (maxX-minX)/zoom/2, hy = (maxY-minY)/zoom/2;
//             minX=cx-hx; maxX=cx+hx; minY=cy-hy; maxY=cy+hy;
//         }

//         void draw(Graphics g, String type, Color color) {
//             ArrayList<Log> line = new ArrayList<Log>();
//             for (Log log : data) if (log.type.equals(type)) line.add(log);
//             Collections.sort(line, new Comparator<Log>() { public int compare(Log a, Log b) { return a.count-b.count; } });
//             g.setColor(color); int px = -1, py = -1;
//             for (Log log : line) {
//                 int x = x(log.count), y = y(log.value(metric));
//                 if (px >= 0) g.drawLine(px,py,x,y);
//                 g.fillOval(x-4,y-4,8,8); px=x; py=y;
//             }
//         }

//         int x(long value) { return left + (int)((change(value,logX.isSelected())-minX)/(maxX-minX)*width); }
//         int y(long value) { return top+height - (int)((change(value,logY.isSelected())-minY)/(maxY-minY)*height); }
//         double change(double value, boolean logarithm) { return logarithm ? Math.log10(Math.max(1,value)) : value; }
//         double back(double value, boolean logarithm) { return logarithm ? Math.pow(10,value) : value; }
//         String number(double value) { return value > 1000000 ? Math.round(value/1000000)+"M" : value > 1000 ? Math.round(value/1000)+"k" : ""+Math.round(value); }

//         public String getToolTipText(MouseEvent e) {
//             for (Log log : data) { int dx=x(log.count)-e.getX(), dy=y(log.value(metric))-e.getY();
//                 if (dx*dx+dy*dy < 100) return log.type+", "+log.count+" элементов: "+log.value(metric)+" нс";
//             }
//             return null;
//         }
//     }

//     class Log {
//         File file; String type; int count; long addAverage, addTotal, otherAverage, otherTotal;
//         Log(File file) throws Exception {
//             this.file=file; String[] name=file.getName().replace(".log","").split("_");
//             type=name[0]; count=Integer.parseInt(name[1]);
//             BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
//             String line;
//             while((line=in.readLine())!=null) {
//                 if(line.startsWith("addAverageTimeNs")) addAverage=value(line);
//                 if(line.startsWith("addTotalTimeNs")) addTotal=value(line);
//                 if(line.startsWith("setAverageTimeNs")||line.startsWith("removeAverageTimeNs")) otherAverage=value(line);
//                 if(line.startsWith("setTotalTimeNs")||line.startsWith("removeTotalTimeNs")) otherTotal=value(line);
//             }
//             in.close();
//         }
//         long value(int metric) { return metric==0?addAverage:metric==1?addTotal:metric==2?otherAverage:otherTotal; }
//         long value(String line) { return Long.parseLong(line.substring(line.indexOf('=')+1).trim()); }
//     }
// }