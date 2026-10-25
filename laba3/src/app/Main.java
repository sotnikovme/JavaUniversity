package app;

import java.nio.file.Paths;
import service.DbManager;

public class Main {
    public static void main(String[] args) {
        DbManager dbManager = new DbManager();
        dbManager.runCollectionExperiments(Paths.get("logs"));
    }
}
