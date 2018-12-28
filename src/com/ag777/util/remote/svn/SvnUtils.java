package com.ag777.util.remote.svn;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.ag777.util.file.FileNioUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.remote.svn.exception.SVNCheckoutException;
import com.ag777.util.remote.svn.model.BasicWithCertificateTrustedAuthenticationManager;

/**
 * svn操作工具类(对svnkit的二次封装)
 * 
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>svnkit-1.9.3.jar</li>
 * <li>sqljet-1.1.11.jar</li>
 * <li>sequence-library-1.0.3.jar</li>
 * <li>antlr-runtime-3.5.2.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年12月28日,last modify at 2018年12月28日
 */
public class SvnUtils {
	
	private SvnUtils() {}
	
	/**
	 * 建立连接
	 * @param url
	 * @param account
	 * @param password
	 * @return
	 * @throws SVNException
	 */
	public static SVNRepository connect(String url, String account, String password) throws SVNException {
		return connect(getSvnUrl(url), account, password);
	}
	
	/**
	 * 建立连接
	 * @param url
	 * @param account
	 * @param password
	 * @return
	 * @throws SVNException
	 */
	public static SVNRepository connect(SVNURL url, String account, String password) throws SVNException {
		DAVRepositoryFactory.setup(); // 初始化
		ISVNAuthenticationManager authManager = new BasicWithCertificateTrustedAuthenticationManager("wanggaozhan",
				"Gzyz-2016-192912-82186691"); // 提供认证
		SVNRepository repos = SVNRepositoryFactory.create(url);
		repos.setAuthenticationManager(authManager); // 设置认证
		return repos;
	}
	
	/**
	 * 将url转化为SvnUrl对象
	 * @param url
	 * @return
	 * @throws SVNException
	 */
	public static SVNURL getSvnUrl(String url) throws SVNException {
		return SVNURL.parseURIEncoded(url); // 某目录在svn的位置，获取目录对应的URL。即版本库对应的URL地址
	}
	
	/**
	 * 获取对应日期的版本号
	 * @param repos
	 * @param date
	 * @return
	 * @throws SVNException
	 */
	public static long getReversion(SVNRepository repos, Date date) throws SVNException {
		return repos.getDatedRevision(date);
	}
	
	/**
	 * 获取svn版本日志数组
	 * @param repos
	 * @param startRevision
	 * @param endRevision
	 * @return
	 * @throws SVNException
	 */
	public static SVNLogEntry[] getSvnLogEntries(SVNRepository repos, long startRevision, long endRevision) throws SVNException {
		@SuppressWarnings("unchecked")
		Collection<SVNLogEntry> logEntries = repos.log(new String[]{""}, null,
				startRevision, endRevision, true, true);
		return logEntries.toArray(new SVNLogEntry[0]);
	}
	
	/**
	 * 根据版本日志数组获取版本号列表
	 * @param logEntries
	 * @return
	 */
	public static List<Long> getVersionList(SVNLogEntry[] logEntries) {
		List<Long> result = ListUtils.newArrayList();
		for (SVNLogEntry svnLogEntry : logEntries) {
			result.add(svnLogEntry.getRevision());
		}
		return result;
	}
	
	/**
	 * 根据起止日期获取版本号列表
	 * @param repos
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SVNException
	 */
	public static List<Long> getVersionList(SVNRepository repos, Date startDate, Date endDate) throws SVNException {
		long startRevision = getReversion(repos, startDate);
		long endRevision = getReversion(repos, endDate);
		SVNLogEntry[] logEntries = getSvnLogEntries(repos, startRevision, endRevision);
		return getVersionList(logEntries);
	}
	
	/**
	 * 检出某个版本的单个svn文件
	 * @param version 版本
	 * @param repos svn连接
	 * @param filePath 文件路径
	 * @param outFilePath 导出文件路径
	 * @return
	 * @throws SVNCheckoutException
	 */
	public long downLoadFileFromSVN(SVNRepository repos, long version, String filePath, String outFilePath) throws SVNCheckoutException {
		SVNNodeKind node = null;
		try {
			if(version == 0){
				version = repos.getLatestRevision();
			}
			node = repos.checkPath(filePath, version);
		} catch (SVNException e) {
			throw new SVNCheckoutException("SVN检测不到该文件:" + filePath, e);
		}
		if (node != SVNNodeKind.FILE) {
			throw new SVNCheckoutException(node.toString() + "不是文件");
		}
		SVNProperties properties = new SVNProperties();
		try {
			OutputStream outputStream = FileNioUtils.getOutputStream(outFilePath);
			repos.getFile(filePath, version, properties, outputStream);
			outputStream.close();
		} catch (SVNException e) {
			throw new SVNCheckoutException("获取SVN服务器中的" + filePath + "文件失败", e);
		} catch (IOException e) {
			throw new SVNCheckoutException("SVN check out file faild.", e);
		}
		return Long.parseLong(properties.getStringValue("svn:entry:revision"));
	}
	
	/**
	 * checkout最新版本
	 * @param url
	 * @param account
	 * @param password
	 * @param workPath
	 * @return 导出版本
	 * @throws SVNException
	 */
	public static long checkout(String url, String account, String password, String workPath) throws SVNException {
		ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		//实例化客户端管理类
		SVNClientManager ourClientManager = SVNClientManager.newInstance((DefaultSVNOptions) options, account, password);
		//要把版本库的内容check out到的目录
		//FIle wcDir = new File("d:/test")
		File wcDir = new File(workPath);
		//通过客户端管理类获得updateClient类的实例。
		SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
		 //sets externals not to be ignored during the checkout
		updateClient.setIgnoreExternals(false);
		//执行check out 操作，返回工作副本的版本号。
		return updateClient.doCheckout(getSvnUrl(url), wcDir, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY,false);
	}
	
	
	public static void main(String[] args) throws SVNException {
		String url = "";
		String account = "xxxx";
		String password = "xxx";
		String tempPath = "f:/test/";
//		SVNRepository repos = connect(url, account, password);
//		try {
//			Console.prettyLog(getSvnLogEntries(repos, 0, -1));
//		} finally {
//			repos.closeSession();
//		}
		long version = checkout(url, account, password, tempPath);
		System.out.println("检出版本["+version+"]至"+tempPath);
	}
	
}
