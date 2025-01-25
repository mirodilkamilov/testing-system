package dev.mirodil.testing_system.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

@Schema(description = "Custom pageable request with filters")
public class PageWithFilterRequest extends PageRequest {
    @Schema(description = "Map of filter criteria. Add as query parameters (e.g., email=john.doe@example.com&userRole=TEST_TAKER).")
    private final Map<String, String> filters;

    public PageWithFilterRequest(int pageNumber, int pageSize, Sort sort, Map<String, String> filters) {
        super(pageNumber, pageSize, sort);
        this.filters = filters != null ? filters : new HashMap<>();
    }

    public Map<String, String> getFilters() {
        return filters;
    }
}
