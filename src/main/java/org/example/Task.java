package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Task {

    private ArrayList<ArrayList<Integer>> costs;
    private ArrayList<Integer> consumers;
    private ArrayList<Integer> producers;

    public Task(ArrayList<ArrayList<Integer>> costs, ArrayList<Integer> consumers, ArrayList<Integer> produces) {
        this.costs = costs;
        this.consumers = consumers;
        this.producers = produces;
    }

    public Task(Task task) {
        this.costs = deepCopy(task.costs);
        this.consumers = new ArrayList<>(task.consumers);
        this.producers = new ArrayList<>(task.producers);
    }

    public static Task gen () {
        ArrayList<Integer> consumers = generateConsumers(6);
        ArrayList<Integer> producers = generateProducers(4);

        int sumConsumers = consumers.stream().mapToInt(Integer::intValue).sum();
        int sumProducers = producers.stream().mapToInt(Integer::intValue).sum();

        if (sumProducers > sumConsumers) {
            consumers.set(consumers.size() - 1, consumers.get(consumers.size() - 1) + (sumProducers - sumConsumers));
        } else if (sumConsumers > sumProducers) {
            producers.set(producers.size() - 1, producers.get(producers.size() - 1) + (sumConsumers - sumProducers));
        }

        return new Task(
                generateCosts(4, 6),
                consumers,
                producers
        );
    }

    public static Task example() {
        ArrayList<Integer> producers = new ArrayList<>(Arrays.asList(80, 120, 150, 60));
        ArrayList<Integer> consumers = new ArrayList<>(Arrays.asList(50, 70, 40, 90, 100, 60));
        ArrayList<ArrayList<Integer>> costs = new ArrayList<>();

        costs.add(new ArrayList<>(Arrays.asList(4, 8, 5, 9, 3, 7)));
        costs.add(new ArrayList<>(Arrays.asList(6, 3, 9, 2, 8, 4)));
        costs.add(new ArrayList<>(Arrays.asList(5, 7, 6, 4, 9, 1)));
        costs.add(new ArrayList<>(Arrays.asList(8, 2, 4, 6, 5, 3)));
        return new Task(costs, consumers, producers);
    }

    private static ArrayList<ArrayList<Integer>> generateCosts(Integer n, Integer m) {
        ArrayList<ArrayList<Integer>> costs = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            costs.add(generateArray(m, 1, 9));
        }
        return costs;
    }

    private static ArrayList<Integer> generateConsumers(Integer length) {
        return generateArray(length, 10, 200);
    }

    private static ArrayList<Integer> generateProducers(Integer length) {
        return generateArray(length, 10, 200);
    }

    private static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private static ArrayList<Integer> generateArray(Integer length, int min, int max) {
        ArrayList<Integer> arr = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            arr.add(randInt(min, max));
        }
        return arr;
    }

    @Override
    public String toString() {
        int n = producers.size();
        int m = consumers.size();

        int cellWidth = 8;

        String horizontal = repeat("─", cellWidth);
        String topSep = "┬";
        String midSep = "┼";
        String botSep = "┴";

        java.util.function.BiFunction<String, Integer, String> center = (text, width) -> {
            int len = text.length();
            int spaces = width - len;
            int left = spaces / 2;
            int right = spaces - left;
            return repeat(" ", left) + text + repeat(" ", right);
        };

        StringBuilder sb = new StringBuilder();

        sb.append("┌").append(horizontal);
        for (int j = 0; j < m; j++) sb.append(topSep).append(horizontal);
        sb.append(topSep).append(horizontal).append("┐\n");

        sb.append("│").append(center.apply("", cellWidth));
        for (int j = 0; j < m; j++) {
            sb.append("│").append(center.apply("C" + (j + 1), cellWidth));
        }
        sb.append("│").append(center.apply("Supply", cellWidth)).append("│\n");

        sb.append("├").append(horizontal);
        for (int j = 0; j < m; j++) sb.append(midSep).append(horizontal);
        sb.append(midSep).append(horizontal).append("┤\n");

        for (int i = 0; i < n; i++) {
            sb.append("│").append(center.apply("P" + (i + 1), cellWidth));

            for (int j = 0; j < m; j++) {
                sb.append("│").append(center.apply(String.valueOf(costs.get(i).get(j)), cellWidth));
            }
            sb.append("│").append(center.apply(String.valueOf(producers.get(i)), cellWidth)).append("│\n");

            if (i < n - 1) {
                sb.append("├").append(horizontal);
                for (int j = 0; j < m; j++) sb.append(midSep).append(horizontal);
                sb.append(midSep).append(horizontal).append("┤\n");
            }
        }

        sb.append("├").append(horizontal);
        for (int j = 0; j < m; j++) sb.append(midSep).append(horizontal);
        sb.append(midSep).append(horizontal).append("┤\n");

        sb.append("│").append(center.apply("Demand", cellWidth));
        for (int j = 0; j < m; j++) {
            sb.append("│").append(center.apply(String.valueOf(consumers.get(j)), cellWidth));
        }
        sb.append("│").append(center.apply("", cellWidth)).append("│\n");

        sb.append("└").append(horizontal);
        for (int j = 0; j < m; j++) sb.append(botSep).append(horizontal);
        sb.append(botSep).append(horizontal).append("┘");

        return sb.toString();
    }

    private static String repeat(String s, int times) {
        return s.repeat(times);
    }

    private ArrayList<ArrayList<Integer>> deepCopy(ArrayList<ArrayList<Integer>> list) {
        ArrayList<ArrayList<Integer>> copy = new ArrayList<>(list.size());
        for (ArrayList<Integer> integers : list) {
            copy.add(new ArrayList<>(integers));
        }
        return copy;
    }

    public ArrayList<ArrayList<Integer>> getCosts() {
        return costs;
    }

    public ArrayList<Integer> getConsumers() {
        return consumers;
    }

    public ArrayList<Integer> getProducers() {
        return producers;
    }

}
