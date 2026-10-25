package service;

import interfaces.freightTransport;
import model.CargoClass;
import model.Vehicle;
import model.freight.CarWithTrailer;
import model.freight.Trailer;
import model.freight.Truck;
import model.passenger.Bus;
import model.passenger.Car;

public class Checkpoint {
    private String reason;

    public boolean Pass(Vehicle vehicle) {
        int maxDimensions;
        int maxWeight;
        int maxSpeed;

        if (vehicle instanceof Car) {
            maxDimensions = 500;
            maxWeight = 3500;
            maxSpeed = 60;
        } else if (vehicle instanceof Truck) {
            maxDimensions = 1200;
            maxWeight = 12000;
            maxSpeed = 40;
        } else if (vehicle instanceof Bus) {
            maxDimensions = 1400;
            maxWeight = 18000;
            maxSpeed = 40;
        } else if (vehicle instanceof Trailer) {
            maxDimensions = 1600;
            maxWeight = 24000;
            maxSpeed = 30;
        } else if (vehicle instanceof CarWithTrailer) {
            maxDimensions = 1000;
            maxWeight = 7000;
            maxSpeed = 40;
        } else {
            reason = "Unknown vehicle type.";
            return false;
        }

        if (vehicle.getDimensions() > maxDimensions) {
            reason = "Maximum dimensions exceeded: " + maxDimensions + " cm.";
            return false;
        }

        if (vehicle.getWeight() > maxWeight) {
            reason = "Maximum weight exceeded: " + maxWeight + " kg.";
            return false;
        }

        if (vehicle.getSpeed() > maxSpeed) {
            reason = "Maximum speed exceeded: " + maxSpeed + " km/h.";
            return false;
        }

        if (vehicle.getCargoClass() != CargoClass.SAFE) {
            if (!(vehicle instanceof freightTransport)) {
                reason = "Only freight transport may carry this cargo class.";
                return false;
            }

            freightTransport freightVehicle = (freightTransport) vehicle;
            if (!freightVehicle.canTransportCargo()) {
                reason = "This vehicle cannot transport this cargo class.";
                return false;
            }
        }

        if ((vehicle.getCargoClass() == CargoClass.FLAMMABLE
                || vehicle.getCargoClass() == CargoClass.CHEMICALLY_DANGEROUS)
                && vehicle.getSpeed() > 30) {
            reason = "Dangerous cargo must pass at a speed of no more than 30 km/h.";
            return false;
        }

        if (vehicle.getCargoClass() == CargoClass.EXPLOSIVE) {
            if (!(vehicle instanceof Truck || vehicle instanceof Trailer)) {
                reason = "Explosive cargo may only be transported by a truck or trailer.";
                return false;
            }

            if (vehicle.getSpeed() > 20) {
                reason = "Explosive cargo must pass at a speed of no more than 20 km/h.";
                return false;
            }
        }

        if (vehicle.getCargoClass() == CargoClass.SPECIAL_IMPORTANCE
                && vehicle.getSpeed() > 35) {
            reason = "Special-importance cargo must pass at a speed of no more than 35 km/h.";
            return false;
        }

        if (vehicle instanceof freightTransport) {
            ((freightTransport) vehicle).checkFreight();
        }

        reason = "";
        return true;
    }

    public String getReason() {
        return reason;
    }
}
