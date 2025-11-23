package org.example;

import java.util.ArrayList;
import java.util.HashSet;

public class PotentialMethod {

    private static void log(Object message) {
        System.out.println(message);
    }

    public static ArrayList<ArrayList<Integer>> optimize(ArrayList<ArrayList<Integer>> basicSolution, Task task) {

        Pair<ArrayList<Integer>, ArrayList<Integer>> uv = initPotentials(basicSolution, task);
        ArrayList<Integer> u = uv.first;
        ArrayList<Integer> v = uv.second;
        ArrayList<ArrayList<Boolean>> basic = Matrix.empty(task.getProducers().size(), task.getConsumers().size(), false);

        for (int i = 0; i < task.getProducers().size(); i++) {
            for (int j = 0; j < task.getConsumers().size(); j++) {
                basic.get(i).set(j, basicSolution.get(i).get(j) != null);
            }
        }

        ArrayList<ArrayList<Integer>> reducedEstimates = getReducedEstimates(task.getCosts(), u, v);
        Pair<Integer, Integer> mostNegativeEstimate = Matrix.minElement(reducedEstimates);
        Integer mostNegativeEstimateValue = reducedEstimates.get(mostNegativeEstimate.first).get(mostNegativeEstimate.second);

        if (mostNegativeEstimateValue < 0) {
            ArrayList<Pair<Integer, Integer>> cycle = findCycle(basic, mostNegativeEstimate);
            log(cycle);
        } else {
            log("Отрицательных потенциалов нет.");
        }

        return null;
    }

    private static Pair<ArrayList<Integer>, ArrayList<Integer>> initPotentials(
            ArrayList<ArrayList<Integer>> basicSolution,
            Task task
    ) {
        ArrayList<Integer> u = new ArrayList<>(task.getProducers().size());
        ArrayList<Integer> v = new ArrayList<>(task.getConsumers().size());
        for (int i = 0; i < task.getProducers().size(); i++) {
            u.add(null);
        }
        for (int i = 0; i < task.getConsumers().size(); i++) {
            v.add(null);
        }

        u.set(0, 0);

        HashSet<Pair<Integer, Integer>> iterationItems = new HashSet<>();
        int initialized = u.size() + v.size() - 1;
        for (int j = 0; j < task.getConsumers().size(); j++) {
            if (basicSolution.get(0).get(j) != null) {
                iterationItems.add(new Pair<>(0, j));
                v.set(j, task.getCosts().get(0).get(j));
                initialized -= 1;
            }
        }
        boolean horizontal = false;

        while (initialized != 0) {
            HashSet<Pair<Integer, Integer>> newIterationItems = new HashSet<>();
            for (Pair<Integer, Integer> iterationItem : iterationItems) {
                if (horizontal) {
                    int i =  iterationItem.first;
                    for (int j = 0; j < v.size(); j++) {
                        if (basicSolution.get(i).get(j) != null) {
                            if (!iterationItems.contains(new Pair<>(i, j))) {
                                newIterationItems.add(new Pair<>(i, j));
                                initialized -= 1;
                            }

                            v.set(j, task.getCosts().get(i).get(j) - u.get(i));
                        }
                    }
                } else {
                    int j =  iterationItem.second;
                    for (int i = 0; i < u.size(); i++) {
                        if (basicSolution.get(i).get(j) != null) {
                            if (!iterationItems.contains(new Pair<>(i, j))) {
                                newIterationItems.add(new Pair<>(i, j));
                                initialized -= 1;
                            }
                            u.set(i, task.getCosts().get(i).get(j) - v.get(j));
                        }
                    }
                }
            }

            horizontal = !horizontal;
            iterationItems = newIterationItems;
        }

        return new Pair<>(u, v);
    }

    private static ArrayList<ArrayList<Integer>> getReducedEstimates(
            ArrayList<ArrayList<Integer>> costs,
            ArrayList<Integer> u,
            ArrayList<Integer> v
    ) {
        ArrayList<ArrayList<Integer>> result = Matrix.empty(costs.size(), costs.get(0).size(), 0);
        for (int i = 0; i < costs.size(); i++) {
            for (int j = 0; j < costs.get(i).size(); j++) {
                result.get(i).set(j, costs.get(i).get(j) - u.get(i) - v.get(j));
            }
        }
        return result;
    }


    public static ArrayList<Pair<Integer, Integer>> findCycle(
            ArrayList<ArrayList<Boolean>> basic,
            Pair<Integer, Integer> start
    ) {
        // Начинаем с небазисной клетки и пробуем найти цикл
        // Пробуем начать с горизонтального движения
        ArrayList<Pair<Integer, Integer>> cycle = findCycleInDirection(basic, start, true);
        if (cycle != null) {
            return cycle;
        }

        // Если не нашли, пробуем начать с вертикального движения
        cycle = findCycleInDirection(basic, start, false);
        return cycle;
    }

    private static ArrayList<Pair<Integer, Integer>> findCycleInDirection(
            ArrayList<ArrayList<Boolean>> basic,
            Pair<Integer, Integer> start,
            boolean startHorizontal
    ) {
        ArrayList<Pair<Integer, Integer>> path = new ArrayList<>();
        path.add(start); // Добавляем стартовую (небазисную) клетку

        HashSet<Pair<Integer, Integer>> visited = new HashSet<>();
        visited.add(start);

        if (dfs(start, start, basic, path, visited, startHorizontal)) {
            return new ArrayList<>(path);
        }

        return null;
    }

    private static boolean dfs(
            Pair<Integer, Integer> current,
            Pair<Integer, Integer> start,
            ArrayList<ArrayList<Boolean>> basic,
            ArrayList<Pair<Integer, Integer>> path,
            HashSet<Pair<Integer, Integer>> visited,
            boolean horizontalMove
    ) {
        int r = current.first;
        int c = current.second;

        if (horizontalMove) {
            // Ищем базисные клетки в строке r
            for (int j = 0; j < basic.get(0).size(); j++) {
                if (j == c) continue; // Пропускаем текущую клетку

                Pair<Integer, Integer> next = new Pair<>(r, j);

                // Проверяем, можем ли мы вернуться к старту
                if (next.equals(start) && path.size() >= 4) {
                    // Нашли цикл!
                    return true;
                }

                // Можем посетить только базисные клетки (кроме старта)
                if (!basic.get(r).get(j)) continue;

                if (!visited.contains(next)) {
                    path.add(next);
                    visited.add(next);

                    // Следующий шаг - вертикальный
                    if (dfs(next, start, basic, path, visited, false)) {
                        return true;
                    }

                    // Откатываем изменения
                    path.remove(path.size() - 1);
                    visited.remove(next);
                }
            }

        } else { // vertical move
            // Ищем базисные клетки в столбце c
            for (int i = 0; i < basic.size(); i++) {
                if (i == r) continue; // Пропускаем текущую клетку

                Pair<Integer, Integer> next = new Pair<>(i, c);

                // Проверяем, можем ли мы вернуться к старту
                if (next.equals(start) && path.size() >= 4) {
                    // Нашли цикл!
                    return true;
                }

                // Можем посетить только базисные клетки (кроме старта)
                if (!basic.get(i).get(c)) continue;

                if (!visited.contains(next)) {
                    path.add(next);
                    visited.add(next);

                    // Следующий шаг - горизонтальный
                    if (dfs(next, start, basic, path, visited, true)) {
                        return true;
                    }

                    // Откатываем изменения
                    path.remove(path.size() - 1);
                    visited.remove(next);
                }
            }
        }

        return false;
    }

}
