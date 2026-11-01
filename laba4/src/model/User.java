package model;

import java.util.Scanner;

public class User {

    String login;
    String password;

    public void User(){
        Scanner scanner = new Scanner(System.in);
        this.login = scanner.nextLine();
        this.password = scanner.nextLine();
    }

    public String toString(){
        return this.login + "@" + this.password;
    }

}
