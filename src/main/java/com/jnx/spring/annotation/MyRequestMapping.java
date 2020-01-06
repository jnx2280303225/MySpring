package com.jnx.spring.annotation;

import java.lang.annotation.*;

/**
 * url路径自定义注解   XRequestMapping
 * @author 蒋楠鑫
 * @date 2019-09-05
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {
	
	/**
	 * 注解里的value属性
	 * @return  默认为空字符串
	 */
	String value() default "";
	
}
