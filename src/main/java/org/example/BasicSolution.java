package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class BasicSolution {

    private static boolean enableLog = false;

    public static ArrayList<ArrayList<Integer>> northwestCornerMethod(Task task) {
        Pair<Integer, Integer> target = new Pair<>(0, 0);
        ArrayList<ArrayList<Integer>> result = Matrix.empty(4, 6, 0);
        ArrayList<Integer> consumers = new ArrayList<>(task.getConsumers());
        ArrayList<Integer> producers = new ArrayList<>(task.getProducers());

        while (target.first < 4 && target.second < 6) {
            Integer minValue = Math.min(
                    producers.get(target.first),
                    consumers.get(target.second)
            );
            result.get(target.first).set(target.second, minValue);
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

        return result;
    }

    private static void log(Object arg) {
        if (enableLog) {
            System.out.println(arg);
        }
    }

    public static ArrayList<ArrayList<Integer>> minimalElementMethod(Task task) {
        log("Метод минимального элемента");
        int n = 4;
        int m = 6;

        ArrayList<ArrayList<Integer>> result = Matrix.empty(4, 6, null);
        ArrayList<Integer> consumers = new ArrayList<>(task.getConsumers());
        ArrayList<Integer> producers = new ArrayList<>(task.getProducers());
        ArrayList<ArrayList<Integer>> costs = task.getCosts();
        HashSet<Pair<Integer, Integer>> usedElements = new HashSet<>();

        boolean[] rowExhausted = new boolean[4];
        boolean[] colExhausted = new boolean[6];

        int iteration = 0;

        while (true) {
            log("Итерация " + (iteration + 1) + ".");

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
                log("Минимальный элемент не найден.");
                break;
            }
            usedElements.add(minElement);

            log("Минимальный элемент: " + minCost + ". На позиции: " + minElement);

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

            log("Таблица после первой итерации.");
            if (enableLog) {
                new Task(costs, consumers, producers).print();
            }
            ++iteration;
        }
        log("Опорных элементов: " + usedElements.size());

        if (usedElements.size() < n + m - 1) {
            log("Добираем опорные элементы");
            iteration = 0;
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
                log("Минимальный элемент: " + minCost + ". На позиции: " + minElement);

                result.get(minElement.first).set(minElement.second, 0);
            }
        }

        return result;
    }


}
