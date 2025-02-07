package dev.mirodil.testing_system.utils;

import dev.mirodil.testing_system.exceptions.GenericValidationError;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.Set;

public class ValidationUtil {
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Validates the sorting and filter parameters of a {@link PageWithFilterRequest} object to ensure that only allowed attributes
     * and valid sort directions (ascending or descending) are used. This method helps prevent SQL injection
     * attacks by restricting sorting and filter attributes to a predefined set of allowed values.
     * <p>Malicious input example:
     * {@code /api/users?size=7&sort=id,desc&sort=fname;DROP TABLE users; --}</p>
     *
     * @param pageable                the {@link PageWithFilterRequest} object to validate.
     * @param allowedSortAttributes   a {@link Set} of attribute names that are permitted for sorting.
     * @param allowedFilterAttributes a {@link Map} of permitted for filters. Key is attribute name and Value is its type (e.g. String.class, Boolean.class).
     * @throws GenericValidationError if an invalid sort or filter attribute or sort direction is detected.
     */
    public static void forceValidPageable(PageWithFilterRequest pageable, Set<String> allowedSortAttributes, Map<String, Class<?>> allowedFilterAttributes) throws GenericValidationError {
        forceValidPageable(pageable, allowedSortAttributes);
        forceValidFilter(pageable.getFilters(), allowedFilterAttributes);
    }

    /**
     * Validates the sorting parameters of a {@link Pageable} object to ensure that only allowed attributes
     * and valid sort directions (ascending or descending) are used. This method helps prevent SQL injection
     * attacks by restricting sorting attributes to a predefined set of allowed values.
     * <p>Malicious input example:
     * {@code /api/users?size=7&sort=id,desc&sort=fname;DROP TABLE users; --}</p>
     *
     * @param pageable              the {@link Pageable} object to validate.
     * @param allowedSortAttributes a {@link Set} of attribute names that are permitted for sorting.
     * @throws GenericValidationError if an invalid sort attribute or direction is detected.
     */
    public static void forceValidPageable(Pageable pageable, Set<String> allowedSortAttributes) throws GenericValidationError {
        forceValidPage(pageable.getPageNumber(), pageable.getPageSize());
        forceValidSort(pageable.getSort(), allowedSortAttributes);
    }

    public static void forceValidPage(int pageNumber, int pageSize) {
        if (pageNumber < 0 || pageSize < 0) {
            throw new GenericValidationError("Page number or page size cannot be negative");
        }

        if (pageSize > MAX_PAGE_SIZE) {
            throw new GenericValidationError("Page size cannot be more than " + MAX_PAGE_SIZE);
        }
    }

    public static void forceValidSort(Sort sort, Set<String> allowedSortAttributes) {
        if (sort.isEmpty()) {
            return;
        }

        for (Sort.Order order : sort.stream().toList()) {
            if (!allowedSortAttributes.contains(order.getProperty())) {
                throw new GenericValidationError("Invalid sort attribute: " + order.getProperty());
            }

            if (!order.isAscending() && !order.isDescending()) {
                throw new GenericValidationError("Sort direction must be either ASC or DESC");
            }
        }
    }

    public static void forceValidFilter(Map<String, Map<String, Class<?>>> filters, Map<String, Class<?>> allowedFilterAttributes) {
        if (filters.isEmpty()) {
            return;
        }

        for (String filterKey : filters.keySet()) {
            if (!allowedFilterAttributes.containsKey(filterKey)) {
                throw new GenericValidationError("Invalid filter attribute: " + filterKey);
            }

            // TODO: add filter type validation e.g. DateTime "eventDateTime>=2025-02-07", eventDateTimeBetween...
        }
    }
}
