package com.ag777.util.web.springmvc;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 有关springmvc手动执行事务工具类
 * <p>
 * [service层@Transactional事务控制使用以及注意]<br/>
	https://blog.csdn.net/seapeak007/article/details/78033870<br/>
	<p>
		非事务声明方法调用事务声明方法，则事务失效。使用了@Transactional的方法，对同一个类里面的方法调用， @Transactional无效。比如有一个类Test，它的一个方法A，A再调用Test本类的方法B（不管B是否public还是private），但A没有声明注解事务，而B有。则外部调用A之后，B的事务是不会起作用的。（经常在这里出错）
	</p>
	[进一步]<br/>
	https://blog.csdn.net/liuwen276/article/details/78481430<br/>
	<p>
		至于前面的事务问题，只要避开Spring目前的AOP实现上的限制，要么都声明要事务，要么分开成两个类，要么直接在方法里使用编程式事务，那么一切OK
	</p>
	[方法实现参考]<br/>
	https://blog.csdn.net/supingemail/article/details/51183116
 *	<p>
 *	需要jar包(springmvc框架):
 * <ul>
 * <li>spring-beans-4.3.6.RELEASE.jar</li>
 * <li>spring-context-4.3.6.RELEASE.jar</li>
 * <li>spring-core-4.3.6.RELEASE.jar</li>
 * <li>spring-jdbc-4.3.6.RELEASE.jar</li>
 * <li>spring-tx-4.3.6.RELEASE.jar</li>
 * </ul>
 *	</p>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年11月14日,last modify at 2018年11月14日
 */
public class TransactionUtils {

	/*
	 * 事务级别
	 * 事务的传播行为为PROPAGATION_REQUIRES_NEW，挂起当前线程绑定的事务，取消当前事务的sessionHolder和connectionHolder，并保存该信息以便后期恢复保存点
	 */
	private static int level = TransactionDefinition.PROPAGATION_REQUIRES_NEW;
	private TransactionUtils() {}
	
	/**
	 * 创建并执行事务
	 * <p>
	 * 这是在有 ApplicationContext 的情况下
	 * </p>
	 * @param ctx
	 * @param transaction
	 * @return
	 */
	public static <T>T tx(ApplicationContext ctx, Transaction<T> transaction) {
		DataSourceTransactionManager txManager = (DataSourceTransactionManager) ctx
				.getBean("txManager");
		TransactionStatus status = txManager.getTransaction(getDef()); // 获得事务状态
		try {
			T result = transaction.tx();
			txManager.commit(status);
			return result;
		} catch(Throwable ex) {
			txManager.rollback(status);	//事务回滚
			throw ex;
		}
	}
	
	/**
	 * 创建并执行事务
	 * <p>
	 * 没有 ApplicationContext 的情况下
	 * </p>
	 * @param txManager
	 * @param transaction
	 * @return
	 */
	public static <T>T tx(DataSourceTransactionManager txManager, Transaction<T> transaction) {
		TransactionStatus status = txManager.getTransaction(getDef()); // 获得事务状态
		try {
			T result = transaction.tx();
			txManager.commit(status);
			return result;
		} catch(Throwable ex) {
			txManager.rollback(status);	//事务回滚
			throw ex;
		}
	}
	
	/**
	 * 创建一个新的事务配置
	 * @return
	 */
	public static DefaultTransactionDefinition getDef() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(level);// 事物隔离级别，开启新事务
		return def;
	}
	
	/**
	 * 执行事务的容器
	 * @author wanggz
	 *
	 * @param <T>
	 */
	public static interface Transaction<T> {
		public T tx();
	}
}
