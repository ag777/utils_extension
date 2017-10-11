package com.ag777.util.db.bonecp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.ag777.util.db.DbHelper;
import com.ag777.util.lang.IOUtils;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

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
 * @version create on 2017年10月11日,last modify at 2017年10月11日
 */
public class BonecpHelper {

	private BoneCP connectionPool;
	
	private BonecpHelper(BoneCP connectionPool) {
		this.connectionPool = connectionPool;
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
	
	public static BonecpHelper init(String ip, int port, String dbName, String user, String password) {
		try {
			// load the database driver (make sure this is in your classpath!)
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("缺少jdbc驱动包");
		}

		try {
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(DbHelper.getDbUrlString(ip, port, dbName)); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
			config.setUsername(user);
			config.setPassword(password);
			// 设置每60秒检查数据库中的空闲连接数
			config.setIdleConnectionTestPeriod(60, TimeUnit.SECONDS);
			// 设置连接空闲时间
			config.setIdleMaxAge(240, TimeUnit.SECONDS);
			// 设置每个分区中的最大连接数 30
			config.setMaxConnectionsPerPartition(30);
			// 设置每个分区中的最小连接数 10
			config.setMinConnectionsPerPartition(10);
			// 当连接池中的连接耗尽的时候 BoneCP一次同时获取的连接数
			config.setAcquireIncrement(5);
			// 连接释放处理
			config.setReleaseHelperThreads(3);
			// 设置分区 分区数为3
			config.setPartitionCount(3);
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
