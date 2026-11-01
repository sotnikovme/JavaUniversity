package test_code;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

    static String filePath = "test_code/test.bin";

    public static void main(String[] args) throws IOException {
        addInFile();
        // readFromFile();
    }


    public static int addInFile() throws IOException {
        try (
            FileOutputStream out = new FileOutputStream(filePath, true);
        ) {

            Scanner scanner = new Scanner(System.in);
            String data = scanner.nextLine();

            while(data != "C"){
                byte[] inBytes = (data + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
                out.write(inBytes);
                data = scanner.nextLine();
        }
        return 0;
        }
    }


    public static String readFromFile() throws IOException {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            return line;
        }
    }

}
