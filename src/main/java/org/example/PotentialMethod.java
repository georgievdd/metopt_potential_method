package org.example;

import java.util.ArrayList;
import java.util.HashSet;

public class PotentialMethod {

    private static void log(Object message) {
        System.out.println(message);
    }

    private static void printSolution(ArrayList<ArrayList<Integer>> solution) {
        for (int i = 0; i < solution.size(); i++) {
            for (int j = 0; j < solution.get(i).size(); j++) {
                Integer value = solution.get(i).get(j);
                if (value == null) {
                    System.out.print("    - ");
                } else {
                    System.out.printf("%5d ", value);
                }
            }
            System.out.println();
        }
    }

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
        int iteration = 0;
        int maxIterations = 100; // защита от бесконечного цикла

        // Выводим начальное решение и его стоимость
        int initialCost = calculateCost(basicSolution, task.getCosts());
        log("\n" + "=".repeat(60));
        log("НАЧАЛЬНОЕ БАЗИСНОЕ РЕШЕНИЕ");
        log("=".repeat(60));
        printSolution(basicSolution);
        log("\nСтоимость начального решения: " + initialCost);

        while (iteration < maxIterations) {
            log("\n" + "=".repeat(60));
            log("ИТЕРАЦИЯ " + (iteration + 1));
            log("=".repeat(60));

            // Вычисляем потенциалы
            Pair<ArrayList<Integer>, ArrayList<Integer>> uv = initPotentials(basicSolution, task);
            ArrayList<Integer> u = uv.first;
            ArrayList<Integer> v = uv.second;

            log("\nПотенциалы u: " + u);
            log("Потенциалы v: " + v);

            // Обновляем матрицу базисных клеток
            ArrayList<ArrayList<Boolean>> basic = Matrix.empty(task.getProducers().size(), task.getConsumers().size(), false);
            for (int i = 0; i < task.getProducers().size(); i++) {
                for (int j = 0; j < task.getConsumers().size(); j++) {
                    basic.get(i).set(j, basicSolution.get(i).get(j) != null);
                }
            }

            // Вычисляем редуцированные оценки
            ArrayList<ArrayList<Integer>> reducedEstimates = getReducedEstimates(task.getCosts(), u, v);
            log("\nРедуцированные оценки:");
            printSolution(reducedEstimates);

            // Находим минимальную оценку
            Pair<Integer, Integer> mostNegativeEstimate = Matrix.minElement(reducedEstimates);
            Integer mostNegativeEstimateValue = reducedEstimates.get(mostNegativeEstimate.first).get(mostNegativeEstimate.second);

            // Проверяем оптимальность
            if (mostNegativeEstimateValue >= 0) {
                int finalCost = calculateCost(basicSolution, task.getCosts());
                log("\n" + "=".repeat(60));
                log("РЕШЕНИЕ ОПТИМАЛЬНО!");
                log("Все редуцированные оценки >= 0");
                log("=".repeat(60));
                log("\nОПТИМАЛЬНОЕ РЕШЕНИЕ:");
                printSolution(basicSolution);
                log("\nСтоимость оптимального решения: " + finalCost);
                log("Начальная стоимость: " + initialCost);
                log("Улучшение: " + (initialCost - finalCost));
                break;
            }

            // Есть отрицательная оценка - продолжаем оптимизацию
            log("\nКлетка с минимальной оценкой: " + mostNegativeEstimate);
            log("Минимальная редуцированная оценка: " + mostNegativeEstimateValue);

            ArrayList<Pair<Integer, Integer>> cycle = findCycle(basic, mostNegativeEstimate);
            log("Найденный цикл: " + cycle);

            if (cycle == null) {
                log("\nОШИБКА: Цикл не найден!");
                break;
            }

            int theta = findTheta(cycle, basicSolution);
            log("θ (тета) = " + theta);

            if (theta == 0) {
                log("\nВНИМАНИЕ: θ = 0, вырожденный случай");
            }

            log("\nБазисное решение ДО перераспределения:");
            printSolution(basicSolution);

            redistributeAlongCycle(cycle, basicSolution, theta);

            log("\nБазисное решение ПОСЛЕ перераспределения:");
            printSolution(basicSolution);

            iteration++;
        }

        if (iteration >= maxIterations) {
            log("\nДостигнуто максимальное количество итераций: " + maxIterations);
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

    /**
     * Находит минимальное значение θ среди клеток цикла со знаком "-"
     * В цикле знаки чередуются: позиция 0 (стартовая) - "+", позиция 1 - "-", позиция 2 - "+", и т.д.
     * θ = min(значения в позициях 1, 3, 5, ... - с нечетными индексами)
     */
    private static int findTheta(
            ArrayList<Pair<Integer, Integer>> cycle,
            ArrayList<ArrayList<Integer>> basicSolution
    ) {
        int theta = Integer.MAX_VALUE;

        // Проходим по клеткам со знаком "-" (нечетные позиции: 1, 3, 5, ...)
        for (int i = 1; i < cycle.size(); i += 2) {
            Pair<Integer, Integer> cell = cycle.get(i);
            Integer value = basicSolution.get(cell.first).get(cell.second);

            // Учитываем базисные клетки (включая нулевые!)
            if (value != null) {
                theta = Math.min(theta, value);
            }
        }

        return theta;
    }

    /**
     * Перераспределяет поставки по циклу
     * К клеткам со знаком "+" (четные позиции) добавляет θ
     * Из клеток со знаком "-" (нечетные позиции) вычитает θ
     */
    private static void redistributeAlongCycle(
            ArrayList<Pair<Integer, Integer>> cycle,
            ArrayList<ArrayList<Integer>> basicSolution,
            int theta
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
                // Знак "+": добавляем θ
                basicSolution.get(row).set(col, currentValue + theta);
            } else {
                // Знак "-": вычитаем θ
                int newValue = currentValue - theta;
                // Если стало 0, клетка становится null (выходит из базиса)
                basicSolution.get(row).set(col, newValue == 0 ? null : newValue);
            }
        }
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
