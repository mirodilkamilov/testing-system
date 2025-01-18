package dev.mirodil.testing_system.utils;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataUtil {
    public static void appendOrderByClause(StringBuilder queryBuilder, Sort sort) {
        if (sort.isEmpty()) {
            return;
        }

        queryBuilder.append(" ORDER BY ");
        queryBuilder.append(sort.stream()
                .map(order -> order.getProperty() + " " + order.getDirection().name())
                .collect(Collectors.joining(", ")));
    }

    public static List<String> appendWhereClause(StringBuilder queryBuilder, Map<String, String> filters) {
        List<String> filterValues = new ArrayList<>();
        if (!filters.isEmpty()) {
            queryBuilder.append(" WHERE ");
            queryBuilder.append(filters.entrySet().stream()
                    .map(entry -> {
                        filterValues.add("%" + entry.getValue() + "%"); // Add the filter value as a parameter
                        return entry.getKey() + " LIKE ?";
                    })
                    .collect(Collectors.joining(" AND ")));
        }

        return filterValues;
    }
}
