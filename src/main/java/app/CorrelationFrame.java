package app;

import org.apache.commons.math3.util.Pair;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CorrelationFrame extends JFrame {

    public static final Double maxCorrelation = 0.7;
    public static final Double maxDeltaCorrelation = 0.3;

    public CorrelationFrame(ArrayList<String> columns, HashMap<String, ArrayList<Double>> values) {
        Double[][] tableValues = new Double[columns.size()][columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            for (int j = 0; j < columns.size(); j++) {
                tableValues[i][j] = calculateCorrelation(values.get(columns.get(i)), values.get(columns.get(j)));
            }
        }

        ArrayList<Pair<Integer, Integer>> highCorrelated = new ArrayList<>();

        for (int i = 0; i < columns.size() - 3; i++) {
            for (int j = i + 1; j < columns.size() - 2; j++) {
                if (tableValues[i][j] > maxCorrelation) highCorrelated.add(new Pair<>(i, j));
            }
        }

        ArrayList<Pair<Integer, Integer>> toDelete = new ArrayList<>();
        for (Pair<Integer, Integer> integerIntegerPair : highCorrelated) {
            Integer col1 = integerIntegerPair.getFirst();
            Integer col2 = integerIntegerPair.getSecond();
            boolean delete = true;
            for (int j = 0; j < columns.size(); j++) {
                if (col1.equals(j) || col2.equals(j)) {
                    continue;
                }
                if (Math.abs(tableValues[col1][j] - tableValues[col2][j]) >= maxDeltaCorrelation) {
                    delete = false;
                    break;
                }
            }
            if (delete) {
                toDelete.add(integerIntegerPair);
            }
        }

        new GainRatioFrame(columns, values);
        HashSet<Integer> finalToDelete = new HashSet<>();
        for (Pair<Integer, Integer> integerIntegerPair : toDelete) {
            boolean findFirst = false;
            for (int j = 0; j < GainRatioFrame.results.size(); j++) {
                if (GainRatioFrame.results.get(j).getSecond().equals(columns.get(integerIntegerPair.getFirst()))) {
                    if (findFirst) {
                        finalToDelete.add(integerIntegerPair.getFirst());
                        break;
                    }
                    findFirst = true;
                }
                if (GainRatioFrame.results.get(j).getSecond().equals(columns.get(integerIntegerPair.getSecond()))) {
                    if (findFirst) {
                        finalToDelete.add(integerIntegerPair.getSecond());
                        break;
                    }
                    findFirst = true;
                }
            }
        }

        Integer[] arrayToDelete = finalToDelete.toArray(new Integer[0]);
        for (int i = 0; i < arrayToDelete.length; i++) {
            for (int j = 0; j < arrayToDelete.length; j++) {
                if (arrayToDelete[i] > arrayToDelete[j]) {
                    Integer boof = arrayToDelete[i];
                    arrayToDelete[i] = arrayToDelete[j];
                    arrayToDelete[j] = boof;
                }
            }
        }

        for (int i = 0; i < arrayToDelete.length; i++) {
            boolean alreadyDeleted = false;
            for (int j = 0; j < columns.size(); j++) {
                if (arrayToDelete[i].equals(j) && ! alreadyDeleted) {
                    values.remove(columns.get(j));
                    columns.remove(j);
                    alreadyDeleted = true;
                    j--;
                    continue;
                }
                values.get(columns.get(j)).remove((int) arrayToDelete[i]);
            }
        }

        tableValues = new Double[columns.size()][columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            for (int j = 0; j < columns.size(); j++) {
                tableValues[i][j] = calculateCorrelation(values.get(columns.get(i)), values.get(columns.get(j)));
            }
        }
        GainRatioFrame.results = new ArrayList<>();
        new GainRatioFrame(columns, values);
        for (Pair<Double, String> iterator : GainRatioFrame.results) {
            System.out.println(iterator.getSecond() + " - " + iterator.getFirst());
        }

        System.out.println("---FOR EXEL---");
        for (Pair<Double, String> iterator : GainRatioFrame.results) {
            System.out.println(iterator.getSecond());
        }
        for (Pair<Double, String> iterator : GainRatioFrame.results) {
            System.out.println(iterator.getFirst().toString().replace(".", ","));
        }


        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JTable frameTable = new JTable(tableValues, columns.toArray());
        frameTable.setDefaultRenderer(Object.class, new Renderer());
        JScrollPane scrollPane = new JScrollPane(frameTable);
        getContentPane().add(scrollPane);
        int sizeX = 50 * columns.size();
        int sizeY = 20 * columns.size();
        setPreferredSize(new Dimension(sizeX, sizeY));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static Double calculateCorrelation(ArrayList<Double> values1, ArrayList<Double> values2) {
        Double avg1 = avg(values1, values2);
        Double avg2 = avg(values2, values1);
        Double numerator = calculateNumerator(values1, values2, avg1, avg2);
        Double denominator = calculateDenominator(values1, values2, avg1, avg2);
        return numerator / denominator;
    }

    public static Double calculateNumerator(ArrayList<Double> values1, ArrayList<Double> values2,
                                            Double avg1, Double avg2) {
        double result = 0d;
        for (int i = 0; i < values1.size(); i++) {
            if (values1.get(i) == null || values2.get(i) == null) {
                continue;
            }
            result += (values1.get(i) - avg1) * (values2.get(i) - avg2);
        }
        return result;
    }

    public static Double calculateDenominator(ArrayList<Double> values1, ArrayList<Double> values2,
                                              Double avg1, Double avg2) {
        double values1Sum = calculateValuesSum(values1, values2, avg1);
        double values2Sum = calculateValuesSum(values2, values1, avg2);
        double result = values1Sum * values2Sum;
        return Math.sqrt(result);
    }

    public static double calculateValuesSum(ArrayList<Double> values1, ArrayList<Double> values2,
                                            Double avg1) {
        double result = 0d;
        for (int i = 0; i < values1.size(); i++) {
            if (values1.get(i) == null || values2.get(i) == null) {
                continue;
            }
            result += (values1.get(i) - avg1) * (values1.get(i) - avg1);
        }
        return result;
    }

    public static Double avg(ArrayList<Double> values1, ArrayList<Double> values2) {
        Double sum = 0d;
        Double counter = 0d;
        for (int i = 0; i < values1.size(); i++) {
            if (values1.get(i) == null || values2.get(i) == null) {
                continue;
            }
            sum += values1.get(i);
            counter++;
        }
        return sum / counter;
    }

    public static class Renderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            Color color = new Color(0, 0, 75);
            if (value != null) {
                double doubleValue = (Double) value;
                int green = (int) Math.min(245, Math.max(0, Math.round(255 * Math.abs(doubleValue))));
                if (!Double.isNaN(doubleValue)) {
                    color = new Color(0, green, 0);
                }
            }
            c.setBackground(color);
            return c;
        }
    }
}
