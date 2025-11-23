package org.example;

import java.util.ArrayList;

public class Matrix {
    public static <T> ArrayList<ArrayList<T>> empty(int n, int m, Object defaultValue) {
        ArrayList<ArrayList<T>> matrix = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            ArrayList<T> row = new ArrayList<>(m);
            for (int j = 0; j < m; j++) {
                row.add((T) defaultValue);
            }
            matrix.add(row);
        }
        return matrix;
    }

    public static ArrayList<ArrayList<Integer>> cellMult(ArrayList<ArrayList<Integer>> first, ArrayList<ArrayList<Integer>> second) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<>(first.size());
        for (int i = 0; i < first.size(); i++) {
            ArrayList<Integer> row = new ArrayList<>(first.get(i).size());
            for (int j = 0; j < first.get(i).size(); j++) {
                Integer v1 = first.get(i).get(j);
                Integer v2 = second.get(i).get(j);
                row.add((v1 == null ? 0 : v1) * (v2 == null ? 0 : v2));
            }
            result.add(row);
        }
        return result;
    }

    public static Integer sumElements(ArrayList<ArrayList<Integer>> matrix) {
        int sum = 0;
        for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.get(i).size(); j++) {
                sum += matrix.get(i).get(j);
            }
        }
        return sum;
    }

    public static ArrayList<Pair<Integer, Integer>> non0fields(ArrayList<ArrayList<Integer>> matrix) {
        ArrayList<Pair<Integer, Integer>> result = new ArrayList<>(matrix.size());
        for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.size(); j++) {
                if (matrix.get(i).get(j) != 0) {
                    result.add(new Pair<>(i, j));
                }
            }
        }
        return result;
    }

    public static Pair<Integer, Integer> minElement(
            ArrayList<ArrayList<Integer>> matrix
    ) {
        Pair<Integer, Integer> result = null;
        Integer min = Integer.MAX_VALUE;
        for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.get(i).size(); j++) {
                if (matrix.get(i).get(j) < min) {
                    min = matrix.get(i).get(j);
                    result = new Pair<>(i, j);
                }
            }
        }
        return result;
    }
}
