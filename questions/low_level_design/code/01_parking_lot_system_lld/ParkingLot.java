import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingLot {
    private static ParkingLot instance;
    List<Floor> floors;
    Map<String, Ticket> activeTickets;
    DisplayBoard displayBoard;
    Map<String, Gate> entryGateMap;
    Map<String, Gate> exitGateMap;
    ParkingStrategy parkingStrategy;


    private ParkingLot(List<Floor> floors, List<Gate> entryGates, List<Gate> exitGates, ParkingStrategy parkingStrategy) {
        this.floors = floors;
        this.activeTickets = new HashMap<>();
        this.displayBoard = new DisplayBoard();
        for (Floor floor : floors) {
            floor.parkingSpotList.forEach(parkingSpot -> this.displayBoard.freeSpots.put(parkingSpot.spotType, this.displayBoard.freeSpots.getOrDefault(parkingSpot.spotType,0) + 1));
        }
        this.entryGateMap = new HashMap<>();
        for (Gate gate : entryGates) {
            this.entryGateMap.put(gate.id, gate);
        }
        this.exitGateMap = new HashMap<>();
        for (Gate gate : exitGates) {
            this.exitGateMap.put(gate.id, gate);
        }
        this.parkingStrategy = parkingStrategy;
    }

    public static synchronized ParkingLot getInstance(List<Floor> floors, List<Gate> entryGates, List<Gate> exitGates,  ParkingStrategy parkingStrategy) {
        if(instance == null) {
            instance = new ParkingLot(floors, entryGates, exitGates, parkingStrategy);
        }
        return instance;
    }

    Ticket parkVehicle(Vehicle vehicle) {
        ParkingSpot parkingSpot = parkingStrategy.findAvailableSpot(vehicle, floors);
        Ticket ticket = null;
        if(parkingSpot != null) {
            ticket = createParkingTicket(vehicle, parkingSpot);
            parkingSpot.updateParkingSpot(vehicle, true);
            activeTickets.put(ticket.ticketId, ticket);
            displayBoard.updateDisplayBoard(parkingSpot.spotType, true);
        }
        return ticket;
    }

    void unParkVehicle(Ticket ticket) {
        activeTickets.remove(ticket.ticketId);
        ParkingSpot currentSpot = ticket.parkingSpot;
        currentSpot.updateParkingSpot(ticket.vehicle, false);
        displayBoard.updateDisplayBoard(ticket.parkingSpot.spotType,false);
    }

    Ticket createParkingTicket(Vehicle vehicle, ParkingSpot parkingSpot) {
        return new Ticket(vehicle, LocalDateTime.now(),parkingSpot);
    }

    Map<SpotType, Integer> getAllAvailableSpots() {
        return displayBoard.freeSpots;
    }
}
