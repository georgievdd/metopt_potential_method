package org.example;

import java.util.ArrayList;

public class Logger {

    private static String repeat(String s, int times) {
        return s.repeat(times);
    }

    private static String center(String text, int width) {
        int len = text.length();
        int spaces = width - len;
        int left = spaces / 2;
        int right = spaces - left;
        return repeat(" ", left) + text + repeat(" ", right);
    }

    public static void header(String title) {
        int width = 70;
        System.out.println("\n" + repeat("═", width));
        System.out.println(center(title.toUpperCase(), width));
        System.out.println(repeat("═", width));
    }

    public static void subheader(String title) {
        System.out.println("\n" + repeat("─", 70));
        System.out.println(title);
        System.out.println(repeat("─", 70));
    }

    public static void section(String title) {
        System.out.println("\n" + title);
    }

    public static void info(String message) {
        System.out.println("  " + message);
    }

    public static void success(String message) {
        System.out.println("  ✓ " + message);
    }

    public static void warning(String message) {
        System.out.println("  ⚠ " + message);
    }

    public static void printSolutionMatrix(ArrayList<ArrayList<Integer>> matrix, String title) {
        int rows = matrix.size();
        int cols = matrix.get(0).size();
        int cellWidth = 8;

        String horizontal = repeat("─", cellWidth);
        String topSep = "┬";
        String midSep = "┼";
        String botSep = "┴";

        System.out.println("\n" + title + ":");

        System.out.print("┌" + horizontal);
        for (int j = 0; j < cols; j++) System.out.print(topSep + horizontal);
        System.out.println("┐");

        System.out.print("│" + center("", cellWidth));
        for (int j = 0; j < cols; j++) {
            System.out.print("│" + center("C" + (j + 1), cellWidth));
        }
        System.out.println("│");

        System.out.print("├" + horizontal);
        for (int j = 0; j < cols; j++) System.out.print(midSep + horizontal);
        System.out.println("┤");

        for (int i = 0; i < rows; i++) {
            System.out.print("│" + center("P" + (i + 1), cellWidth));
            for (int j = 0; j < cols; j++) {
                Integer value = matrix.get(i).get(j);
                String text = value == null ? "-" : String.valueOf(value);
                System.out.print("│" + center(text, cellWidth));
            }
            System.out.println("│");

            if (i < rows - 1) {
                System.out.print("├" + horizontal);
                for (int j = 0; j < cols; j++) System.out.print(midSep + horizontal);
                System.out.println("┤");
            }
        }

        System.out.print("└" + horizontal);
        for (int j = 0; j < cols; j++) System.out.print(botSep + horizontal);
        System.out.println("┘");
    }

    public static void printVector(ArrayList<Integer> vector, String name) {
        System.out.print("  " + name + " = [");
        for (int i = 0; i < vector.size(); i++) {
            Integer val = vector.get(i);
            if (val == null) {
                System.out.print("null");
            } else {
                System.out.print(val);
            }
            if (i < vector.size() - 1) System.out.print(", ");
        }
        System.out.println("]");
    }

    public static void printCycle(ArrayList<Pair<Integer, Integer>> cycle) {
        System.out.print("  Цикл: ");
        for (int i = 0; i < cycle.size(); i++) {
            Pair<Integer, Integer> cell = cycle.get(i);
            System.out.print("(" + (cell.first + 1) + "," + (cell.second + 1) + ")");
            if (i < cycle.size() - 1) {
                System.out.print(" → ");
            }
        }
        System.out.println();

        System.out.print("  Знаки: ");
        for (int i = 0; i < cycle.size(); i++) {
            System.out.print(i % 2 == 0 ? "+" : "-");
            if (i < cycle.size() - 1) {
                System.out.print("   ");
            }
        }
        System.out.println();
    }

    public static void printBasicCells(ArrayList<ArrayList<Integer>> solution) {
        int count = 0;
        System.out.print("  Базисные клетки: ");
        for (int i = 0; i < solution.size(); i++) {
            for (int j = 0; j < solution.get(i).size(); j++) {
                if (solution.get(i).get(j) != null) {
                    if (count > 0) System.out.print(", ");
                    System.out.print("(" + (i + 1) + "," + (j + 1) + ")=" + solution.get(i).get(j));
                    count++;
                }
            }
        }
        System.out.println("\n  Всего базисных клеток: " + count);
    }

    public static int calculateCost(ArrayList<ArrayList<Integer>> solution, ArrayList<ArrayList<Integer>> costs) {
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

    public static void checkBalance(Task task) {
        int supply = task.getProducers().stream().mapToInt(Integer::intValue).sum();
        int demand = task.getConsumers().stream().mapToInt(Integer::intValue).sum();

        if (supply == demand) {
            success("Задача сбалансирована: Σ запасы = Σ потребности = " + supply);
        } else {
            warning("Задача не сбалансирована: Σ запасы = " + supply + ", Σ потребности = " + demand);
        }
    }

    public static void checkBasicCount(ArrayList<ArrayList<Integer>> solution, int expected) {
        int count = 0;
        for (int i = 0; i < solution.size(); i++) {
            for (int j = 0; j < solution.get(i).size(); j++) {
                if (solution.get(i).get(j) != null) {
                    count++;
                }
            }
        }

        if (count == expected) {
            success("Число базисных переменных корректно: " + count + " = " + (solution.size() + solution.get(0).size() - 1));
        } else {
            warning("Число базисных переменных некорректно: " + count + " ≠ " + expected + " (вырожденность)");
        }
    }

    public static void checkOptimality(ArrayList<ArrayList<Integer>> reducedEstimates) {
        int minEstimate = Integer.MAX_VALUE;
        Pair<Integer, Integer> minCell = null;

        for (int i = 0; i < reducedEstimates.size(); i++) {
            for (int j = 0; j < reducedEstimates.get(i).size(); j++) {
                if (reducedEstimates.get(i).get(j) < minEstimate) {
                    minEstimate = reducedEstimates.get(i).get(j);
                    minCell = new Pair<>(i, j);
                }
            }
        }

        if (minEstimate >= 0) {
            success("Критерий оптимальности выполнен: все редуцированные оценки ≥ 0");
        } else {
            warning("Критерий оптимальности не выполнен: минимальная оценка = " + minEstimate + " в клетке (" + (minCell.first + 1) + "," + (minCell.second + 1) + ")");
        }
    }
}
