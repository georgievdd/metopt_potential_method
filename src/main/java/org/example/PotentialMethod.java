package org.example;

import java.util.ArrayList;
import java.util.HashSet;

public class PotentialMethod {

    private static int calculateCost(ArrayList<ArrayList<Integer>> solution, ArrayList<ArrayList<Integer>> costs) {
        int totalCost = 0;
        for (int i = 0; i < solution.size(); i++) {
            for (int j = 0; j < solution.get(i).size(); j++) {
                Integer value = solution.get(i).get(j);
                if (value != null && value > 0) {
                    totalCost += value * costs.get(i).get(j);
                }
            }
        }
        return totalCost;
    }

    public static ArrayList<ArrayList<Integer>> optimize(ArrayList<ArrayList<Integer>> basicSolution, Task task) {
        Logger.header("Метод потенциалов");

        Logger.section("Начальное базисное решение:");
        Logger.printSolutionMatrix(basicSolution, "Распределение поставок");
        Logger.printBasicCells(basicSolution);
        Logger.checkBasicCount(basicSolution, task.getProducers().size() + task.getConsumers().size() - 1);
        int initialCost = Logger.calculateCost(basicSolution, task.getCosts());
        Logger.info("Стоимость начального решения: " + initialCost);

        int iteration = 0;
        int maxIterations = 100;

        while (iteration < maxIterations) {
            Logger.subheader("Итерация " + (iteration + 1));

            Pair<ArrayList<Integer>, ArrayList<Integer>> uv = initPotentials(basicSolution, task);
            ArrayList<Integer> u = uv.first;
            ArrayList<Integer> v = uv.second;

            Logger.section("Шаг 1: Вычисление потенциалов");
            Logger.printVector(u, "u");
            Logger.printVector(v, "v");

            ArrayList<ArrayList<Boolean>> basic = Matrix.empty(task.getProducers().size(), task.getConsumers().size(), false);
            for (int i = 0; i < task.getProducers().size(); i++) {
                for (int j = 0; j < task.getConsumers().size(); j++) {
                    basic.get(i).set(j, basicSolution.get(i).get(j) != null);
                }
            }

            Logger.section("Шаг 2: Вычисление редуцированных оценок");
            ArrayList<ArrayList<Integer>> reducedEstimates = getReducedEstimates(task.getCosts(), u, v);
            Logger.printSolutionMatrix(reducedEstimates, "Редуцированные оценки Δ[i,j] = c[i,j] - u[i] - v[j]");

            Pair<Integer, Integer> mostNegativeEstimate = Matrix.minElement(reducedEstimates);
            Integer mostNegativeEstimateValue = reducedEstimates.get(mostNegativeEstimate.first).get(mostNegativeEstimate.second);

            Logger.section("Шаг 3: Проверка критерия оптимальности");
            Logger.checkOptimality(reducedEstimates);

            if (mostNegativeEstimateValue >= 0) {
                Logger.header("Решение оптимально!");
                Logger.printSolutionMatrix(basicSolution, "Оптимальное распределение");
                int finalCost = Logger.calculateCost(basicSolution, task.getCosts());
                Logger.info("Стоимость оптимального решения: " + finalCost);
                Logger.info("Улучшение: " + (initialCost - finalCost) + " единиц");
                Logger.info("Количество итераций: " + iteration);
                break;
            }

            Logger.section("Шаг 4: Поиск улучшающего цикла");
            Logger.info("Входящая клетка: (" + (mostNegativeEstimate.first + 1) + "," + (mostNegativeEstimate.second + 1) + ") с оценкой " + mostNegativeEstimateValue);

            ArrayList<Pair<Integer, Integer>> cycle = findCycle(basic, mostNegativeEstimate);

            if (cycle == null) {
                Logger.warning("Цикл не найден! Алгоритм остановлен.");
                break;
            }

            Logger.printCycle(cycle);

            Logger.section("Шаг 5: Определение θ и перераспределение");
            Pair<Integer, Integer> thetaAndLeaving = findThetaAndLeavingCell(cycle, basicSolution);
            int theta = thetaAndLeaving.first;
            int leavingIndex = thetaAndLeaving.second;

            Logger.info("θ = " + theta);
            if (leavingIndex >= 0) {
                Pair<Integer, Integer> leavingCell = cycle.get(leavingIndex);
                Logger.info("Выходящая клетка: (" + (leavingCell.first + 1) + "," + (leavingCell.second + 1) + ")");
            }

            redistributeAlongCycle(cycle, basicSolution, theta, leavingIndex);

            Logger.section("Шаг 6: Новое базисное решение");
            Logger.printSolutionMatrix(basicSolution, "Распределение после перераспределения");
            int newCost = Logger.calculateCost(basicSolution, task.getCosts());
            Logger.info("Новая стоимость: " + newCost + " (улучшение: " + (initialCost - newCost) + ")");

            iteration++;
        }

        if (iteration >= maxIterations) {
            Logger.warning("Достигнуто максимальное количество итераций: " + maxIterations);
        }

        return basicSolution;
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
        initialized -= 1;

        for (int j = 0; j < task.getConsumers().size(); j++) {
            if (basicSolution.get(0).get(j) != null) {
                iterationItems.add(new Pair<>(0, j));
                v.set(j, task.getCosts().get(0).get(j));
                initialized -= 1;
            }
        }
        boolean horizontal = false;
        int stuckCounter = 0;
        int maxStuckIterations = 10;

        while (initialized != 0 && stuckCounter < maxStuckIterations) {
            HashSet<Pair<Integer, Integer>> newIterationItems = new HashSet<>();
            for (Pair<Integer, Integer> iterationItem : iterationItems) {
                if (horizontal) {
                    int i =  iterationItem.first;
                    for (int j = 0; j < v.size(); j++) {
                        if (basicSolution.get(i).get(j) != null) {
                            if (v.get(j) == null) {
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
                            if (u.get(i) == null) {
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

            if (newIterationItems.isEmpty()) {
                for (int i = 0; i < u.size(); i++) {
                    if (u.get(i) == null) {
                        for (int j = 0; j < v.size(); j++) {
                            if (basicSolution.get(i).get(j) != null && v.get(j) != null) {
                                u.set(i, task.getCosts().get(i).get(j) - v.get(j));
                                newIterationItems.add(new Pair<>(i, j));
                                initialized -= 1;
                                break;
                            }
                        }
                    }
                }

                for (int j = 0; j < v.size(); j++) {
                    if (v.get(j) == null) {
                        for (int i = 0; i < u.size(); i++) {
                            if (basicSolution.get(i).get(j) != null && u.get(i) != null) {
                                v.set(j, task.getCosts().get(i).get(j) - u.get(i));
                                newIterationItems.add(new Pair<>(i, j));
                                initialized -= 1;
                                break;
                            }
                        }
                    }
                }

                iterationItems = newIterationItems;
                if (newIterationItems.isEmpty()) {
                    stuckCounter++;
                }
            }
        }

        for (int i = 0; i < u.size(); i++) {
            if (u.get(i) == null) u.set(i, 0);
        }
        for (int j = 0; j < v.size(); j++) {
            if (v.get(j) == null) v.set(j, 0);
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
                Integer uVal = u.get(i) != null ? u.get(i) : 0;
                Integer vVal = v.get(j) != null ? v.get(j) : 0;
                result.get(i).set(j, costs.get(i).get(j) - uVal - vVal);
            }
        }
        return result;
    }

    private static Pair<Integer, Integer> findThetaAndLeavingCell(
            ArrayList<Pair<Integer, Integer>> cycle,
            ArrayList<ArrayList<Integer>> basicSolution
    ) {
        int theta = Integer.MAX_VALUE;
        int leavingIndex = -1;

        for (int i = 1; i < cycle.size(); i += 2) {
            Pair<Integer, Integer> cell = cycle.get(i);
            Integer value = basicSolution.get(cell.first).get(cell.second);

            if (value != null && value < theta) {
                theta = value;
                leavingIndex = i;
            }
        }

        return new Pair<>(theta, leavingIndex);
    }

    private static void redistributeAlongCycle(
            ArrayList<Pair<Integer, Integer>> cycle,
            ArrayList<ArrayList<Integer>> basicSolution,
            int theta,
            int leavingIndex
    ) {
        for (int i = 0; i < cycle.size(); i++) {
            Pair<Integer, Integer> cell = cycle.get(i);
            int row = cell.first;
            int col = cell.second;

            Integer currentValue = basicSolution.get(row).get(col);
            if (currentValue == null) {
                currentValue = 0;
            }

            if (i % 2 == 0) {
                basicSolution.get(row).set(col, currentValue + theta);
            } else {
                int newValue = currentValue - theta;
                if (i == leavingIndex) {
                    basicSolution.get(row).set(col, null);
                } else {
                    basicSolution.get(row).set(col, newValue);
                }
            }
        }
    }

    public static ArrayList<Pair<Integer, Integer>> findCycle(
            ArrayList<ArrayList<Boolean>> basic,
            Pair<Integer, Integer> start
    ) {
        ArrayList<Pair<Integer, Integer>> cycle = findCycleInDirection(basic, start, true);
        if (cycle != null) {
            return cycle;
        }

        cycle = findCycleInDirection(basic, start, false);
        return cycle;
    }

    private static ArrayList<Pair<Integer, Integer>> findCycleInDirection(
            ArrayList<ArrayList<Boolean>> basic,
            Pair<Integer, Integer> start,
            boolean startHorizontal
    ) {
        ArrayList<Pair<Integer, Integer>> path = new ArrayList<>();
        path.add(start);

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
            for (int j = 0; j < basic.get(0).size(); j++) {
                if (j == c) continue;

                Pair<Integer, Integer> next = new Pair<>(r, j);

                if (next.equals(start) && path.size() >= 4) {
                    return true;
                }

                if (!basic.get(r).get(j)) continue;

                if (!visited.contains(next)) {
                    path.add(next);
                    visited.add(next);

                    if (dfs(next, start, basic, path, visited, false)) {
                        return true;
                    }

                    path.remove(path.size() - 1);
                    visited.remove(next);
                }
            }

        } else {
            for (int i = 0; i < basic.size(); i++) {
                if (i == r) continue;

                Pair<Integer, Integer> next = new Pair<>(i, c);

                if (next.equals(start) && path.size() >= 4) {
                    return true;
                }

                if (!basic.get(i).get(c)) continue;

                if (!visited.contains(next)) {
                    path.add(next);
                    visited.add(next);

                    if (dfs(next, start, basic, path, visited, true)) {
                        return true;
                    }

                    path.remove(path.size() - 1);
                    visited.remove(next);
                }
            }
        }

        return false;
    }

}
