package dev.mirodil.testing_system.configs;

import dev.mirodil.testing_system.services.PageWithFilterRequest;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;
import java.util.stream.Collectors;

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

        Map<String, String> filters = webRequest.getParameterMap().entrySet().stream()
                .filter(entry -> !entry.getKey().equals("page") && !entry.getKey().equals("size") && !entry.getKey().equals("sort"))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));

        return new PageWithFilterRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), filters);
    }
}
