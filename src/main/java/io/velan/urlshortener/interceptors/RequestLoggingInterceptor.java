package io.velan.urlshortener.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

// INTERCEPTOR -> Spring MVC level (Spring specific, not servlet api)
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        logger.info(
                "Interceptor PRE: {} {} handled by {}",
                request.getMethod(),
                request.getRequestURI(),
                handler.getClass().getSimpleName());

        return true;
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView)
            throws Exception {
        logger.info(
                "Interceptor POST: {} {} - ModelAndView: {}",
                request.getMethod(),
                request.getRequestURI(),
                modelAndView != null ? modelAndView.getViewName() : "null");
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        if (ex != null) {
            logger.error(
                    "Interceptor COMPLETION: {} {} - Exception: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    ex.getMessage());
        } else {
            logger.info(
                    "Interceptor COMPLETION: {} {} - Status: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus());
        }
    }
}
