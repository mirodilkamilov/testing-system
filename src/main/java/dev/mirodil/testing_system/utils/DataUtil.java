package dev.mirodil.testing_system.utils;

import org.springframework.data.domain.Sort;

import java.util.stream.Collectors;

public class DataUtil {
    public static String convertSortOrdersToOrderByClause(Sort sort) {
        return " ORDER BY " + sort.stream()
                .map(order -> order.getProperty() + " " + order.getDirection().name())
                .collect(Collectors.joining(", "));
    }
}
