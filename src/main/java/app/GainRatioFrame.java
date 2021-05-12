package app;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class GainRatioFrame extends JFrame {

    public static ArrayList<Pair<Double, String>> results = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(GainRatioFrame.class);

    private static HashSet<Pair<Pair<Double, Double>, Double>> classes = new HashSet<>();

    private static final ArrayList<Pair<Double, Double>> kgfIntervals = new ArrayList<>();

    public GainRatioFrame(ArrayList<String> columns, HashMap<String, ArrayList<Double>> values) {
        String kgf = columns.get(columns.size() - 1);
        String gTotal = columns.get(columns.size() - 2);
        refactoringValues(columns, values);

        getClasses(values.get(kgf), values.get(gTotal));

        logger.info("created {} intervals", kgfIntervals.size());
        logger.info("created {} classes", classes.size());

        for (String iterator : columns) {
//            System.out.println("\n" + iterator);
            Double result = calculatingResult(values.get(iterator), values.get(kgf), values.get(gTotal));
            int i = 0;
            for (; i < results.size(); i++) {
                if (results.get(i).getFirst() > result) {
                    break;
                }
            }
            results.add(i, new Pair<>(result, iterator));
        }
//        String kgf = columns.get(columns.size() - 1);
//        String gTotal = columns.get(columns.size() - 2);
        columns.add(columns.size(), gTotal);
        columns.add(columns.size(), kgf);
//
//        for (Pair<Double, String> iterator : results) {
//            System.out.println(iterator.getSecond());
//        }
//        System.out.println();
//        for (Pair<Double, String> iterator : results) {
//            System.out.println(iterator.getFirst().toString().replace(".", ","));
//        }

        try {
            FileWriter dataSet = new FileWriter("dataSet.csv", false);
            for (int i = 0; i < values.get(columns.get(0)).size(); i++) {
                for (String iterator : values.keySet()) {
                    if (iterator.equals("КГФ") || (iterator.equals("G_total"))) {
                        continue;
                    }
                    if (values.get(iterator).get(i) == null) {
                        dataSet.write("nan, ");
                        continue;
                    }
                    dataSet.write(values.get(iterator).get(i).toString() + ", ");
                }
                if (values.get("G_total").get(i) != null) {
                    dataSet.write(values.get("G_total").get(i).toString() + ", ");
                } else {
                    dataSet.write("nan, ");
                }
                if (values.get("КГФ").get(i) != null) {
                    dataSet.write(values.get("КГФ").get(i).toString() + ", ");
                } else {
                    dataSet.write("nan, ");
                }
                dataSet.write("\n");
                dataSet.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Double calculatingResult(ArrayList<Double> column, ArrayList<Double> kgf, ArrayList<Double> gTotal) {
        double result;

        double avgInfo = calculateAvgInfo(kgf, gTotal);

        double info = calculateInfo(column, kgf, gTotal);

//        HashSet<Double> uniqValues = new HashSet<>(column);
//        for (Double iterator : uniqValues) {
//            if (iterator == null) {
//                continue;
//            }
//            HashSet<Double> uniqValue = new HashSet<>();
//            uniqValue.add(iterator);
//            double midData = calculateInfo(uniqValue, column, kgf, gTotal);
//            int count = 0;
//            for (Double iterator2 : column) {
//                if (iterator2 == null) {
//                    continue;
//                }
//                if (iterator.equals(iterator2)) {
//                    count++;
//                }
//            }
//            midData = ((double) count) * midData / ((double) columnCopy.size());
//            info += midData;
//        }

        Double split = calculateSplit(column);

        logger.info("got : \n  avgInfo = {}\n  info = {}\n  split = {}", new Object[]{avgInfo, info, split});

        result = (avgInfo - info) / split;

        return result;
    }

    private static Double calculateAvgInfo(ArrayList<Double> kgf, ArrayList<Double> gTotal) {
        double result = 0;
        HashMap<Pair<Pair<Double, Double>, Double>, Integer> counter = new HashMap<>();
        for (Pair<Pair<Double, Double>, Double> iterator : classes) {
            counter.put(iterator, 0);
        }
        for (int i = 0; i < kgf.size(); i++) {
            Double kgfValue = kgf.get(i);
            Double gTotalValue = gTotal.get(i);
            Pair<Double, Double> interval = findInterval(kgfValue);
            Pair<Pair<Double, Double>, Double> class_ = new Pair<>(interval, gTotalValue);
            class_ = findClass(class_);
            counter.put(class_, counter.get(class_) + 1);
        }
        for (Pair<Pair<Double, Double>, Double> iterator : counter.keySet()) {
            int count = counter.get(iterator);
            if (count == 0) {
                continue;
            }
            double probability = ((double) count) / ((double) kgf.size());
            result -= probability * (Math.log(probability) / Math.log(2));
        }
        return result;
    }

    private static Double calculateSplit(ArrayList<Double> column) {
        double result = 0;
        HashSet<Double> uniqueValues = new HashSet<>(column);

        for (Double iterator : uniqueValues) {
            if (iterator == null) {
                continue;
            }
            int count = 0;
            for (Double iterator2 : column) {
                if (iterator2 == null) {
                    continue;
                }
                if (iterator.equals(iterator2)) {
                    count++;
                }
            }
            double probability = ((double) count) / ((double) column.size());
            result -= probability * Math.log(probability) / Math.log(2);
        }

        return result;
    }

    private static Double calculateInfo(ArrayList<Double> column, ArrayList<Double> kgf, ArrayList<Double> gTotal) {
        double result = 0d;

        HashSet<Double> uniqueValues = new HashSet<>(column);

        logger.info("unique values : {}", uniqueValues);

        for (Double uniqueValue : uniqueValues) {
            if (uniqueValue == null) {
                continue;
            }
            HashMap<Pair<Pair<Double, Double>, Double>, Integer> classCounter = new HashMap<>();
            for (Pair<Pair<Double, Double>, Double> iterator : classes) {
                classCounter.put(iterator, 0);
            }
            int nullCount = 0;
            int uniqueValueCount = 0;
            for (int i = 0; i < column.size(); i++) {
                if (column.get(i) == null) {
                    nullCount++;
                    continue;
                }
                if (!column.get(i).equals(uniqueValue)) {
                    continue;
                }
                uniqueValueCount++;
                Double kgfValue = kgf.get(i);
                Double gTotalValue = gTotal.get(i);
                Pair<Double, Double> interval = findInterval(kgfValue);
                Pair<Pair<Double, Double>, Double> class_ = new Pair<>(interval, gTotalValue);
                class_ = findClass(class_);
                classCounter.put(class_, classCounter.get(class_) + 1);
            }
            double infoXResult = 0d;
            for (Pair<Pair<Double, Double>, Double> iterator : classCounter.keySet()) {
                int count = classCounter.get(iterator);
                if (count == 0) {
                    continue;
                }
                double probability = ((double) count) / ((double) uniqueValueCount);
                infoXResult -= probability * Math.log(probability) / Math.log(2);
            }
            logger.info("\nvalue count : {}\ninfoX : {}", uniqueValueCount, infoXResult);
            result += ((double) uniqueValueCount) * infoXResult / ((double) (column.size() - nullCount));
        }

        return result;
    }

//    private static void deleteNulls(ArrayList<Double> columnCopy2) {
//        ArrayList<Integer> indexes = new ArrayList<>();
//        for (int i = 0; i < columnCopy2.size(); i++) {
//            if (columnCopy2.get(i) == null) {
//                indexes.add(0, i);
//            }
//        }
//        for (Integer index : indexes) {
//            columnCopy2.remove((int) index);
//        }
//    }

    private static Pair<Pair<Double, Double>, Double> findClass(Pair<Pair<Double, Double>, Double> class_) {
        for (Pair<Pair<Double, Double>, Double> iterator : classes) {
            if (iterator.getFirst() == class_.getFirst() && Objects.equals(iterator.getSecond(), class_.getSecond())) {
                return iterator;
            }
        }
        logger.error("ERROR : did not found class for value {}", class_);
        return null;
    }

    private static void refactoringValues(ArrayList<String> columns, HashMap<String, ArrayList<Double>> values) {
        String kgf = columns.get(columns.size() - 1);
        String gTotal = columns.get(columns.size() - 2);
        int index = 0;
        while (values.get(kgf).size() > index) {
            if (values.get(kgf).get(index) == null && values.get(gTotal).get(index) == null) {
                logger.info("removing string {}", index);
                removeString(index, values);
                index--;
            }
            index++;
        }
        columns.remove(kgf);
        columns.remove(gTotal);
    }

    private static void removeString(int index, HashMap<String, ArrayList<Double>> values) {
        logger.info("removing string {}", index);
        for (String iterator : values.keySet()) {
            values.get(iterator).remove(index);
        }
    }

    private static void getClasses(ArrayList<Double> kgf, ArrayList<Double> gTotal) {
        //элемент - пара(мин и макс)kgf и gTotal
        HashSet<Pair<Pair<Double, Double>, Double>> classes = new HashSet<>();

        int count = (int) Math.round(1 + Math.log(kgf.size()) / Math.log(2));
        Double minKgf = min(kgf);
        Double maxKgf = max(kgf);
        Double weight = (maxKgf - minKgf) / ((double) count);
        getIntervals(minKgf, maxKgf, weight, count);

        first :
        for (int i = 0; i < kgf.size(); i++) {
            Pair<Double, Double> interval = findInterval(kgf.get(i));
            Double gTotalValue = gTotal.get(i);
            Pair<Pair<Double, Double>, Double> class_ = new Pair<>(interval, gTotalValue);
            for (Pair<Pair<Double, Double>, Double> iterator : classes) {
                if (iterator.getFirst() == class_.getFirst() && Objects.equals(iterator.getSecond(), class_.getSecond())) {
                    continue first;
                }
            }
            classes.add(class_);
        }

        GainRatioFrame.classes = classes;
    }

    private static void getIntervals(Double min, Double max, Double weight, int count) {
        int index;
        for ( index = 0; index < count - 1; index++) {
            GainRatioFrame.kgfIntervals.add(new Pair<>(min + index * weight, min + (index + 1) * weight));
        }
        GainRatioFrame.kgfIntervals.add(new Pair<>(min + index * weight, max));
    }

    private static Pair<Double, Double> findInterval(Double value) {
        if (value == null) {
            return null;
        }
        for (Pair<Double, Double> iterator : GainRatioFrame.kgfIntervals) {
            if (iterator.getFirst() <= value && iterator.getSecond() >= value) {
                return iterator;
            }
        }
        return null;
    }

    private static Double min(ArrayList<Double> values) {
        int i = 0;
        while (values.get(i) == null) {
            i++;
        }
        Double result = values.get(i);
        for (Double iterator : values) {
            if (iterator ==  null) {
                continue;
            }
            if (iterator < result) {
                result = iterator;
            }
        }
        return result;
    }

    private static Double max(ArrayList<Double> values) {
        int i = 0;
        while (values.get(i) == null) {
            i++;
        }
        Double result = values.get(i);
        for (Double iterator : values) {
            if (iterator ==  null) {
                continue;
            }
            if (iterator > result) {
                result = iterator;
            }
        }
        return result;
    }
}
