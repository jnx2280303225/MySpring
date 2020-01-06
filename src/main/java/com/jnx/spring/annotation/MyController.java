package com.jnx.spring.annotation;

import java.lang.annotation.*;

/**
 * 控制器层自定义注解    XController
 * @author 蒋楠鑫
 * @date 2019-09-05
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {
	
	/**
	 * 注解里的value属性
	 * @return  默认为空字符串
	 */
	String value() default "";
	
}
