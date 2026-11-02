package model;

import core.Group;

public class User {
    private final long id;
    private final String login, password;
    private final Group group;

    public User(long id, String login, String password, Group group) {
        if (id < 1 || login == null || login.isEmpty() || password == null || password.isEmpty() || group == null)
            throw new IllegalArgumentException("Invalid user data");
        this.id = id;
        this.login = login;
        this.password = password;
        this.group = group;
    }
    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Group getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return id + ": " + login + " (" + group + ")";
    }
}
