package dev.mirodil.testing_system.utils;

import dev.mirodil.testing_system.configs.PageableWithFilterResolver;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

/**
 * Extends {@link PageRequest} to support dynamic filtering.
 * <p>
 * Filters are stored as a nested map:
 * {@code Map<attribute, Map<value, valueType>>}, e.g.:
 * <pre>
 *     { "isPassed": { "true": Boolean.class }, "title": { "Test": String.class } }
 * </pre>
 * Example query parameters: {@code ?title=Test&isPassed=true&sort=eventDateTime,desc&sort=id}.
 * Internally, {@link PageableWithFilterResolver} converts them to the correct types.
 *
 * @see PageableWithFilterResolver
 * @see PageRequest
 */
@Schema(description = "Custom pageable request with filters")
public class PageWithFilterRequest extends PageRequest {
    /**
     * Filter criteria in the form of {@code Map<attribute, Map<value, valueType>>}.
     */
    @Schema(description = "Map of filter criteria. Add as query parameters (e.g., email=john.doe@example.com&userRole=TEST_TAKER).")
    private final Map<String, Map<String, Class<?>>> filters;

    public PageWithFilterRequest(int pageNumber, int pageSize, Sort sort, Map<String, Map<String, Class<?>>> filters) {
        super(pageNumber, pageSize, sort);
        this.filters = filters != null ? filters : new HashMap<>();
    }

    public Map<String, Map<String, Class<?>>> getFilters() {
        return filters;
    }
}
