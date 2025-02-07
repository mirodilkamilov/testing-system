package dev.mirodil.testing_system.configs;

import dev.mirodil.testing_system.exceptions.GenericValidationError;
import dev.mirodil.testing_system.models.TestEvent;
import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.utils.PageWithFilterRequest;
import dev.mirodil.testing_system.utils.ValidationUtil;
import dev.mirodil.testing_system.validations.ValidTestEventPageRequest;
import dev.mirodil.testing_system.validations.ValidUserPageRequest;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom argument resolver for {@link PageWithFilterRequest} that automatically
 * extracts pagination, sorting, and filter parameters from request queries.
 * The resolver is also responsible for validation of these parameters.
 * <p>
 * Sorting and filtering attributes are validated based on entity-specific
 * annotations ({@code @ValidTestEventPageRequest}, {@code @ValidUserPageRequest}).
 * <p>
 * Example query: {@code ?title=Test&isPassed=false}
 * Converts to: {@code { "isPassed": { "false": Boolean.class }, "title": { "Test": String.class } }}
 *
 * @see PageWithFilterRequest
 * @see PageableHandlerMethodArgumentResolver
 */
@Component
public class PageableWithFilterResolver implements HandlerMethodArgumentResolver {
    private final PageableHandlerMethodArgumentResolver defaultPageableResolver;

    public PageableWithFilterResolver(PageableHandlerMethodArgumentResolver defaultPageableResolver) {
        this.defaultPageableResolver = defaultPageableResolver;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return PageWithFilterRequest.class.equals(parameter.getParameterType());
    }

    @Override
    public PageWithFilterRequest resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Pageable pageable = defaultPageableResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        // Extract raw filters from query parameters
        Map<String, String> rawFilters = webRequest.getParameterMap().entrySet().stream()
                .filter(entry -> !entry.getKey().equals("page") && !entry.getKey().equals("size") && !entry.getKey().equals("sort"))
                .filter(entry -> entry.getValue().length > 0 && !entry.getValue()[0].isBlank()) // Remove empty values
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));

        // Determine allowed filters and sorts based on entity annotation
        Map<String, Class<?>> allowedFilters = getEntityAllowedFilters(parameter);
        Set<String> allowedSorts = getEntityAllowedSorts(parameter);

        Map<String, Map<String, Class<?>>> filters = convertFiltersToTypedMap(rawFilters, allowedFilters);

        // Construct PageWithFilterRequest and validate allowed parameters
        PageWithFilterRequest pageRequest = new PageWithFilterRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), filters);
        ValidationUtil.forceValidPageable(pageRequest, allowedSorts, allowedFilters);

        return pageRequest;
    }

    /**
     * Determines the allowed filter attributes and their types for the given entity.
     *
     * @param parameter The method parameter
     * @return A map of allowed filter attributes and their corresponding types
     */
    private Map<String, Class<?>> getEntityAllowedFilters(MethodParameter parameter) {
        if (parameter.hasParameterAnnotation(ValidTestEventPageRequest.class)) {
            return TestEvent.getAllowedFilterAttributes();
        } else if (parameter.hasParameterAnnotation(ValidUserPageRequest.class)) {
            return User.getAllowedFilterAttributes();
        }
        throw new IllegalArgumentException("No allowed filters defined for this entity.");
    }

    /**
     * Determines the allowed sort attributes for the given entity.
     *
     * @param parameter The method parameter
     * @return A set of allowed sorting attributes
     */
    private Set<String> getEntityAllowedSorts(MethodParameter parameter) {
        if (parameter.hasParameterAnnotation(ValidTestEventPageRequest.class)) {
            return TestEvent.getAllowedSortAttributes();
        } else if (parameter.hasParameterAnnotation(ValidUserPageRequest.class)) {
            return User.getAllowedSortAttributes();
        }
        throw new IllegalArgumentException("No allowed sorts defined for this entity.");
    }

    /**
     * Converts raw filter parameters into a structured map with data types.
     *
     * @param rawFilters     The raw filter parameters from the request
     * @param allowedFilters The allowed filters with their expected data types
     * @return A structured filter map with values mapped to their types
     */
    private Map<String, Map<String, Class<?>>> convertFiltersToTypedMap(Map<String, String> rawFilters, Map<String, Class<?>> allowedFilters) {
        if (rawFilters.isEmpty()) {
            return null;
        }

        Map<String, Map<String, Class<?>>> filterWithTypes = new HashMap<>();
        for (String filterKey : rawFilters.keySet()) {
            if (!allowedFilters.containsKey(filterKey)) {
                throw new GenericValidationError("Invalid filter attribute: " + filterKey);
            }

            Class<?> type = allowedFilters.get(filterKey);
            if (type == null) {
                throw new RuntimeException("Filter type is not defined for: " + filterKey);
            }

            Map<String, Class<?>> filterValueType = Map.of(rawFilters.get(filterKey), type);
            filterWithTypes.put(filterKey, filterValueType);
        }
        return filterWithTypes;
    }
}
