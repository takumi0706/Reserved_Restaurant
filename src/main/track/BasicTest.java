package main.track;

import java.util.*;

public class BasicTest {

    static class Reservation {
        int date;
        int slot;
        int people;
        int table;
        String id;

        Reservation(int date, int slot, int people, int table, String id) {
            this.date = date;
            this.slot = slot;
            this.people = people;
            this.table = table;
            this.id = id;
        }
    }

    static class Table {
        int capacity;
        Map<Integer, Map<Integer, Reservation>> reservations;

        Table(int capacity) {
            this.capacity = capacity;
            this.reservations = new HashMap<>();
        }
    }

    static class Restaurant {
        int n;
        Table[] tables;
        String openingHours;
        int k;
        List<String> slots;

        Restaurant(int n, int[] capacities, String openingHours, int k, List<String> slots) {
            this.n = n;
            this.tables = new Table[n];
            for (int i = 0; i < n; i++) {
                this.tables[i] = new Table(capacities[i]);
            }
            this.openingHours = openingHours;
            this.k = k;
            this.slots = slots;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> input = new ArrayList<>();

        while (scanner.hasNext()) {
            input.add(scanner.nextLine());
        }

        int n = Integer.parseInt(input.get(0).trim());
        int[] capacities = Arrays.stream(input.get(1).trim().split(" ")).mapToInt(Integer::parseInt).toArray();
        String openingHours = input.get(2).split(" ")[0];
        int k = Integer.parseInt(input.get(2).split(" ")[1]);
        List<String> slots = Arrays.asList(input.get(2).split(" ")).subList(2, 2 + k);

        Restaurant restaurant = new Restaurant(n, capacities, openingHours, k, slots);
        Map<String, Reservation> reservationMap = new HashMap<>();

        for (int i = 3; i < input.size(); i++) {
            String[] parts = input.get(i).split(" ");
            String timestamp = parts[0] + " " + parts[1];
            String command = parts[2];

            if (command.equals("time")) {
                int slot = Integer.parseInt(parts[3]);
                if (slot > 1) {
                    for (Table table : restaurant.tables) {
                        table.reservations.remove(slot - 1);
                    }
                }
                if (slot <= k) {
                    for (int t = 0; t < n; t++) {
                        Map<Integer, Reservation> slotReservations = restaurant.tables[t].reservations.get(slot);
                        if (slotReservations != null) {
                            for (Reservation res : slotReservations.values()) {
                                System.out.printf("%s table %d = %05d%n", timestamp, t + 1, Integer.parseInt(res.id));
                            }
                        }
                    }
                }
            } else if (command.equals("issue-specified")) {
                String reservationId = parts[3];
                int date = Integer.parseInt(parts[4]);
                int slot = Integer.parseInt(parts[5]);
                int people = Integer.parseInt(parts[6]);
                int table = Integer.parseInt(parts[7]);

                if (restaurant.tables[table - 1].capacity < people) {
                    System.out.printf("%s Error: the maximum number of people at the table has been exceeded.%n", timestamp);
                } else if (restaurant.tables[table - 1].reservations.containsKey(slot) &&
                        restaurant.tables[table - 1].reservations.get(slot).containsKey(date)) {
                    System.out.printf("%s Error: the table is occupied.%n", timestamp);
                } else {
                    if (!restaurant.tables[table - 1].reservations.containsKey(slot)) {
                        restaurant.tables[table - 1].reservations.put(slot, new HashMap<>());
                    }
                    Reservation reservation = new Reservation(date, slot, people, table, reservationId);
                    restaurant.tables[table - 1].reservations.get(slot).put(date, reservation);
                    reservationMap.put(reservationId, reservation);
                    System.out.printf("%s %s%n", timestamp, reservationId);
                }
            }
        }
    }
}
