package com.jnx.spring.annotation;

import java.lang.annotation.*;

/**
 * 成员属性自定义注解    XAutowired
 * @author 蒋楠鑫
 * @date 2018-11-16
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutowired {
	
	/**
	 * 注解里的value属性
	 * @return  默认为空字符串
	 */
	String value() default "";

}
