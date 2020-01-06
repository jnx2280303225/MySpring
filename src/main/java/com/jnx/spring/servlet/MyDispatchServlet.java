package com.jnx.spring.servlet;

import com.jnx.spring.annotation.MyAutowired;
import com.jnx.spring.annotation.MyController;
import com.jnx.spring.annotation.MyRequestMapping;
import com.jnx.spring.annotation.MyService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 自定义核心分发器实体类
 * @author 蒋楠鑫
 * @date 2019-09-05
 */
public class MyDispatchServlet extends HttpServlet {
	
	private static final long serialVersionUID = -2742759679965876287L;
	
	/**
	 * 属性配置文件
	 */
	private Properties contextConfig = new Properties();
	/**
	 * 要注入的类名称的集合
	 */
	private List<String> classNameList = new ArrayList<>();
	/**
	 * IOC容器集合
	 */
	private Map<String, Object> iocMap = new HashMap<>();
	/**
	 * url对应的方法的映射集合
	 */
	private Map<String, Method> handlerMapping = new HashMap<>();
	
	/**
	 * Get请求
	 * @param request   http请求对象
	 * @param response  http响应对象
	 * @throws ServletException     服务器异常
	 * @throws IOException  IO异常
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}
	
	/**
	 * Post请求
	 * @param request   http请求对象
	 * @param response  http响应对象
	 * @throws ServletException     服务器异常
	 * @throws IOException  IO异常
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doDispatch(request,response);
		} catch (Exception e) {
			e.printStackTrace();
			response.getWriter().write("500 服务器系统错误!");
		}
	}
	
	/**
	 * 运行阶段拦截并且分发请求
	 * @param request   http请求对象
	 * @param response  http响应对象
	 * @throws InvocationTargetException    无法匹配资源异常
	 * @throws IllegalAccessException   无访问权限异常
	 * @throws IOException  IO异常
	 */
	private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, IOException {
		//前端请求的url
		String url = request.getRequestURI();
		//前端请求的上下文路径
		String contextPath = request.getContextPath();
		//获取控制器的请求路径及请求的方法
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		//打印url
		System.out.println("前端请求的url为:" + url);
		//判断请求的路径是否存在,不存在返回404
		if (!handlerMapping.containsKey(url)){
			response.getWriter().write("404 NOT FOUND!请求资源不存在!");
			return;
		}
		//存在就获取请求的方法
		Method method = handlerMapping.get(url);
		//打印调用的方法名称
		System.out.println("前端访问的控制层方法为:" + method);
		//获取实体类对应的名称
		String beanName = method.getDeclaringClass().getName();
		System.out.println("访问控制器类为:" + iocMap.get(beanName));
		//调用该方法,多个参数增加列数列表,按顺序对应
		method.invoke(iocMap.get(beanName),request,response);
		System.out.println("通过反射调用方法:" + iocMap.get(beanName) + "---"+method.getName());
	}
	
	/**
	 * 系统初始化配置的方法
	 * @param servletConfig    服务器配置
	 * @throws ServletException     服务器异常
	 */
	@Override
	public void init(ServletConfig servletConfig) throws ServletException{
		//1.加载配置文件
		loadConfig(servletConfig.getInitParameter("contextConfigLocation"));
		//2.扫描所有的类
		scanPackage(contextConfig.getProperty("scan-package"));
		//3.初始化IOC容器将所有的类实例保存到IOC容器中
		doInstance();
		//4.依赖注入
		doAutoWired();
		//5.初始化handlerMapping
		initHandlerMapping();
		//6.打印相关数据
		printData();
	}
	
	/**
	 * 1.加载配置文件
	 * @param contextConfigLocation     配置文件位置
	 */
	private void loadConfig(String contextConfigLocation){
		//通过IO将配置文件加载到内存
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
		try {
			contextConfig.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 2.扫描全项目的相关的类
	 * @param scanPackage   扫描的包路径
	 */
	private void scanPackage(String scanPackage){
		//将包名的.替换成/
		URL resource = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
		if (resource == null){
			return;
		}
		//获取url中访问的文件对象
		File classPath = new File(resource.getFile());
		for (File file : Objects.requireNonNull(classPath.listFiles())){
			if (file.isDirectory()){
				//如果路径是个文件夹就递归
				scanPackage(scanPackage+"."+file.getName());
			} else {
				if (!file.getName().endsWith(".class")){
					//跳过非字节码文件
					continue;
				}
				//保存类全名
				String className = (scanPackage+"."+file.getName()).replace(".class","");
				classNameList.add(className);
				System.out.println("已扫描到类:" + className);
			}
		}
	}
	
	/**
	 * 3.初始化IOC容器将所有的类实例保存到IOC容器中
	 */
	private void doInstance(){
		//非空判断
		if (classNameList.isEmpty()) {
			return;
		}
		//初始化IOC容器
		try {
			for (String className : classNameList){
				Class<?> aClass = Class.forName(className);
				String name = aClass.getName();
				//判断注解的类型(控制器,服务层,数据持久层),目前只做了控制器和服务层便于测试
				if (aClass.isAnnotationPresent(MyController.class)){
					//该类是控制器
					Object object = aClass.newInstance();
					iocMap.put(name,object);
					System.out.println(name + "为控制器,已加载到IOC容器中");
				} else if (aClass.isAnnotationPresent(MyService.class)){
					MyService annotation = aClass.getAnnotation(MyService.class);
					//该类是服务层,且注解里有自定义的值
					if (!"".equals(annotation.value())){
						name = annotation.value();
					}
					Object object = aClass.newInstance();
					iocMap.put(name,object);
					System.out.println(name + "为服务层实现类,已加载到IOC容器中");
					//找类的接口
					Class[] interfaces = aClass.getInterfaces();
					for (Class anInterface : interfaces) {
						//判断类名是否重复
						String string = anInterface.getName();
						if (iocMap.containsKey(string)){
							throw new Exception(string + "类名重复,已存在相同的服务层接口!");
						}
						iocMap.put(string,object);
						System.out.println(string + "为服务层接口,已加载到IOC容器中!");
					}
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 4.依赖注入
	 */
	private void doAutoWired(){
		//非空判断
		if (iocMap.isEmpty()){
			return;
		}
		//遍历ioc容器
		for (Map.Entry<String, Object> entry : iocMap.entrySet()){
			//类中成员属性的集合
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields){
				//没有依赖注入的注解就跳过
				if (!field.isAnnotationPresent(MyAutowired.class)){
					continue;
				}
				//只要有注解就要加载即使是private
				field.setAccessible(true);
				String fieldName = field.getType().getName();
				System.out.println("开始加载属性---" + fieldName);
				//获取注解对应的类
				MyAutowired annotation = field.getAnnotation(MyAutowired.class);
				String name = annotation.value().trim();
				//如果value没有值就属性的类名
				if ("".equals(name)){
					name = fieldName;
				}
				try {
					field.set(entry.getValue(),iocMap.get(name));
					System.out.println(name + "依赖注入完成");
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 5.初始化handlerMapping
	 */
	private void initHandlerMapping(){
		//非空判断
		if (iocMap.isEmpty()){
			return;
		}
		for (Map.Entry<String, Object> entry : iocMap.entrySet()){
			//获取类对象
			Class<?> aClass = entry.getValue().getClass();
			//只解析控制器层
			if (!aClass.isAnnotationPresent(MyController.class)){
				continue;
			}
			String baseUrl = "";
			//如果类上面有RequestMapping就拼接url
			if (aClass.isAnnotationPresent(MyRequestMapping.class)){
				MyRequestMapping annotation = aClass.getAnnotation(MyRequestMapping.class);
				baseUrl = annotation.value();
			}
			//获取方法上的url的集合
			for (Method method : aClass.getMethods()) {
				if (!method.isAnnotationPresent(MyRequestMapping.class)){
					continue;
				}
				//拼接url
				MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
				String url = ("/" + baseUrl + "/" + annotation.value()).replaceAll("/+","/");
				//放入容器
				handlerMapping.put(url,method);
				System.out.println(url + "已初始化");
			}
		}
	}
	
	/**
	 * 6.启动时打印各类容器数据
	 */
	private void printData(){
		System.out.println("配置文件:" + contextConfig.propertyNames());
		System.out.println("类名称集合:" + classNameList);
		System.out.println("IOC容器:" + iocMap);
		System.out.println("URL映射的集合" + handlerMapping);
		System.out.println("======启动成功======");
	}
}
