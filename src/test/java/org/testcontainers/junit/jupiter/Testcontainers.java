package org.testcontainers.junit.jupiter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Testcontainers {
    boolean disabledWithoutDocker() default false;
}
