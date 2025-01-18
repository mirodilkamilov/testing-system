package dev.mirodil.testing_system.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

public class PageWithFilterRequest extends PageRequest {
    private final Map<String, String> filters;

    public PageWithFilterRequest(int pageNumber, int pageSize, Sort sort, Map<String, String> filters) {
        super(pageNumber, pageSize, sort);
        this.filters = filters != null ? filters : new HashMap<>();
    }

    public Map<String, String> getFilters() {
        return filters;
    }
}
