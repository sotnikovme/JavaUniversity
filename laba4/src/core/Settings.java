package core;

import core.DB;

import java.io.FileInputStream;
import java.util.Scanner;


public class Settings {
    String DbName = "data/vehicles.txt";
    String LogFile = "logs/app.log";
    DB db = new DB();
    String login;
    String password;
    Group group;
    boolean debug = true;
    boolean autotests = true;
    // db.file=data/vehicles.txt
    // log.file=logs/app.log


    public Settings() {
        Scanner scanner = new Scanner(System.in);
        this.login = scanner.nextLine();
        this.password = scanner.nextLine();
    }
    
}

