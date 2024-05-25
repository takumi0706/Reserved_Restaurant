package main.track;

import java.util.*;

public class Advance {
    public static void main(String[] args) {
        String[] input = getStdin();
        int nTables = Integer.parseInt(input[0].trim());
        int[] tableCapacities = Arrays.stream(input[1].trim().split(" ")).mapToInt(Integer::parseInt).toArray();
        String[] runningHoursInfo = input[2].trim().split(" ");
        String runningHours = runningHoursInfo[0];
        int kSlots = Integer.parseInt(runningHoursInfo[1]);
        List<String> slots = Arrays.asList(runningHoursInfo).subList(2, 2 + kSlots);

        Restaurant restaurant = new Restaurant(nTables, tableCapacities, runningHours, kSlots, slots);

        for (int i = 3; i < input.length; i++) {
            String[] query = input[i].split(" ");
            String timestamp = query[0] + " " + query[1];
            String command = query[2];

            switch (command) {
                case "time":
                    handleTime(Integer.parseInt(query[3]), restaurant, timestamp);
                    break;
                case "issue-specified":
                    handleIssueSpecified(query, restaurant, timestamp);
                    break;
                case "issue-unspecified":
                    handleIssueUnspecified(query, restaurant, timestamp);
                    break;
                case "cancel":
                    handleCancel(query[3], restaurant, timestamp);
                    break;
                default:
                    break;
            }
        }
    }

    private static String[] getStdin() {
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> lines = new ArrayList<>();
        while (scanner.hasNext()) {
            lines.add(scanner.nextLine());
        }
        return lines.toArray(new String[0]);
    }

    private static void handleTime(int slot, Restaurant restaurant, String timestamp) {
        if (slot > 1) {
            int previousSlot = slot - 1;
            String currentDate = timestamp.split(" ")[0];
            restaurant.clearReservations(Integer.parseInt(currentDate), previousSlot);
        }
        if (slot <= restaurant.kSlots) {
            restaurant.printReservations(slot, timestamp);
        }
    }

    private static void handleIssueSpecified(String[] query, Restaurant restaurant, String timestamp) {
        int id = Integer.parseInt(query[3]);
        int reservationDate = Integer.parseInt(query[4]);
        int slot = Integer.parseInt(query[5]);
        int people = Integer.parseInt(query[6]);
        int tableId = Integer.parseInt(query[7]);

        if (restaurant.isCurrentSlot(slot, timestamp)) {
            System.out.printf("%s Error: the current slot cannot be specified.%n", timestamp);
        } else if (restaurant.isPastSlot(slot, timestamp)) {
            System.out.printf("%s Error: a past time cannot be specified.%n", timestamp);
        } else if (restaurant.isOverCapacity(tableId, people)) {
            System.out.printf("%s Error: the maximum number of people at the table has been exceeded.%n", timestamp);
        } else if (restaurant.isTableOccupied(tableId, reservationDate, slot)) {
            System.out.printf("%s Error: the table is occupied.%n", timestamp);
        } else {
            restaurant.addReservation(tableId, reservationDate, slot, id, people);
            System.out.printf("%s %05d%n", timestamp, id);
        }
    }

    private static void handleIssueUnspecified(String[] query, Restaurant restaurant, String timestamp) {
        int id = Integer.parseInt(query[3]);
        int reservationDate = Integer.parseInt(query[4]);
        int slot = Integer.parseInt(query[5]);
        int people = Integer.parseInt(query[6]);

        if (restaurant.isCurrentSlot(slot, timestamp)) {
            System.out.printf("%s Error: the current slot cannot be specified.%n", timestamp);
        } else if (restaurant.isPastSlot(slot, timestamp)) {
            System.out.printf("%s Error: a past time cannot be specified.%n", timestamp);
        } else {
            int tableId = restaurant.findBestTable(reservationDate, slot, people);
            if (tableId == -1) {
                System.out.printf("%s Error: no available table is found.%n", timestamp);
            } else {
                restaurant.addReservation(tableId, reservationDate, slot, id, people);
                System.out.printf("%s %05d %d%n", timestamp, id, tableId);
            }
        }
    }

    private static void handleCancel(String id, Restaurant restaurant, String timestamp) {
        if (restaurant.cancelReservation(Integer.parseInt(id), timestamp)) {
            System.out.printf("%s Canceled %05d%n", timestamp, Integer.parseInt(id));
            restaurant.adjustReservations();
        } else {
            System.out.printf("%s Error: no such ticket is found.%n", timestamp);
        }
    }
}

class Restaurant {
    int nTables;
    Table[] tables;
    String runningHours;
    int kSlots;
    List<String> slots;

    Restaurant(int nTables, int[] capacities, String runningHours, int kSlots, List<String> slots) {
        this.nTables = nTables;
        this.tables = new Table[nTables];
        for (int i = 0; i < nTables; i++) {
            this.tables[i] = new Table(capacities[i], i + 1);
        }
        this.runningHours = runningHours;
        this.kSlots = kSlots;
        this.slots = slots;
    }

    boolean isCurrentSlot(int slot, String timestamp) {
        String slotTime = slots.get(slot - 1);
        String[] slotParts = slotTime.split("-");
        return timestamp.compareTo(slotParts[0]) >= 0 && timestamp.compareTo(slotParts[1]) <= 0;
    }

    boolean isPastSlot(int slot, String timestamp) {
        String[] timeParts = timestamp.split(" ");
        String endingTime = timeParts[1];
        String[] slotParts = slots.get(slot - 1).split("-");
        return endingTime.compareTo(slotParts[1]) >= 0;
    }

    boolean isOverCapacity(int tableId, int people) {
        return tables[tableId - 1].capacity < people;
    }

    boolean isTableOccupied(int tableId, int reservationDate, int slot) {
        return tables[tableId - 1].isOccupied(reservationDate, slot);
    }

    void addReservation(int tableId, int reservationDate, int slot, int id, int people) {
        tables[tableId - 1].addReservation(reservationDate, slot, new Reservation(id, reservationDate, slot, people, tableId));
    }

    int findBestTable(int reservationDate, int slot, int people) {
        Table bestTable = null;
        for (Table table : tables) {
            if (table.capacity >= people && !table.isOccupied(reservationDate, slot)) {
                if (bestTable == null || table.capacity < bestTable.capacity || (table.capacity == bestTable.capacity && table.tableNumber < bestTable.tableNumber)) {
                    bestTable = table;
                }
            }
        }
        return bestTable != null ? bestTable.tableNumber : -1;
    }

    boolean cancelReservation(int id, String timestamp) {
        boolean found = false;
        for (Table table : tables) {
            for (Map.Entry<Integer, Map<Integer, Reservation>> dateEntry : table.reservations.entrySet()) {
                Map<Integer, Reservation> slotReservations = dateEntry.getValue();
                for (Map.Entry<Integer, Reservation> slotEntry : slotReservations.entrySet()) {
                    Reservation reservation = slotEntry.getValue();
                    if (reservation.id == id) {
                        if (isCancelable(reservation, timestamp)) {
                            slotReservations.remove(slotEntry.getKey());
                            found = true;
                            return true;
                        } else {
                            System.out.printf("%s Error: you must cancel at least one day in advance.%n", timestamp);
                            found = true;
                            return false;
                        }
                    }
                }
                if (found) break;
            }
            if (found) break;
        }
        return false;
    }

    boolean isCancelable(Reservation reservation, String timestamp) {
        String currentDate = timestamp.split(" ")[0];
        return Integer.parseInt(currentDate) < reservation.reservationDate;
    }

    void adjustReservations() {
        List<Reservation> freeReservations = new ArrayList<>();
        for (Table table : tables) {
            for (Map<Integer, Reservation> slotReservations : table.reservations.values()) {
                freeReservations.addAll(slotReservations.values());
                slotReservations.clear();
            }
        }
        freeReservations.sort(Comparator.comparingInt((Reservation res) -> res.people).reversed().thenComparingInt(res -> res.id));

        for (Reservation res : freeReservations) {
            int tableId = findBestTable(res.reservationDate, res.slot, res.people);
            if (tableId != -1) {
                addReservation(tableId, res.reservationDate, res.slot, res.id, res.people);
            }
        }
    }

    void clearReservations(int reservationDate, int slot) {
        for (Table table : tables) {
            table.clearReservations(reservationDate, slot);
        }
    }

    void printReservations(int slot, String timestamp) {
        String currentDate = timestamp.split(" ")[0];
        for (Table table : tables) {
            table.printReservations(Integer.parseInt(currentDate), slot, timestamp);
        }
    }
}

class Table {
    int capacity;
    int tableNumber;
    Map<Integer, Map<Integer, Reservation>> reservations; // reservationDate -> slot -> Reservation

    Table(int capacity, int tableNumber) {
        this.capacity = capacity;
        this.tableNumber = tableNumber;
        this.reservations = new HashMap<>();
    }

    boolean isOccupied(int reservationDate, int slot) {
        return reservations.containsKey(reservationDate) && reservations.get(reservationDate).containsKey(slot);
    }

    void addReservation(int reservationDate, int slot, Reservation reservation) {
        reservations.computeIfAbsent(reservationDate, k -> new HashMap<>()).put(slot, reservation);
    }

    void clearReservations(int reservationDate, int slot) {
        if (reservations.containsKey(reservationDate)) {
            reservations.get(reservationDate).remove(slot);
        }
    }

    void printReservations(int reservationDate, int slot, String timestamp) {
        if (reservations.containsKey(reservationDate)) {
            Map<Integer, Reservation> slotReservations = reservations.get(reservationDate);
            if (slotReservations.containsKey(slot)) {
                Reservation reservation = slotReservations.get(slot);
                System.out.printf("%s table %d = %05d%n", timestamp, tableNumber, reservation.id);
            }
        }
    }
}

class Reservation {
    int id;
    int reservationDate;
    int slot;
    int people;
    int tableId;

    Reservation(int id, int reservationDate, int slot, int people, int tableId) {
        this.id = id;
        this.reservationDate = reservationDate;
        this.slot = slot;
        this.people = people;
        this.tableId = tableId;
    }
}
