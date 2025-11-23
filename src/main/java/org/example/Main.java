package org.example;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        Task task = Task.gen();

        Logger.header("Транспортная задача");
        System.out.println(task);
        Logger.checkBalance(task);

        Logger.header("Построение начального базисного решения");

        Logger.subheader("Метод северо-западного угла");
        ArrayList<ArrayList<Integer>> northwestCornerResult = BasicSolution.northwestCornerMethod(task);
        Logger.printSolutionMatrix(northwestCornerResult, "Базисное решение (северо-западный угол)");
        Logger.printBasicCells(northwestCornerResult);
        Logger.checkBasicCount(northwestCornerResult, 9);
        int nwCost = Logger.calculateCost(northwestCornerResult, task.getCosts());
        Logger.info("Стоимость: " + nwCost);

        Logger.subheader("Метод минимального элемента");
        ArrayList<ArrayList<Integer>> minElementResult = BasicSolution.minimalElementMethod(task);
        Logger.printSolutionMatrix(minElementResult, "Базисное решение (минимальный элемент)");
        Logger.printBasicCells(minElementResult);
        Logger.checkBasicCount(minElementResult, 9);
        int minCost = Logger.calculateCost(minElementResult, task.getCosts());
        Logger.info("Стоимость: " + minCost);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("ОПТИМИЗАЦИЯ МЕТОДОМ ПОТЕНЦИАЛОВ");
        System.out.println("=".repeat(70));

        Logger.header("Вариант 1: Оптимизация решения северо-западного угла");
        ArrayList<ArrayList<Integer>> optimizedNW = PotentialMethod.optimize(northwestCornerResult, task);

        Logger.header("Вариант 2: Оптимизация решения минимального элемента");
        ArrayList<ArrayList<Integer>> optimizedMin = PotentialMethod.optimize(minElementResult, task);

        Logger.header("Сравнение результатов");
        System.out.println("\n┌─────────────────────────────────┬──────────────┬──────────────┐");
        System.out.println("│ Метод                           │ Начальная    │ Оптимальная  │");
        System.out.println("├─────────────────────────────────┼──────────────┼──────────────┤");
        System.out.printf("│ Северо-западный угол            │ %12d │ %12d │%n", nwCost, Logger.calculateCost(optimizedNW, task.getCosts()));
        System.out.printf("│ Минимальный элемент             │ %12d │ %12d │%n", minCost, Logger.calculateCost(optimizedMin, task.getCosts()));
        System.out.println("└─────────────────────────────────┴──────────────┴──────────────┘");
    }
}