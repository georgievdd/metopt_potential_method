package org.example;


import java.util.ArrayList;

public class Main {

    private static void print(Object object) {
        System.out.println(object);
    }

    private static String repeat(String s, int times) {
        return s.repeat(times);
    }

    public static void main(String[] args) {
        Task task = Task.example();
        task.print();

        ArrayList<ArrayList<Integer>> minElementResult = BasicSolution.minimalElementMethod(task);
        ArrayList<ArrayList<Integer>> optimizedResult = PotentialMethod.optimize(minElementResult, task);
//        print(Matrix.sumElements(Matrix.cellMult(optimizedResult, task.getCosts())));
    }
}