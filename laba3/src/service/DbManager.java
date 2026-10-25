package service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import model.CargoClass;
import model.Vehicle;
import model.freight.CarWithTrailer;
import model.freight.Trailer;
import model.freight.Truck;
import model.passenger.Bus;
import model.passenger.Car;


public class DbManager {
    private static final int VEHICLE_TYPES_COUNT = 5;
    private static final int[] EXPERIMENT_SIZES = {10, 100, 1000, 10000, 100000};
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final Random random = new Random();

    public TransportDb createTransportDb() {
        TransportDb transportDb = new TransportDb();

        transportDb.addVehicle(new Car(470, 1800, 55, CargoClass.SAFE));
        transportDb.addVehicle(new Car(520, 3000, 55, CargoClass.SAFE));
        transportDb.addVehicle(new Truck(1100, 11000, 35, CargoClass.FLAMMABLE));
        transportDb.addVehicle(new Bus(1300, 19000, 40, CargoClass.SAFE));
        transportDb.addVehicle(new Trailer(1500, 23000, 25, CargoClass.EXPLOSIVE));
        transportDb.addVehicle(new CarWithTrailer(950, 6000, 45,
                CargoClass.CHEMICALLY_DANGEROUS));
        transportDb.addVehicle(new Truck(1000, 9000, 30, CargoClass.SPECIAL_IMPORTANCE));

        return transportDb;
    }

// Random TransportDb's lists

    public TransportDb createRandomTransportDb(int vehicleCount) {
        TransportDb transportDb = new TransportDb();
        fillWithRandomVehicles(transportDb, vehicleCount);
        return transportDb;
    }

    public void fillWithRandomVehicles(TransportDb transportDb, int vehicleCount) {
        Objects.requireNonNull(transportDb, "Transport database must not be null.");

        if (vehicleCount < 0) {
            throw new IllegalArgumentException("Vehicle count must not be negative.");
        }

        for (int index = 0; index < vehicleCount; index++) {
            transportDb.addVehicle(createRandomVehicle());
        }
    }

    private Vehicle createRandomVehicle() {
        int dimensions = randomInt(300, 1800);
        int weight = randomInt(1000, 30000);
        int speed = randomInt(10, 140);
        CargoClass cargoClass = randomCargoClass();

        switch (random.nextInt(VEHICLE_TYPES_COUNT)) {
            case 0:
                return new Car(dimensions, weight, speed, cargoClass);
            case 1:
                return new Bus(dimensions, weight, speed, cargoClass);
            case 2:
                return new Truck(dimensions, weight, speed, cargoClass);
            case 3:
                return new Trailer(dimensions, weight, speed, cargoClass);
            default:
                return new CarWithTrailer(dimensions, weight, speed, cargoClass);
        }
    }

    private int randomInt(int minimum, int maximum) {
        return minimum + random.nextInt(maximum - minimum + 1);
    }

    private CargoClass randomCargoClass() {
        CargoClass[] cargoClasses = CargoClass.values();
        return cargoClasses[random.nextInt(cargoClasses.length)];
    }

    /** Runs experiments for ArrayList and LinkedList and writes ten log files. */
    public void runCollectionExperiments(Path logsDirectory) {
        for (int elementCount : EXPERIMENT_SIZES) {
            runArrayListExperiment(logsDirectory, elementCount);
            runLinkedListExperiment(logsDirectory, elementCount);
        }
    }

    private void runArrayListExperiment(Path logsDirectory, int elementCount) {
        final TransportDb transportDb = new TransportDb();
        runExperiment("ArrayList", logsDirectory.resolve("ArrayList_" + elementCount + ".log"),
                elementCount, new VehicleCollection() {
                    @Override
                    public void add(Vehicle vehicle) {
                        transportDb.addToArrayDb(vehicle);
                    }

                    @Override
                    public void set(int index, Vehicle vehicle) {
                        transportDb.setArrayVehicle(index, vehicle);
                    }
                });
    }

    private void runLinkedListExperiment(Path logsDirectory, int elementCount) {
        final TransportDb transportDb = new TransportDb();
        runExperiment("LinkedList", logsDirectory.resolve("LinkedList_" + elementCount + ".log"),
                elementCount, new VehicleCollection() {
                    @Override
                    public void add(Vehicle vehicle) {
                        transportDb.addToLinkedDb(vehicle);
                    }

                    @Override
                    public void set(int index, Vehicle vehicle) {
                        transportDb.setLinkedVehicle(index, vehicle);
                    }
                });
    }

    private void runExperiment(String collectionName, Path logFile, int elementCount,
                               VehicleCollection collection) {
        try (MessageHandler messages = new MessageHandler(logFile)) {
            ArrayList<Long> addDurations = new ArrayList<Long>();
            ArrayList<Long> setDurations = new ArrayList<Long>();
            long experimentStart = System.nanoTime();

            messages.info("Start program: " + DATE_TIME_FORMAT.format(LocalDateTime.now()));
            messages.info(collectionName);

            try {
                for (int index = 0; index < elementCount; index++) {
                    Vehicle vehicle = createRandomVehicle();
                    long operationStart = System.nanoTime();
                    collection.add(vehicle);
                    long duration = System.nanoTime() - operationStart;
                    addDurations.add(duration);
                    messages.info("add, ID = " + (index + 1) + ", " + duration + " ns");
                }

                ArrayList<Integer> indexes = createRandomIndexes(elementCount, elementCount / 10);
                for (int index : indexes) {
                    Vehicle vehicle = createRandomVehicle();
                    long operationStart = System.nanoTime();
                    collection.set(index, vehicle);
                    long duration = System.nanoTime() - operationStart;
                    setDurations.add(duration);
                    messages.info("set, ID = " + index + ", " + duration + " ns");
                }
            } catch (RuntimeException exception) {
                messages.error("Collection operation failed.", exception);
            }

            writeStatistics(messages, "add", addDurations);
            writeStatistics(messages, "set", setDurations);
            messages.info("totalOperationsTimeNs = " + (sum(addDurations) + sum(setDurations)));
            messages.info("experimentTimeNs = " + (System.nanoTime() - experimentStart));
            messages.info("errorCount = " + messages.getErrorCount());
            messages.info("Finish program: " + DATE_TIME_FORMAT.format(LocalDateTime.now()));
        }
    }

    private ArrayList<Integer> createRandomIndexes(int collectionSize, int indexesCount) {
        ArrayList<Integer> indexes = new ArrayList<Integer>(collectionSize);
        for (int index = 0; index < collectionSize; index++) {
            indexes.add(index);
        }
        Collections.shuffle(indexes, random);
        return new ArrayList<Integer>(indexes.subList(0, indexesCount));
    }

    private void writeStatistics(MessageHandler messages, String operation, ArrayList<Long> durations) {
        long totalTime = sum(durations);
        long averageTime = durations.isEmpty() ? 0 : totalTime / durations.size();

        messages.info(operation + "TotalCount = " + durations.size());
        messages.info(operation + "TotalTimeNs = " + totalTime);
        messages.info(operation + "AverageTimeNs = " + averageTime);
    }

    private long sum(ArrayList<Long> durations) {
        long total = 0;
        for (long duration : durations) {
            total += duration;
        }
        return total;
    }

    private interface VehicleCollection {
        void add(Vehicle vehicle);

        void set(int index, Vehicle vehicle);
    }
}
