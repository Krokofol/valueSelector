package app;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IOException {
        ArrayList<String> columns = new ArrayList<>();
        HashMap<String, ArrayList<Double>> values = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\aleks\\Documents\\GitHub\\lab1\\src\\main\\resources\\clean.csv"));
        String string = reader.readLine();
        for (String iterator : string.split(",")) {
            values.put(iterator, new ArrayList<>());
            columns.add(iterator);
        }
        logger.info("found {} columns", columns.size());
        while ((string = reader.readLine()) != null) {
            int i = 0;
            String[] arguments = string.split(",");
            for (String iterator : arguments) {
                if (iterator.equals("")) {
                    values.get(columns.get(i)).add(null);
                } else {
                    values.get(columns.get(i)).add(Double.parseDouble(iterator));
                }
                i++;
            }
        }
        logger.info("found {} strings", values.get(columns.get(0)).size());
        cleaningArgs(columns, values);
        deletingOutliers(columns, values);
        logger.info("after cleaning has {} columns and {} rows", new Object[]{columns.size(), values.get(columns.get(0)).size()});
        new CorrelationFrame(columns, values);
    }

    private static void deletingOutliers(ArrayList<String> columns, HashMap<String, ArrayList<Double>> values) {
        HashSet<Integer> stringNumsHash = new HashSet<>();
        ArrayList<String> columnsCopy = new ArrayList<>(columns);
//        HashMap<Integer, String> hashMap = new HashMap<>();
//
        String kgf = columns.get(columns.size() - 1);
        String gTotal = columns.get(columns.size() - 2);
        columnsCopy.remove(kgf);
        columnsCopy.remove(gTotal);

        for (String columnIterator : columnsCopy) {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (int i = 0; i < values.get(columnIterator).size(); i++) {
                if (values.get(columnIterator).get(i) == null) {
                    continue;
                }
                stats.addValue(values.get(columnIterator).get(i));
            }
            double value25 = stats.getPercentile(25);
            double value75 = stats.getPercentile(75);
            double min = value25 - 1.5 * (value75 - value25);
            double max = value75 + 1.5 * (value75 - value25);
            for (int i = 0; i < values.get(columnIterator).size() - 2; i++) {
                if (values.get(columnIterator).get(i) == null && values.get(gTotal).get(i) == null) {
//                    logger.info("in column {} : {} < {} < {} and {} is {}", new Object[]{columnIterator, min, values.get(columnIterator).get(i), max, columns.get(columns.size() - 2), values.get(columns.get(columns.size() - 2)).get(i)});
                    stringNumsHash.add(i);
//                    hashMap.put(i, columnIterator + " in " + i);
                }
                if (values.get(columnIterator).get(i) == null) {
                    continue;
                }
                if ((values.get(columnIterator).get(i) > max
                        || values.get(columnIterator).get(i) < min
                        || values.get(columnIterator).get(i) == null)
                        && values.get(gTotal).get(i) == null) {
//                    logger.info("in column {} : {} < {} < {} and {} is {}", new Object[]{columnIterator, min, values.get(columnIterator).get(i), max, columns.get(columns.size() - 2), values.get(gTotal).get(i)});
                    stringNumsHash.add(i);
                }
            }
        }
        ArrayList<Integer> stringNums =  new ArrayList<>(stringNumsHash);
//        logger.info("deleting {}", stringNums);
        for (int i = stringNums.size() - 1; i >= 0; i--) {
//            logger.info("deleting i = {} where gTotal is {}", i, values.get(gTotal).get(stringNums.get(i)));
            for (String iterator : columns) {
                values.get(iterator).remove((int) stringNums.get(i));
            }
        }
//        for (Integer i : hashMap.keySet()) {
//            System.out.println(hashMap.get(i));
//        }
    }

    private static void cleaningArgs(ArrayList<String> columns, HashMap<String, ArrayList<Double>> values) {
        ArrayList<String> columnsToDelete = new ArrayList<>();
        for (String columnName : columns) {
            if (columnName.equals(columns.get(columns.size() - 1)) || columnName.equals(columns.get(columns.size() - 2))) {
                continue;
            }
            int nullCounter = 0;
            for (Double iterator : values.get(columnName)) {
                if (iterator == null) {
                    nullCounter++;
                }
            }
            if (nullCounter > values.size() / 3) {
                columnsToDelete.add(columnName);
            }
        }
        for (String columnName : columnsToDelete) {
            values.remove(columnName);
            columns.remove(columnName);
        }
    }

}
