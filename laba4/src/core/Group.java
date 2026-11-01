package core;

public enum Group {
    ROOT("root"),
    USER("user");

    private String title;

    Group(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
