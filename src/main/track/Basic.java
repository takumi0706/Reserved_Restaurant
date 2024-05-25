package main.track;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class Basic {
    public static void main(String[] args) {
        String[] input = getStdin();
        int n_tables = Integer.parseInt(input[0].trim());
        int[] capacity = Arrays.stream(input[1].trim().split(" ")).mapToInt(Integer::parseInt).toArray();
        String runningHours = input[2].split(" ")[0];
        int k_slots = Integer.parseInt(input[2].split(" ")[1]);
        List<String> slots = Arrays.asList(input[2].split(" ")).subList(2, 2 + k_slots);

        Restaurant restaurant = new Restaurant(n_tables, capacity, runningHours, k_slots, slots);

        for (int i = 3; i < input.length; i++) {
            String[] query = input[i].split(" ");
            String timestamp = query[0] + " " + query[1];
            String command = query[2];
            int slot;

            switch (command) {
                case "time":
                    slot = Integer.parseInt(query[3]);
                    Handling_time_command(slot, n_tables, restaurant, timestamp);
                    break;

                case "issue-specified":
                    int id = Integer.parseInt(query[3]);
                    int date = Integer.parseInt(query[4]);
                    slot = Integer.parseInt(query[5]);
                    int people = Integer.parseInt(query[6]);
                    int table_id = Integer.parseInt(query[7]);

                    Handling_issue_specified(id, date, slot, people, table_id, restaurant, timestamp);
                    break;

                default:
                    break;
            }
        }
        System.exit(0);
    }

    private static String[] getStdin() {
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> lines = new ArrayList<>();
        while (scanner.hasNext()) {
            lines.add(scanner.nextLine());
        }
        return lines.toArray(new String[lines.size()]);
    }

    private static void Handling_time_command(int slot, int n_tables, Restaurant restaurant, String timestamp){
        if (slot > 1) {
            // Remove previous slot reservations
            for (Table table : restaurant.tables) {
                table.reservations.remove(slot - 1);
            }
        }
        if (slot <= restaurant.k_slots) {
            // Print current slot reservations
            for (int t = 0; t < n_tables; t++) {
                Map<Integer, Reservation> slotReservations = restaurant.tables[t].reservations.get(slot);
                if (slotReservations != null) {
                    for (Reservation res : slotReservations.values()) {
                        System.out.printf("%s table %d = %05d%n", timestamp, t + 1, res.id);
                    }
                }
            }
        }
    }

    private static void Handling_issue_specified(int id, int date, int slot, int people, int table_id, Restaurant restaurant, String timestamp){
        if(isWithReservationSlot(timestamp, restaurant.slots, slot)){
            System.out.printf("%s Error: the current slot cannot be specified.\n", timestamp);
        }else if(isPastTime(timestamp, restaurant.slots)){
            System.out.printf("%s Error: a past time cannot be specified.\n", timestamp);
        }else if(restaurant.tables[table_id - 1].capacity < people){
            System.out.printf("%s Error: the maximum number of people at the table has been exceeded.\n", timestamp);
        }else if(restaurant.tables[table_id - 1].reservations.containsKey(slot) && restaurant.tables[table_id - 1].reservations.get(slot).containsKey(date)){
            System.out.printf("%s Error: the table is occupied.\n", timestamp);
        }else{
            if (!restaurant.tables[table_id - 1].reservations.containsKey(slot)) {
                restaurant.tables[table_id - 1].reservations.put(slot, new HashMap<>());
            }
            Reservation reservation = new Reservation(date, slot, people, table_id, id);
            restaurant.tables[table_id - 1].reservations.get(slot).put(date, reservation);
            System.out.printf("%s %05d%n", timestamp, id);
        }
    }

    private static boolean isWithReservationSlot(String timestamp, List<String> slots, int slot){
        String slotTime = slots.get(slot - 1);
        String[] slotParts = slotTime.split("-");
        return timestamp.compareTo(slotParts[0]) >= 0 && timestamp.compareTo(slotParts[1]) <= 0;
    }

    private static boolean isPastTime(String timestamp, List<String> slots){
        String[] timeParts = timestamp.split(" ");
        String time = timeParts[1];
        for(String slot : slots){
            String[] slotParts = slot.split("-");
            if(time.compareTo(slotParts[0]) >= 0){
                return true;
            }
        }
        return false;
    }
}


class Table {
    int capacity;
    //    Map<slot, Map<date, Reservation>>
    Map<Integer, Map<Integer, Reservation>> reservations;

    Table(int capacity) {
        this.capacity = capacity;
        this.reservations = new HashMap<>();
    }
}

class Reservation {
    int date;
    int slot;
    int people;
    int table_id;
    int id;

    Reservation(int date, int slot, int people, int table_id, int id) {
        this.date = date;
        this.slot = slot;
        this.people = people;
        this.table_id = table_id;
        this.id = id;
    }
}

class Restaurant {
    int n_tables;
    Table[] tables;
    String runningHours;
    int k_slots;
    List<String> slots;

    Restaurant(int n_tables, int[] capacities, String runningHours, int k_slots, List<String> slots) {
        this.n_tables = n_tables;
        this.tables = new Table[n_tables];
        for (int i = 0; i < n_tables; i++) {
            this.tables[i] = new Table(capacities[i]);
        }
        this.runningHours = runningHours;
        this.k_slots = k_slots;
        this.slots = slots;
    }
}
