package dev.mirodil.testing_system.configs;

import dev.mirodil.testing_system.exceptions.GenericValidationError;
import dev.mirodil.testing_system.models.TestEvent;
import dev.mirodil.testing_system.models.User;
import dev.mirodil.testing_system.utils.FilterCriteria;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom argument resolver for {@link PageWithFilterRequest} that automatically
 * extracts pagination, sorting, and filter parameters from request queries.
 * The resolver is also responsible for validation of these parameters.
 * <p>
 * Sorting and filtering attributes are validated based on entity-specific
 * annotations ({@code @ValidTestEventPageRequest}, {@code @ValidUserPageRequest}).
 * <p>
 * Example query: {@code ?scorePercentage>=70&scorePercentage<=90&isPassed=true,false}
 * <p>
 * Converts to List of {@link FilterCriteria} - {@code List<FilterCriteria<>(key, List<operators>, List<values>)>}:
 * <pre>
 * [
 *   new FilterCriteria<>("scorePercentage", List.of(">=", "<="), List.of(70, 90)), // Integer values
 *   new FilterCriteria<>("isPassed", List.of("="), List.of(true, false)) // Boolean values
 * ]
 * </pre>
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
    public PageWithFilterRequest resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Pageable pageable = defaultPageableResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        // Extract raw filters from query parameters
        Map<String, List<String>> rawFilters = extractFilterParameters(webRequest);

        // Determine allowed filters and sorts based on entity annotation
        Map<String, Class<?>> allowedFilters = getEntityAllowedFilters(parameter);
        Set<String> allowedSorts = getEntityAllowedSorts(parameter);

        List<FilterCriteria<?>> filters = convertFiltersToTypedList(rawFilters, allowedFilters);

        // Construct PageWithFilterRequest and validate allowed parameters
        PageWithFilterRequest pageRequest = new PageWithFilterRequest(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort(),
                filters
        );
        ValidationUtil.forceValidPageable(pageRequest, allowedSorts, allowedFilters);

        return pageRequest;
    }

    private Map<String, List<String>> extractFilterParameters(NativeWebRequest webRequest) {
        Map<String, List<String>> rawFilters = new HashMap<>();
        Iterator<String> iterator = webRequest.getParameterNames();

        while (iterator.hasNext()) {
            String paramName = iterator.next();
            if (paramName.equals("page") || paramName.equals("size") || paramName.equals("sort")) {
                continue;
            }

            String key = paramName;
            String[] filterValues = webRequest.getParameterValues(key);
            if (filterValues == null) {
                continue;
            }

            List<String> operatorValueList = new ArrayList<>();

            for (String value : filterValues) {
                String operator = "=";
                // Matches operator and value in the key
                Matcher matcher = Pattern.compile("([<>])(.*)").matcher(key);
                if (matcher.find()) {
                    operator = matcher.group(1);
                    // If value is not blank then, original query param must have "=". Otherwise, everything would be in key only
                    value = value.isBlank() ? matcher.group(2).trim() : "=" + value;
                    key = key.endsWith(operator) && !value.isBlank()
                            ? key.substring(0, key.length() - operator.length()).strip() // ?score>=10 -> key: score>, value: 10
                            : key.substring(0, key.length() - operator.length() - value.length()).strip(); // ?score>10 -> key: score>10, value: ""
                }

                if (value.isBlank()) {
                    continue;
                }

                String operatorAndValue = operator + value;
                // If current and previous operators are same, replace value
                if (!operatorValueList.isEmpty() && operatorValueList.getFirst().startsWith(operator)) {
                    operatorValueList.clear();
                    operatorValueList.add(operatorAndValue);
                    continue;
                }

                operatorValueList.add(operatorAndValue);
            }

            if (rawFilters.containsKey(key)) {
                rawFilters.get(key).addAll(operatorValueList);
                continue;
            }
            rawFilters.put(key, operatorValueList);
        }
        return rawFilters;
    }

    /**
     * Converts raw filter parameters into a list of structured filter criteria.
     *
     * @param rawFilters     The raw filter parameters from the request
     * @param allowedFilters The allowed filters with their expected data types
     * @return A list of {@link FilterCriteria} objects representing structured filters
     * @throws GenericValidationError   If an invalid filter attribute is provided
     * @throws IllegalArgumentException If malformed filter value is passed
     * @throws RuntimeException         If filter type is not defined in an entity level
     */
    private List<FilterCriteria<?>> convertFiltersToTypedList(Map<String, List<String>> rawFilters, Map<String, Class<?>> allowedFilters) {
        if (rawFilters.isEmpty()) {
            return Collections.emptyList();
        }

        List<FilterCriteria<?>> filterCriteriaList = new ArrayList<>();
        final Pattern operatorValuePattern = Pattern.compile("([<>]=?|=)(.*)");

        for (Map.Entry<String, List<String>> entry : rawFilters.entrySet()) {
            String key = entry.getKey();

            if (!allowedFilters.containsKey(key)) {
                throw new GenericValidationError("Invalid filter attribute: " + key);
            }

            Class<?> type = allowedFilters.get(key);
            if (type == null) {
                throw new RuntimeException("Filter type is not defined for: " + key);
            }

            List<String> operators = new ArrayList<>();
            List<Object> convertedValues = new ArrayList<>();

            for (String operatorValue : entry.getValue()) {
                Matcher matcher = operatorValuePattern.matcher(operatorValue);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Malformed filter value for '" + key + "'. Expected format: operator + value (e.g., =true, <=70). Received: " + operatorValue);
                }

                String operator = matcher.group(1);
                String value = matcher.group(2).trim();

                // Support multi-value filters (e.g., ?status=active,inactive)
                if (value.contains(",") && operator.equals("=")) {
                    List<String> matchedValues = Arrays.stream(value.split(",", 10))
                            .filter(matchedValue -> !matchedValue.isBlank())
                            .toList();

                    operators.add(operator);
                    convertedValues.addAll(convertListValuesToTypes(key, matchedValues, type));
                } else {
                    convertedValues.addAll(convertListValuesToTypes(key, List.of(value), type));
                    operators.add(operator);
                }
            }

            if (!convertedValues.isEmpty()) {
                filterCriteriaList.add(new FilterCriteria<>(key, operators, convertedValues));
            }
        }

        return filterCriteriaList;
    }

    /**
     * Converts a list of string values to list of its expected data type.
     */
    private List<Object> convertListValuesToTypes(String key, List<String> values, Class<?> type) {
        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object> valuesWithType = new ArrayList<>();
        for (String value : values) {
            if (value.equalsIgnoreCase("NULL")) {
                valuesWithType.add(null);
                continue;
            }

            try {
                Object valueWithType = switch (type.getSimpleName()) {
                    case "Integer" -> Integer.parseInt(value);
                    case "Long" -> Long.parseLong(value);
                    case "Double" -> Double.parseDouble(value);
                    case "Boolean" -> Boolean.parseBoolean(value);
                    case "Instant" -> parseInstantValue(value);
                    default -> value;
                };
                valuesWithType.add(valueWithType);
            } catch (NumberFormatException e) {
                throw new GenericValidationError("Invalid value '" + value + "' for filter '" + key + "' (expected " + type.getSimpleName() + ").");
            } catch (DateTimeParseException e) {
                throw new GenericValidationError("Invalid value: '" + value + "' for filter '" + key + "' (expected ISO format [yyyy-MM-ddTHH:mm:ssZ or with offset +05:00] or date [yyyy-MM-dd]).");
            }
        }
        return valuesWithType;
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

    private Instant parseInstantValue(String value) {
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            LocalDate localDate = LocalDate.parse(value, dateOnlyFormatter);
            return localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        }
    }
}
