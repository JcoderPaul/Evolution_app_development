package me.oldboy.config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * Registers the DelegatingFilterProxy to use the springSecurityFilterChain
 * before any other registered Filter.
 */
public class AppSecurityInitializer extends AbstractSecurityWebApplicationInitializer {
}
