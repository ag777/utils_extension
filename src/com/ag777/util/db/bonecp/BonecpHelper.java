package com.ag777.util.db.bonecp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.ag777.util.db.DbHelper;
import com.ag777.util.lang.Console;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.StringUtils;
import com.google.common.util.concurrent.ListenableFuture;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.Statistics;

/**
 * bonecp简单封装
 * <p>
 * 项目建议在外部专门写一类来持有这个对象
 * 需要jar包:
 * <ul>
 * <li>bonecp-0.8.0.RELEASE.jar</li>
 * <li>guava-15.0.jar</li>
 * <li>slf4j-api-1.7.25.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2017年10月11日,last modify at 2017年12月12日
 */
public class BonecpHelper {

	private BoneCP connectionPool;
	
	private BonecpHelper(BoneCP connectionPool) {
		this.connectionPool = connectionPool;
	}
	
	public BoneCPConfig getConfig() {
		return connectionPool.getConfig();
	}
	
	public Optional<Connection> getConnection() {
		try {
			Connection conn = connectionPool.getConnection();
			if(conn != null) {
				return Optional.of(conn);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}
	
	public Optional<Connection> getAsyncConnection() {
		try {
			ListenableFuture<Connection> conn = connectionPool.getAsyncConnection();
			return Optional.of(conn.get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}
	
	/**
	 * 输出[已使用::xx;剩余:xx;总共:xx]的信息
	 */
	public void showStatus() {
		int used = connectionPool.getTotalLeased();
		int free = connectionPool.getTotalFree();
		int total = connectionPool.getTotalCreatedConnections();
		Console.log(StringUtils.concat("已经使用:", used,";剩余:", free, ";总共:",total));
	}
	
	/**
	 * 返回自带的分析类
	 * @return
	 */
	public Statistics showStatistics() {
		return connectionPool.getStatistics();
	}
	
	public static BonecpHelper init(String ip, int port, String dbName, String user, String password) {
		return init(ip, port, dbName, user, password, 30, 10, 3, 5);
	}
	
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param dbName
	 * @param user
	 * @param password
	 * @param minSize	每个分区中的最小连接数
	 * @param maxSize 每个分区中的最大连接数
	 * @param partitionCount	分区数
	 * @param requireOnce	连接池中的连接耗尽的时候 BoneCP一次同时获取的连接数
	 * @return
	 */
	public static BonecpHelper init(String ip, int port, String dbName, String user, String password,int minSize, int maxSize,int partitionCount,int requireOnce) {
		try {
			// load the database driver (make sure this is in your classpath!)
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("缺少jdbc驱动包");
		}

		try {
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(DbHelper.getDbUrlString(ip, port, dbName, DbHelper.DRIVER_CLASS_NAME_MYSQL)); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
			config.setUsername(user);
			config.setPassword(password);
			// 设置每60秒检查数据库中的空闲连接数
			config.setIdleConnectionTestPeriod(60, TimeUnit.SECONDS);
			// 设置连接空闲时间
			config.setIdleMaxAge(240, TimeUnit.SECONDS);
			// 设置每个分区中的最大连接数 30
			config.setMaxConnectionsPerPartition(maxSize);
			// 设置每个分区中的最小连接数 10
			config.setMinConnectionsPerPartition(minSize);
			// 当连接池中的连接耗尽的时候 BoneCP一次同时获取的连接数
			config.setAcquireIncrement(requireOnce);
			// 连接释放处理
			config.setReleaseHelperThreads(3);
//			config.setCloseConnectionWatch(true);
//			config.setCloseConnectionWatchTimeoutInMs(1000);
			// 设置分区 分区数为3
			config.setPartitionCount(partitionCount);
			BoneCP connectionPool = new BoneCP(config); // setup the connection pool
			return new BonecpHelper(connectionPool);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("数据库连接池初始化失败，请检查数据配置或数据库本身是否正常运行");
		}
	}
	
	public void close() {
		if(connectionPool != null) {
			connectionPool.shutdown();
			IOUtils.close(connectionPool);
		}
	}
}
