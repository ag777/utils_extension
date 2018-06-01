package com.ag777.util.lang.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ag777.util.lang.collection.ListUtils;

/**
 * 反射辅助类。
 * 
 * @author ag777
 * @version create on 2017年06月07日,last modify at 2017年09月30日
 */
public class ReflectionHelper<T> {

	private Class<T> mClazz;
	private Field[] fields;
	private Method[] methods;
	
	public ReflectionHelper(Class<T> clazz) {
		this.mClazz = clazz;
		initFields();
		initMethods();
	}
	
	public static <T>ReflectionHelper<T> get(Class<T> clazz) {
		return new ReflectionHelper<T>(clazz);
	}
	
	/*=============外部调用=================*/
	/**
	 * 获取类路径,如java.lang.String
	 * @return
	 */
	public String getName() {
		return mClazz.getName();
	}
	
	/**
	 * 实例化成对象,支持内部类,如果失败返回null
	 * @return
	 */
	public T newInstance()  {
		try {
			return ReflectionUtils.newInstace(mClazz);
		}catch(Exception ex) {
			
		}
		return null;
	}
	
	/**
	 * 获取方法
	 * @param name 方法名
	 * @param parameterTypes 参数类型
	 * @return
	 * @throws Exception
	 */
	public Method getMethod(String name, Class<?>... parameterTypes) throws Exception {
		return mClazz.getMethod(name, parameterTypes);
	}
	
	/**
	 * 执行方法
	 * @param name 方法名
	 * @param parameterTypes	参数类型
	 * @param obj 实例化的类(静态方法传null)
	 * @param args	实际的参数列表
	 * @return 返回值
	 * @throws Exception
	 */
	public Object invoke(String name, Class<?> parameterTypes, T obj, Object[] args) throws Exception{
		Method method = mClazz.getMethod(name, parameterTypes);
		return method.invoke(obj, args);
	}
	
	//--成员变量的相关
	/**
	 * 获取所有成员变量名
	 * @return
	 */
	public List<String> getFieldNameList() {
		List<String> fieldNameList = new ArrayList<>();
		for (Field field : fields) {
			fieldNameList.add(field.getName());
		}
		return fieldNameList;
	}
	
	/**
	 * 获取一个实例的所有变量名及对应的值
	 * @param obj
	 * @return
	 */
	public Map<String, Object> getFieldMap(T obj) {
		Map<String, Object> fieldMap = new HashMap<>();
		for (Field field : fields) {
			String name = field.getName();
			try {
				Object value = field.get(obj);
				fieldMap.put(name, value);
			} catch (Exception e) {
				fieldMap.put(name, null);
			}
		}
		
		return fieldMap;
	}
	
	/**
	 * 获取一个实例的所有类型为targetClazz的变量名及对应的值,
	 * 注意:返回类型为String,targetClazz
	 * @param obj
	 * @param targetClazz 筛选类型
	 * @return
	 */
	public <V>Map<String, V> getFieldMap(T obj, Class<V> targetClazz) {
		Map<String, V> fieldMap = new HashMap<>();
		for (Field field : fields) {
			Class<?> c = field.getType();
			//判断c是否为targetClazz或者其子类
			if(targetClazz == null || !(targetClazz.equals(c) || targetClazz.isAssignableFrom(c)) ) {
				continue;
			}
			
			String name = field.getName();
			try {
				@SuppressWarnings("unchecked")
				V value = (V) field.get(obj);
				fieldMap.put(name, value);
			} catch (Exception e) {
				fieldMap.put(name, null);
			}
		}
		
		return fieldMap;
	}
	
	/**
	 * 通过注释获取变量列表
	 * @param annotationClass
	 * @return
	 */
	public List<Field> getFieldListByAnnotation(Class<? extends Annotation> annotationClass) {
		List<Field> result = new ArrayList<>();
		for (Field field : fields) {
			if(field.isAnnotationPresent(annotationClass)) {
				result.add(field);
			}
		}
		return result;
	}
	
	//--方法相关
	/**
	 * 获取所有方法名
	 * @return
	 */
	public List<String> getMethodNameList() {
		List<String> methodNameList = new ArrayList<>();
		for (Method method : methods) {
			String name = method.getName();
			methodNameList.add(name);
		}
		return methodNameList;
	}
	
	/**
	 * 通过函数名获取方法列表
	 * @param methodName
	 * @return
	 */
	public List<Method> getMethodListByName(String methodName) {
		return getMethodList(new Filter() {

			@Override
			public boolean dofilter(Method method, String methodName, Class<?> returnType, int modifiers,
					Class<?>[] parameterTypes) {
				if(methodName.equals(methodName)) {
					return true;
				}
				return false;
			}
		});
	}
	
	/**
	 * 通过方法名模糊查询方法列表
	 * @param methodNameLike
	 * @return
	 */
	public List<Method> getMethodListByNameLike(String methodNameLike) {
		if(methodNameLike == null) {
			return new ArrayList<>();
		}
		return getMethodList(new Filter() {

			@Override
			public boolean dofilter(Method method, String methodName, Class<?> returnType, int modifiers,
					Class<?>[] parameterTypes) {
				if(methodName.contains(methodName)) {
					return true;
				}
				return false;
			}
		});
	}
	
	/**
	 * 通过注释获取方法列表
	 * @param annotationClass
	 * @return
	 */
	public List<Method> getMethodListByAnnotation(Class<? extends Annotation> annotationClass) {
		List<Method> result = new ArrayList<>();
		for (Method method : methods) {
			if(method.isAnnotationPresent(annotationClass)) {
				result.add(method);
			}
		}
		return result;
	}
	
	/**
	 * 获取所有方法名及对应的基本信息
	 * @return {"methodName":方法名,"parameterTypes":参数类型列表, "returnType":返回类型,"isStatic":是否为静态方法}
	 */
	public Map<String, Object> getMethodMap() {
		Map<String, Object> result = new HashMap<>();
		for (Method method : methods) {
				String methodName = method.getName();							//方法名
				Class<?>[] parameterTypes = method.getParameterTypes();	//参数类型列表
				Class<?> returnType = method.getReturnType();					//返回类型
				int modifiers = method.getModifiers();									//获取修饰符
				result.put("methodName", methodName);
				result.put("parameterTypes", parameterTypes);
				result.put("returnType", returnType);
				result.put("isStatic", Modifier.isStatic(modifiers));
		}

		return result;
	}
	
	/**
	 * 解析并获取方法名-{参数:方法}的树状结构
	 * @return
	 */
	public Map<String, Map<List<Class<?>>, Method>> getMethodTree() {
		Map<String, Map<List<Class<?>>, Method>> root = new HashMap<>();
		for (Method method : methods) {
			String name =method.getName();											//方法名
			List<Class<?>> parameterTypes = ListUtils.ofList(method.getParameterTypes());	//参数类型列表
			if(!root.containsKey(name)) {
				root.put(name, new HashMap<List<Class<?>>, Method>());
			}
			Map<List<Class<?>>, Method> item = root.get(name);
			item.put(parameterTypes, method);
		}
		return root;
	}
	
	/**
	 * 获取筛选后的方法集合
	 * @param filter dofilter方法return true代表加入返回列表
	 * @return
	 */
	public List<Method> getMethodList(Filter filter) {
		List<Method> result = new ArrayList<>();
		for (Method method : methods) {
			String methodName = method.getName();
			Class<?>[] parameterTypes = method.getParameterTypes();
			Class<?> returnType = method.getReturnType();
			if(filter.dofilter(method, methodName, returnType, method.getModifiers(), parameterTypes)) {
				result.add(method);
			}
		}
		return result;
	}
	
	//--注解相关
	/**
	 * 获取包含某个注解的field列表
	 * @param annotationClass
	 * @return
	 */
	public List<Field> findByAnnotation(Class<? extends Annotation> annotationClass) {
		List<Field> result = new ArrayList<>();
		for (Field field : fields) {
			if(field.isAnnotationPresent(annotationClass)) {
				result.add(field);
			}
		}
		return result;
	}
	
	//--泛型相关
	/**
	 * 不稳定方法
	 * 通过class获取泛型的类型列表(有几个列几个)
	 * 例:List<Integer> 获取到Integer.class
	 * 流程
	 * 	1.判断这个类是接口还是实体类,分情况执行
	 * 	2.接口分支
	 * 		①获取实现的接口数组
	 * 		②遍历接口数组，获取所有泛型列表
	 * 3.实体类分支
	 * 		①获取继承的类
	 * 		②直接获取泛型数组
	 * 4.遍历泛型数组，将引用(type)直接强转成class加入返回的列表
	 * @return 如果泛型类型未知，得到类型为Object,如果不存在泛型返回空list
	 */
	public List<Type> getClassListOfTs() {
		
		if(mClazz.isInterface()) {
			List<Type> classList = new ArrayList<>();
			Type[] type = mClazz.getGenericInterfaces();
			for (Type type2 : type) {
				classList.addAll(getTypeListOfTsByType(type2));
			}
			return classList;
		}else {
			Type type = mClazz.getGenericSuperclass();
			return getTypeListOfTsByType(type);
		}
		
	}
	
	/**
	 * 不稳定方法
	 * 通过class获取泛型的类型(取到的第一个)
	 * 例:List<Integer> 获取到Integer.class
	 * 流程
	 * 	1.判断这个类是接口还是实体类,分情况执行
	 * 	2.接口分支
	 * 		①取得第一个接口
	 * 		②获取其对应的第一个泛型
	 * 3.实体类分支
	 * 		①取得继承的类
	 * 		②直接获取第一个泛型
	 * 4.将引用(type)直接强转成class加入返回的列表
	 * @return 如果泛型类型未知，得到类型为Object,如果不存在泛型返回空list
	 */
	public Type getTypeOfT() {
		Type type = null;
		System.out.println(mClazz.getName());
		if(mClazz.isInterface()) {System.out.println(1);
			Type[] types = mClazz.getGenericInterfaces();
			if(types != null && types.length > 0) {
				
				type = types[0];
			}
		}else {System.out.println(2);
			type = mClazz.getGenericSuperclass();
		}
		return getTypeOfTByType(type);
	}
	
	/*=============工具类=================*/
	public interface Filter {
		boolean dofilter(Method method, String methodName, Class<?> returnType, int modifiers, Class<?>[] parameterTypes);
	}
	
	/*=============内部方法=================*/
	/**
	 * 初始化变量列表
	 */
	private void initFields() {
		fields = mClazz.getDeclaredFields();
		for (Field field : fields) {
	        field.setAccessible(true);	//这步是为了能获取到private修饰的变量值
		}
	}

	/**
	 * 初始化方法列表
	 */
	private void initMethods() {
		methods= mClazz.getDeclaredMethods();
		for (Method method : methods) {
			method.setAccessible(true);
		}
	}
	
	/**
	 * 从class的type(引用)获取这个class上的泛型类型集合
	 * @param typeOfClass
	 * @return 如果泛型类型未知，得到类型为Object,如果不存在泛型返回空list
	 */
	private static List<Type> getTypeListOfTsByType(Type typeOfClass) {
		
		List<Type> typeList = new ArrayList<>();
		if(typeOfClass!=null && typeOfClass instanceof ParameterizedType) {
			Type[] types = ((ParameterizedType) typeOfClass).getActualTypeArguments();
			for (Type t : types) {
				typeList.add(t);
			}
		}
		return typeList;
	}
	
	/**
	 * 从class的type(引用)获取这个class上第一个泛型类型
	 * @param typeOfClass
	 * @return 如果泛型类型未知，得到类型为Object,如果不存在泛型返回空list
	 */
	private static Type getTypeOfTByType(Type typeOfClass) {
		if(typeOfClass!=null && typeOfClass instanceof ParameterizedType) {
			Type[] types = ((ParameterizedType) typeOfClass).getActualTypeArguments();
			for (Type t : types) {
				return t;
			}
		}
		return null;
	}
}
