package com.jnx.spring.service.impl;

import com.jnx.spring.annotation.MyService;
import com.jnx.spring.service.TestService;

/**
 * 测试服务层实现类
 * @author 蒋楠鑫
 * @date 2019-09-17
 */
@MyService
public class TestServiceImpl implements TestService {
	/**
	 * 测试实现类方法
	 *
	 * @return 返回的字符串
	 */
	@Override
	public String getMessage() {
		return "收到数据库返回的数据";
	}
}
