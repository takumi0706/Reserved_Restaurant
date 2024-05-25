//package main.track;
//
//import java.util.*;
//
//public class Advance {
//    public static void main(String[] args) {
//        String[] input = getStdin();
//        int n_tables = Integer.parseInt(input[0].trim());
//        int[] capacity = Arrays.stream(input[1].trim().split(" ")).mapToInt(Integer::parseInt).toArray();
//        String runningHours = input[2].split(" ")[0];
//        int k_slots = Integer.parseInt(input[2].split(" ")[1]);
//        List<String> slots = Arrays.asList(input[2].split(" ")).subList(2, 2 + k_slots);
//
//        Restaurant restaurant = new Restaurant(n_tables, capacity, runningHours, k_slots, slots);
//
//        for (int i = 3; i < input.length; i++) {
//            String[] query = input[i].split(" ");
//            String currentDate = query[0];
//            String timestamp = query[0] + " " + query[1];
//            String command = query[2];
//
//            int slot;
//            int id;
//            int reservationDate;
//            int people;
//
//            switch (command) {
//                case "time":
//                    slot = Integer.parseInt(query[3]);
//                    handleTime(slot, n_tables, restaurant, timestamp);
//                    break;
//
//                case "issue-specified":
//                    id = Integer.parseInt(query[3]);
//                    reservationDate = Integer.parseInt(query[4]);
//                    slot = Integer.parseInt(query[5]);
//                    people = Integer.parseInt(query[6]);
//                    int table_id = Integer.parseInt(query[7]);
//
//                    handleIssueSpecified(id, reservationDate, slot, people, table_id, restaurant, timestamp, currentDate);
//                    break;
//
//                case "issue-unspecified":
//                    id = Integer.parseInt(query[3]);
//                    reservationDate = Integer.parseInt(query[4]);
//                    slot = Integer.parseInt(query[5]);
//                    people = Integer.parseInt(query[6]);
//
//                    handleIssueUnspecified(id, reservationDate, slot, people, restaurant, timestamp, currentDate);
//                    break;
//
//                case "cancel":
//                    id = Integer.parseInt(query[3]);
//                    handleCancel(id, restaurant, timestamp, currentDate);
//                    break;
//
//                default:
//                    break;
//            }
//        }
//        System.exit(0);
//    }
//
//    private static String[] getStdin() {
//        Scanner scanner = new Scanner(System.in);
//        ArrayList<String> lines = new ArrayList<>();
//        while (scanner.hasNext()) {
//            lines.add(scanner.nextLine());
//        }
//        return lines.toArray(new String[lines.size()]);
//    }
//
//    private static void handleTime(int slot, int n_tables, Restaurant restaurant, String timestamp){
//        if (slot > 1) {
//            // Remove previous slot reservations
//            int previousSlot = slot - 1;
//            for (Table table : restaurant.tables) {
//                table.reservations.remove(previousSlot);
//            }
//        }
//        if (slot <= restaurant.k_slots ) {
//            // Print current slot reservations
//            for (int t = 0; t < n_tables; t++) {
//                Map<Integer, Reservation> slotReservations = restaurant.tables[t].reservations.get(slot);
//                if (slotReservations != null) {
//                    for (Reservation res : slotReservations.values()) {
//                        if(String.valueOf(res.reservationDate).equals(timestamp.split(" ")[0])){
//                            System.out.printf("%s table %d = %05d%n", timestamp, t + 1, res.id);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private static void handleIssueSpecified(int id, int reservationDate, int slot, int people, int table_id, Restaurant restaurant, String timestamp, String currentDate){
//        if (currentDate.equals(String.valueOf(reservationDate)) && isWithReservationSlot(timestamp, restaurant.slots, slot)) {
//            System.out.printf("%s Error: the current slot cannot be specified.%n", timestamp);
//        } else if (currentDate.equals(String.valueOf(reservationDate)) && isPastTime(timestamp, restaurant.slots, slot)){
//            System.out.printf("%s Error: a past time cannot be specified.%n", timestamp);
//        } else if (restaurant.tables[table_id - 1].capacity < people) {
//            System.out.printf("%s Error: the maximum number of people at the table has been exceeded.%n", timestamp);
//        } else if (restaurant.tables[table_id - 1].reservations.containsKey(slot) && restaurant.tables[table_id - 1].reservations.get(slot).containsKey(reservationDate)) {
//            System.out.printf("%s Error: the table is occupied.%n", timestamp);
//        } else {
//            if (!restaurant.tables[table_id - 1].reservations.containsKey(slot)) {
//                restaurant.tables[table_id - 1].reservations.put(slot, new HashMap<>());
//            }
//            Reservation reservation = new Reservation(reservationDate, slot, people, table_id, id);
//            restaurant.tables[table_id - 1].reservations.get(slot).put(reservationDate, reservation);
//            System.out.printf("%s %05d%n", timestamp, id);
//        }
//    }
//
//    private static void handleIssueUnspecified(int id, int reservationDate, int slot, int people, Restaurant restaurant, String timestamp, String currentDate) {
//        if (currentDate.equals(String.valueOf(reservationDate)) && isWithReservationSlot(timestamp, restaurant.slots, slot)) {
//            System.out.printf("%s Error: the current slot cannot be specified.%n", timestamp);
//        } else if (currentDate.equals(String.valueOf(reservationDate)) && isPastTime(timestamp, restaurant.slots, slot)){
//            System.out.printf("%s Error: a past time cannot be specified.%n", timestamp);
//        } else {
//            Table bestTable = null;
//            for (Table table : restaurant.tables) {
//                if (table.capacity >= people && (!table.reservations.containsKey(slot) || !table.reservations.get(slot).containsKey(reservationDate))) {
//                    if (bestTable == null || table.capacity < bestTable.capacity || (table.capacity == bestTable.capacity && table.tableNumber < bestTable.tableNumber)) {
//                        bestTable = table;
//                    }
//                }
//            }
//            if (bestTable != null) {
//                bestTable.reservations.put(slot, new HashMap<>());
//                Reservation reservation = new Reservation(reservationDate, slot, people, bestTable.reservations.size(), id);
//                bestTable.reservations.get(slot).put(reservationDate, reservation);
//                System.out.printf("%s %05d %d%n", timestamp, id, bestTable.tableNumber);
//            }else{
//                System.out.printf("%s Error: no available table is found.%n", timestamp);
//            }
//        }
//    }
//
//    private static void handleCancel(int id, Restaurant restaurant, String timestamp, String currentDate) {
//        boolean found = false;
//        for (Table table : restaurant.tables) {
//            for (Map<Integer, Reservation> slotReservations : table.reservations.values()) {
//                for (Reservation reservation : slotReservations.values()) {
//                    if (reservation.id == id) {
//                        if (Integer.parseInt(currentDate) < reservation.reservationDate){
//                            slotReservations.remove(reservation.reservationDate);
//                            System.out.printf("%s Canceled %05d%n", timestamp, id);
//                            adjustReservations(restaurant, reservation.reservationDate, reservation.slot);
//                            found = true;
//                            break;
//                        } else if(Integer.parseInt(currentDate) >= reservation.reservationDate){
//                            System.out.printf("%s Error: you must cancel at least one day in advance.%n", timestamp);
//                            found = true;
//                            break;
//                        }
//                    }
//                }
//                if (found) break;
//            }
//            if (found) break;
//        }
//        if (!found) {
//            System.out.printf("%s Error: no such ticket is found.%n", timestamp);
//        }
//    }
//
//    private static void adjustReservations(Restaurant restaurant, int date, int slot) {
//        List<Reservation> freeReservations = new ArrayList<>();
//        for (Table table : restaurant.tables) {
//            Map<Integer, Reservation> slotReservations = table.reservations.get(slot);
//            if (slotReservations != null) {
//                for (Reservation res : slotReservations.values()) {
//                    if (res.reservationDate == date) {
//                        freeReservations.add(res);
//                    }
//                }
//                slotReservations.clear();
//            }
//        }
//
//        // Sort reservations: more people first, then by ID
//        freeReservations.sort(Comparator.comparingInt((Reservation res) -> res.people).reversed().thenComparingInt(res -> res.id));
//
//        for (Reservation res : freeReservations) {
//            handleIssueUnspecified(res.id, res.reservationDate, res.slot, res.people, restaurant, "", "");
//        }
//    }
//
//    private static boolean isWithReservationSlot(String timestamp, List<String> slots, int slot){
//        String slotTime = slots.get(slot - 1);
//        String[] slotParts = slotTime.split("-");
//        return timestamp.compareTo(slotParts[0]) >= 0 && timestamp.compareTo(slotParts[1]) <= 0;
//    }
//
//    private static boolean isPastTime(String timestamp, List<String> slots, int slot){
//        String[] timeParts = timestamp.split(" ");
//        String endingTime = timeParts[1];
//        String[] slotParts = slots.get(slot -1 ).split("-");
//        return endingTime.compareTo(slotParts[1]) >= 0;
//    }
//}
//
//
//class Table {
//    int capacity;
//    int tableNumber;
//    Map<Integer, Map<Integer, Reservation>> reservations;
//
//    Table(int capacity, int tableNumber) {
//        this.capacity = capacity;
//        this.tableNumber = tableNumber;
//        this.reservations = new HashMap<>();
//    }
//}
//
//class Reservation {
//    int reservationDate;
//    int slot;
//    int people;
//    int table_id;
//    int id;
//
//    Reservation(int reservationDate, int slot, int people, int table_id, int id) {
//        this.reservationDate = reservationDate;
//        this.slot = slot;
//        this.people = people;
//        this.table_id = table_id;
//        this.id = id;
//    }
//}
//
//class Restaurant {
//    int n_tables;
//    Table[] tables;
//    String runningHours;
//    int k_slots;
//    List<String> slots;
//
//    Restaurant(int n_tables, int[] capacities, String runningHours, int k_slots, List<String> slots) {
//        this.n_tables = n_tables;
//        this.tables = new Table[n_tables];
//        for (int i = 0; i < n_tables; i++) {
//            this.tables[i] = new Table(capacities[i], i + 1);
//        }
//        this.runningHours = runningHours;
//        this.k_slots = k_slots;
//        this.slots = slots;
//    }
//}
