package org.example;


import java.util.ArrayList;

public class Main {

    private static void print(Object object) {
        System.out.println(object);
    }

    private static String repeat(String s, int times) {
        return s.repeat(times);
    }

    private static void printSolution(String methodName, ArrayList<ArrayList<Integer>> solution, Integer cost) {
        System.out.println("\n=== " + methodName + " ===");

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

        System.out.print("┌" + horizontal);
        for (int j = 0; j < 6; j++) System.out.print(topSep + horizontal);
        System.out.println("┐");

        System.out.print("│" + center.apply("", cellWidth));
        for (int j = 0; j < 6; j++) {
            System.out.print("│" + center.apply("C" + (j + 1), cellWidth));
        }
        System.out.println("│");

        System.out.print("├" + horizontal);
        for (int j = 0; j < 6; j++) System.out.print(midSep + horizontal);
        System.out.println("┤");

        for (int i = 0; i < 4; i++) {
            System.out.print("│" + center.apply("P" + (i + 1), cellWidth));
            for (int j = 0; j < 6; j++) {
                String value = solution.get(i).get(j) == 0 ? "-" : String.valueOf(solution.get(i).get(j));
                System.out.print("│" + center.apply(value, cellWidth));
            }
            System.out.println("│");

            if (i < 3) {
                System.out.print("├" + horizontal);
                for (int j = 0; j < 6; j++) System.out.print(midSep + horizontal);
                System.out.println("┤");
            }
        }

        System.out.print("└" + horizontal);
        for (int j = 0; j < 6; j++) System.out.print(botSep + horizontal);
        System.out.println("┘");

        System.out.println("Стоимость: " + cost);
    }

    public static void main(String[] args) {
        Task task = Task.example();
        task.print();

        ArrayList<ArrayList<Integer>> minElementResult = BasicSolution.minimalElementMethod(task);
        PotentialMethod.optimize(minElementResult, task);

    }
}