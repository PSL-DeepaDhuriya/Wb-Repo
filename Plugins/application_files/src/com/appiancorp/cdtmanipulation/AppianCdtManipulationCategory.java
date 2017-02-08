package com.appiancorp.cdtmanipulation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.appiancorp.suiteapi.expression.annotations.Category;

@Category("appianCdtManipulationCategory")
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AppianCdtManipulationCategory {

}
