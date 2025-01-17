package dev.mirodil.testing_system.utils;

import dev.mirodil.testing_system.exceptions.GenericValidationError;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public class ValidationUtil {
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
        Sort sort = pageable.getSort();
        if (sort.isEmpty()) {
            return;
        }

        for (Sort.Order order : sort.stream().toList()) {
            if (!allowedSortAttributes.contains(order.getProperty())) {
                throw new GenericValidationError("Not allowed attribute for sorting: " + order.getProperty());
            }

            if (!order.isAscending() && !order.isDescending()) {
                throw new GenericValidationError("Sort direction must be either ASC or DESC");
            }
        }
    }
}
