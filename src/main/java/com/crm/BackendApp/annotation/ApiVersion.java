package com.crm.BackendApp.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares API versioning at the controller class level.
 * When applied to a @RestController, all endpoint mappings in that class
 * are automatically prefixed with "/api/v{value}" (e.g. /api/v1).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {
    int value() default 1;
}
