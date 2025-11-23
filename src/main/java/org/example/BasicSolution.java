package org.example;

import java.util.ArrayList;
import java.util.HashSet;

public class BasicSolution {

    public static ArrayList<ArrayList<Integer>> northwestCornerMethod(Task task) {
        int n = task.getProducers().size();
        int m = task.getConsumers().size();

        Pair<Integer, Integer> target = new Pair<>(0, 0);
        ArrayList<ArrayList<Integer>> result = Matrix.empty(n, m, null);
        ArrayList<Integer> consumers = new ArrayList<>(task.getConsumers());
        ArrayList<Integer> producers = new ArrayList<>(task.getProducers());
        ArrayList<ArrayList<Integer>> costs = task.getCosts();
        HashSet<Pair<Integer, Integer>> usedElements = new HashSet<>();

        while (target.first < n && target.second < m) {
            Integer minValue = Math.min(
                    producers.get(target.first),
                    consumers.get(target.second)
            );
            result.get(target.first).set(target.second, minValue);
            usedElements.add(new Pair<>(target.first, target.second));

            producers.set(target.first, producers.get(target.first) - minValue);
            consumers.set(target.second, consumers.get(target.second) - minValue);

            if (producers.get(target.first) == 0 && consumers.get(target.second) == 0) {
                if (target.first < 3) {
                    ++target.first;
                } else {
                    ++target.second;
                }
            } else if (producers.get(target.first) == 0) {
                ++target.first;
            } else {
                ++target.second;
            }
        }

        if (usedElements.size() < n + m - 1) {
            while (usedElements.size() < n + m - 1) {
                int minCost = Integer.MAX_VALUE;
                Pair<Integer, Integer> minElement = null;

                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < m; j++) {
                        if (costs.get(i).get(j) < minCost && !usedElements.contains(new Pair<>(i, j))) {
                            minCost = costs.get(i).get(j);
                            minElement = new Pair<>(i, j);
                        }
                    }
                }

                usedElements.add(minElement);
                result.get(minElement.first).set(minElement.second, 0);
            }
        }

        return result;
    }

    public static ArrayList<ArrayList<Integer>> minimalElementMethod(Task task) {
        int n = 4;
        int m = 6;

        ArrayList<ArrayList<Integer>> result = Matrix.empty(4, 6, null);
        ArrayList<Integer> consumers = new ArrayList<>(task.getConsumers());
        ArrayList<Integer> producers = new ArrayList<>(task.getProducers());
        ArrayList<ArrayList<Integer>> costs = task.getCosts();
        HashSet<Pair<Integer, Integer>> usedElements = new HashSet<>();

        boolean[] rowExhausted = new boolean[4];
        boolean[] colExhausted = new boolean[6];

        while (true) {
            int minCost = Integer.MAX_VALUE;
            Pair<Integer, Integer> minElement = null;

            for (int i = 0; i < 4; i++) {
                if (rowExhausted[i]) continue;
                for (int j = 0; j < 6; j++) {
                    if (colExhausted[j]) continue;
                    if (costs.get(i).get(j) < minCost) {
                        minCost = costs.get(i).get(j);
                        minElement = new Pair<>(i, j);
                    }
                }
            }

            if (minElement == null) {
                break;
            }
            usedElements.add(minElement);

            int allocation = Math.min(producers.get(minElement.first), consumers.get(minElement.second));
            result.get(minElement.first).set(minElement.second, allocation);

            producers.set(minElement.first, producers.get(minElement.first) - allocation);
            consumers.set(minElement.second, consumers.get(minElement.second) - allocation);

            if (producers.get(minElement.first) == 0) {
                rowExhausted[minElement.first] = true;
            }
            if (consumers.get(minElement.second) == 0) {
                colExhausted[minElement.second] = true;
            }
        }

        if (usedElements.size() < n + m - 1) {
            while (usedElements.size() < n + m - 1) {
                int minCost = Integer.MAX_VALUE;
                Pair<Integer, Integer> minElement = null;

                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 6; j++) {
                        if (costs.get(i).get(j) < minCost && !usedElements.contains(new Pair<>(i, j))) {
                            minCost = costs.get(i).get(j);
                            minElement = new Pair<>(i, j);
                        }
                    }
                }

                usedElements.add(minElement);

                result.get(minElement.first).set(minElement.second, 0);
            }
        }

        return result;
    }
}
