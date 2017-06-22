package info.rmapproject.core.utils;

import org.springframework.context.annotation.Profile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies candidate spring beans for a testing environment
 *
 * @author emetsger@jhu.edu
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Profile("testing")
public @interface Testing {

}
