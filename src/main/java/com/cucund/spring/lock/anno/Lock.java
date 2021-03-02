package com.cucund.spring.lock.anno;



import com.cucund.spring.lock.LockType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Lock {

	String value() default "";

	LockType lockType() default LockType.NATIVE ;

	int time() default 0;

	TimeUnit unit() default TimeUnit.SECONDS;

}
