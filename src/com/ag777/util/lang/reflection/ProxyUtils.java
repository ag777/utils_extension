package com.ag777.util.lang.reflection;

import com.ag777.util.lang.function.TriConsumer;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.BiConsumer;

/**
 * java动态代理示例工具类
 * 
 * @author ag777
 * @version create on 2020年10月23日,last modify at 2020年10月23日
 */
public class ProxyUtils {

	private ProxyUtils() {}

	/**
	 * 原生动态代理示例，特殊需求请自行实现
	 * <p>仅支持接口实现类，该代理会生成并返回另一个接口实现对象 
	 * @param obj 必须实现接口
	 * @param onBefore 在方法执行前执行
	 * @param onAfter 在方法执行后执行
	 * @param onException 在发生异常时执行，不会阻止异常抛出
	 * @return 接口，而非对象
	 */
	@SuppressWarnings("unchecked")
	public static <T, I>I proxy(T obj, BiConsumer<Method, Object[]> onBefore, TriConsumer<Method, Object[], Object> onAfter,  TriConsumer<Method, Object[], Throwable> onException) {
		if(obj == null) {
			return null;
		}
		return (I) Proxy.newProxyInstance(
				obj.getClass().getClassLoader(), 
				obj.getClass().getInterfaces(),
				(o, method, args) -> intercept1(obj, method, args, onBefore, onAfter, onException)
		);
	}
	
	/**
	 * cglib动态代理示例，特殊需求请自行实现
	 * <p>支持任意实现类，该代理会生成并返回另一个继承于该对象的新对象(所以不支持final和static类) 
	 * @param obj 必须实现接口
	 * @param onBefore 在方法执行前执行
	 * @param onAfter 在方法执行后执行
	 * @param onException 在发生异常时执行，不会阻止异常抛出
	 * @return 代理生成的对象
	 */
	public static <T>T proxyCgi(T obj, BiConsumer<Method, Object[]> onBefore, TriConsumer<Method, Object[], Object> onAfter,  TriConsumer<Method, Object[], Throwable> onException) {
		if(obj == null) {
			return null;
		}
		MethodInterceptor interceptor = (o, method, args, proxy) -> intercept1(obj, method, args, onBefore, onAfter, onException);
		
		return getProxyInstance(obj, interceptor);
	}
	
	
	/**
	 * 拦截方法
	 * @param target 目标对象
	 * @param method 方法
	 * @param args 参数
	 * @param onBefore 在方法执行前执行
	 * @param onAfter 在方法执行后执行
	 * @param onException 在发生异常时执行，不会阻止异常抛出
	 * @return 新对象
	 * @throws Throwable 异常
	 */
	private static Object intercept1(Object target, Method method, Object[] args, BiConsumer<Method, Object[]> onBefore, TriConsumer<Method, Object[], Object> onAfter,  TriConsumer<Method, Object[], Throwable> onException) throws Throwable {
		try {
			//调用之前
			if(onBefore != null) {
				onBefore.accept(method, args);
			}
			Object result = method.invoke(target, args);
			//调用之后
			if(onAfter != null) {
				onAfter.accept(method, args, result);
			}
			return result;
		} catch (Throwable t) {
			if(onException != null) {
				onException.accept(method, args, t);	
			}
			throw t;
		}
		
	}
	
	//给目标对象创建一个代理对象
    @SuppressWarnings({ "unchecked" })
	private static <T>T getProxyInstance(T obj, MethodInterceptor interceptor){
        //1.工具类
        Enhancer en = new Enhancer();
        //2.设置父类
        en.setSuperclass(obj.getClass());
        //3.设置回调函数
        en.setCallback(interceptor);
        //4.创建子类(代理对象)
        return (T) en.create();
    }
}
