package com.jnx.spring.controller;

import com.jnx.spring.annotation.MyAutowired;
import com.jnx.spring.annotation.MyController;
import com.jnx.spring.annotation.MyRequestMapping;
import com.jnx.spring.service.TestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 测试控制器层
 * @author 蒋楠鑫
 * @date 2019-09-17
 */
@MyController
@MyRequestMapping("test")
public class TestController {
	
	@MyAutowired
	private TestService testService;
	
	/**
	 * 测试方法
	 */
	@MyRequestMapping("getMessage")
	public void getMessage(HttpServletRequest request, HttpServletResponse response){
		try {
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			PrintWriter writer = response.getWriter();
			writer.write("访问到了一个页面!服务端返回结果为:" + testService.getMessage());
			writer.write("111");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
