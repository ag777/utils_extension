package com.ag777.util.remote.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.nio.charset.Charset;
import javax.naming.AuthenticationException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.Console;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.exception.Assert;
import com.ag777.util.lang.model.Charsets;

/**
 * ftp操作辅助类
 * <p>
 * 线程不安全!!!
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>commons-net-xxx.jar</li>
 * </ul>
 * </p>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年04月13日,last modify at 2018年04月16日
 */
public class FtpHelper {

	public final static int PORT_DEFAULT = 21; //ftp默认端口号为21
	
	private FTPClient client;
	private boolean localPassiveMode = true; //被动传输
	private boolean encodingMode = true;
	private String[] ENCODINGS = {Charsets.GBK, Charsets.ISO_8859_1};
	
	public FtpHelper(FTPClient client) {
		this.client = client;
		modeLocalPassiveMode(localPassiveMode);
	}
	
	@Deprecated
	public FTPClient getClient() {
		return client;
	}
	
	/**
	 * 请务必在使用过后调用该方法,来关闭ftp连接
	 */
	public void dispose() {
		if(client == null && client.isConnected()) {
			try {
				client.logout();
				client.disconnect();
			} catch (IOException e) {
			}
		}
		client = null;
	}
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @return
	 * @throws SocketException FTP的IP地址可能错误，请正确配置。
	 * @throws IOException FTP的端口错误,请正确配置。
	 * @throws AuthenticationException ftp没有登录成功
	 */
	public static FtpHelper connect(
			String host,int port,  
            String userName, String password) throws SocketException, IOException, AuthenticationException {
		
		FTPClient client = null;
		try {
			client = new FTPClient();  
			client.connect(host, port);// 连接FTP服务器  
			client.login(userName, password);// 登陆FTP服务器  
	        if (FTPReply.isPositiveCompletion(client.getReplyCode())) {
	            FtpHelper helper = new FtpHelper(client);
	            try {	//尝试开启服务端对utf-8的支持，如果开启成功，则说明不需要再传输过程中对编码进行二次转换
	    			if(FTPReply.isPositiveCompletion(
	    					client.sendCommand(
	    							"OPTS UTF8", "ON"))) {
	    				helper.modeEncoding(false);
	    			}
	    		} catch (IOException e) {
	    		}
	            return helper;
	        } else {
	            throw new AuthenticationException("未连接到FTP,用户名或密码错误!");  
	        }
		} catch(Exception ex) {
			if(client != null && client.isConnected()) {
				try{
					client.disconnect();
				} catch(IOException e) {	//断开连接失败
				}
			}
			throw ex;
		}
	}
	
	/**
	 * 是否开启被动传输模式,默认开启
	 * @param localPassiveMode
	 * @return
	 */
	public FtpHelper modeLocalPassiveMode(boolean localPassiveMode) {
		this.localPassiveMode = localPassiveMode;
		// 设置PassiveMode传输
		if(localPassiveMode) {
			 client.enterLocalPassiveMode();
		} else {
			client.enterLocalActiveMode();
		}
		return this;
	}
	
	/**
	 * 是否开启编码转换功能，默认开启
	 * @param encodingMode
	 * @return
	 */
	public FtpHelper modeEncoding(boolean encodingMode) {
		this.encodingMode = encodingMode;
		return this;
	}
	
	/**
	 * 设置本地及文件编码
	 * <p>
	 * 	这个设置项是为了防止文件路径带中文导致ftp传输失败,或者中文乱码问题
	 * 根据网上的文章本地编码为GBK，服务端编码为ISO-8859-1
	 * 调用该方法意味着开启编码转换功能
	 * </p>
	 * 
	 * @param encodingLocal
	 * @param encodingServer
	 * @return
	 */
	public FtpHelper setEncoding(String encodingLocal, String encodingServer) {
		ENCODINGS = new String[]{encodingLocal, encodingServer};
		modeEncoding(true);
		return this;
	}
	
	/**
	 * 上传文件或目录
	 * 
	 * @param localFilePath 本地文件路径
	 * @param targetDirs 逐级目录,比如a/b/就是{"a","b"},根目录传null
	 * @return
	 * @throws IOException
	 */
	public synchronized boolean uploadFile(String localFilePath, String[] targetDirs) throws IOException {
		Assert.notNull(client, "该ftpClient已经被释放");
		Assert.notExisted(localFilePath, "需要ftp传输的文件不存在:"+localFilePath);
		
		try {
			if(!ListUtils.isEmpty(targetDirs)) {	//逐级进入对应目录并创建文件夹
				for (String dir : targetDirs) {
					dir = encode(dir+File.separator);
					client.makeDirectory(dir);
					client.changeWorkingDirectory(dir);
				}
				
			}
			return uploadFile(localFilePath, (String)null);
		} catch(Exception ex) {
			throw ex;
		} finally {
			if(targetDirs != null) {	//为了在操作完成后回到根目录
				for (int i=0; i<targetDirs.length; i++) {
					client.changeToParentDirectory();
				}
				
			}
		}
	}
	
	
	/**
	 * 读取文件
	 * @param targetPath
	 * @param charset 默认utf-8
	 * @return
	 * @throws IOException
	 */
	public InputStream readFile(String targetPath, Charset charset) throws IOException {
		if(charset == null) {
			charset = Charsets.utf8();
		}
		try {
			client.setControlEncoding(charset.toString()); // 中文支持  
			client.setFileType(FTPClient.BINARY_FILE_TYPE);
//			client.changeWorkingDirectory(ftpPath);
	        return client.retrieveFileStream(encode(targetPath));	//需要注意编码转换
		} catch(Exception ex) {
			throw ex;
		} finally {
//			IOUtils.close(in);	//IOUtils.readLines方法中已关闭该输入流
		}
	}
	
	public boolean isDirectory(String targetPath) throws IOException {
		FTPFile[] files = client.listFiles(targetPath);
		return files.length>1;
	}
	
	/**
	 * 获取文件()
	 * <p>
	 * 	包含.和..目录
	 * </p>
	 * @param targetPath
	 * @return
	 * @throws IOException 
	 */
	public FTPFile[] listFiles(String targetPath) throws IOException {
		return client.listFiles(encode(targetPath));
	}
	
	/**
	 * 删除文件或文件夹(文件夹删除还有问题)
	 * <p>
	 * 	试了各种方法都删不掉空目录，极小概率成功，原因未知，望dalao指点
	 * </p>
	 * 
	 * @param targetPath
	 * @return
	 */
	@Deprecated
	public synchronized boolean delete(String targetPath) {
		
		try {	//文件夹
			targetPath = encode(targetPath);
			FTPFile[] files = client.listFiles(targetPath);
			if(files.length == 0) {	//文件不存在
				return true;
			}
			if(files.length == 1) {	//文件
				return client.deleteFile(targetPath);
			} else {	//文件夹
				return deleteDir(targetPath, files);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/*=============内部方法=================*/
	private boolean uploadFile(String localFilePath, String targetDir) throws IOException {
		Assert.notNull(client, "该ftpClient已经被释放");
		Assert.notExisted(localFilePath, "需要ftp传输的文件不存在:"+localFilePath);
		
		try {
			File file = new File(localFilePath);
			if(targetDir != null) {
				String dir = encode(targetDir);
				client.makeDirectory(dir);
				client.changeWorkingDirectory(dir);
			}
			
			if(file.isDirectory()) {		//文件夹
				String dir = file.getName()+File.separator;
				File[] files = file.listFiles();
				if(ListUtils.isEmpty(files)) {	//没有子文件
					client.makeDirectory(encode(dir));
				} else {
					for (File subFile : files) {
						boolean flag = uploadFile(subFile.getAbsolutePath(), file.getName()+File.separator);
						if(!flag) {
							return false;
						}
					}
				}
				
			} else {			//文件
				boolean flag = uploadSingleFile(localFilePath, file.getName());
				if(!flag) {
					Console.err("ftp传输失败:"+localFilePath);
					return false;
				}
			}
			return true;
		} catch(Exception ex) {
			throw ex;
		} finally {
			if(targetDir != null) {	//为了在操作完成后能回到根目录
				client.changeToParentDirectory();
			}
		}
	}
	
	/**
	 * 上传单个文件
	 * 
	 * @param localFilePath 需要上传的本地文件路径
	 * @param targetPath ftp上传的目标路径
	 * @return
	 * @throws IOException
	 */
	private boolean uploadSingleFile(String localFilePath, String fileName) throws IOException {
		Assert.notNull(client, "该ftpClient已经被释放");
//		Assert.notExisted(localFilePath, "需要ftp传输的文件不存在:"+localFilePath);
		if(!new File(localFilePath).isFile()) {
			throw new IllegalArgumentException("只能传输单个文件");
		}
		InputStream in = null;
		try {
			
			// 设置以二进制流的方式传输  
	        client.setFileType(FTPClient.BINARY_FILE_TYPE);
	        
	        // FTPFile[] files = ftpClient.listFiles(new String(remoteFileName));  
	        in = FileUtils.getInputStream(localFilePath);
	        return client.storeFile(encode(fileName), in);  
		} catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			IOUtils.close(in);
		}
	}
	
	private boolean deleteDir(String targetPath, FTPFile[] files) throws IOException {
		if(!ListUtils.isEmpty(files)) {
			client.changeWorkingDirectory(targetPath);
			try {
				for (FTPFile file : files) {
					String name = encode(file.getName());
					if(".".equals(name) || "..".equals(name)) {
						continue;
					}
					if(file.isDirectory()) {	//文件夹
						String dir = name+File.separator;
						
						if(!deleteDir(name+File.separator, client.listFiles(dir))) {
							Console.err("删除目录失败:"+file.getName());
							return false;
						}
						if(!client.removeDirectory(dir)) {
							Console.err("删除目录失败:"+file.getName());
							return false;
						}
					} else {	//文件
						System.out.println("删除文件"+file.getName());
						if(!client.deleteFile(name)) {
							Console.err("删除文件失败:"+file.getName());
							return false;
						}
					}
					
				}
				return client.removeDirectory(targetPath);
			} catch(Exception ex) {
				throw ex;
			} finally {
				client.changeToParentDirectory();
			}
			
			
		}
		return false;
	}
	
	/**
	 * 转换文件名/路径编码
	 * @param fileName
	 * @return
	 */
	private String encode(String fileName) {
		if(encodingMode) {
			try {
				return new String(fileName.getBytes(ENCODINGS[0]), ENCODINGS[1]);
			} catch (UnsupportedEncodingException e) {
			}
		}
		return fileName;
	}
    
	
	public static void main(String[] args) {
		FtpHelper helper = null;
		try {
			helper = connect(
					"xxxx", 21, "xx", "xxxx");
			boolean flag = false;
//			flag = helper.uploadFile("f:\\a\\", new String[]{"临时"});
//			System.out.println(flag);
//			flag = helper.uploadFile("f:\\啊.txt", new String[]{"aaa"});
//			System.out.println(flag);
//			System.out.println(helper.readFile("临时/a/有内容.txt", Charsets.utf8()).size());
			flag = helper.delete("a");
			System.out.println(flag);
		} catch (AuthenticationException | IOException e) {
			e.printStackTrace();
		} finally {
			if(helper != null) {
				helper.dispose();
			}
		}
	}
}
