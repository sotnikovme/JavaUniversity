package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import model.Vehicle;

public class TransportDb {
    private final ArrayList<Vehicle> arrayDb = new ArrayList<>();
    private final LinkedList<Vehicle> linkedDb = new LinkedList<>();

    public void addVehicle(Vehicle vehicle) {
        arrayDb.add(vehicle);
        linkedDb.add(vehicle);
    }

    public void addToArrayDb(Vehicle vehicle) {
        arrayDb.add(vehicle);
    }

    public void addToLinkedDb(Vehicle vehicle) {
        linkedDb.add(vehicle);
    }

    public void setArrayVehicle(int index, Vehicle vehicle) {
        arrayDb.set(index, vehicle);
    }

    public void setLinkedVehicle(int index, Vehicle vehicle) {
        linkedDb.set(index, vehicle);
    }

    public List<Vehicle> getArrayDb() {
        return Collections.unmodifiableList(arrayDb);
    }

    public List<Vehicle> getLinkedDb() {
        return Collections.unmodifiableList(linkedDb);
    }

}
