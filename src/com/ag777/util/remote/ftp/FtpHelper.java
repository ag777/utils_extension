package com.ag777.util.remote.ftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.List;

import javax.naming.AuthenticationException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.Console;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.exception.Assert;
import com.ag777.util.lang.exception.model.ValidateException;
import com.ag777.util.lang.interf.Disposable;
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
 * 关于主动模式和被动模式的参考资料:https://www.cnblogs.com/xiaohh/p/4789813.html
 * 中文乱码问题解决参考:https://blog.csdn.net/qq_35170213/article/details/80284730
 * </p>
 * 
 * @author ag777
 * @version create on 2018年04月13日,last modify at 2020年04月29日
 */
public class FtpHelper implements Disposable {

	public final static int PORT_DEFAULT = 21; //ftp默认端口号为21
	
	private FTPClient client;
	private boolean localPassiveMode = true; //被动传输
	private boolean encodingMode = true;
	private Charset[] ENCODINGS = {Charsets.GBK, Charsets.ISO_8859_1};
	
	public FtpHelper(FTPClient client) {
		this.client = client;
		modeLocalPassiveMode(localPassiveMode);
		client.setControlEncoding(ENCODINGS[0].toString());
		try {
			client.setFileType(FTPClient.BINARY_FILE_TYPE);
		} catch (IOException e) {
		}
	}
	
	@Deprecated
	public FTPClient getClient() {
		return client;
	}
	
	/**
	 * 请务必在使用过后调用该方法,来关闭ftp连接
	 */
	@Override
	public void dispose() {
		if(client != null && client.isConnected()) {
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
	 * <p>
	 *  主动FTP对FTP服务器的管理有利，但对客户端的管理不利。因为FTP服务器企图与客户端的高位随机端口建立连接，而这个端口很有可能被客户端的防火墙阻塞掉。
	 *  被动FTP对FTP客户端的管理有利，但对服务器端的管理不利。因为客户端要与服务器端建立两个连接，其中一个连到一个高位随机端口，而这个端口很有可能被服务器端的防火墙阻塞掉。
	 * </p>
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
	 * @param charsetLocal
	 * @param charsetServer
	 * @return
	 */
	public FtpHelper setEncoding(Charset charsetLocal, Charset charsetServer) {
		ENCODINGS = new Charset[]{charsetLocal, charsetServer};
		modeEncoding(true);
		return this;
	}
	
	/**
	 * 上传文件
	 * @param localFiles 本地文件列表
	 * @param targetDir 指定的远程目录
	 * @return
	 * @throws FileNotFoundException 本地文件不存在
	 * @throws IOException
	 */
	public boolean uploadFile(File[] localFiles, String targetDir) throws FileNotFoundException, IOException {
		/*文件判存*/
		for (File file : localFiles) {
			if(!file.exists()) {
				throw new FileNotFoundException("file not existed:"+file.getAbsolutePath());
			}
		}
		/*文件判存 end*/
		String currentWorkingDir = client.printWorkingDirectory();
		mkDirs(targetDir, false); //创建文件夹并进入对应目录
		try {
			for (File file : localFiles) {
				if(file.isDirectory()) {		//文件夹
					String dir = file.getName()+File.separator;
					File[] subFiles = file.listFiles();
					if(ListUtils.isEmpty(subFiles)) {	//空文件夹
						mkDirs(dir, true); //创建文件夹并返回当前目录
					} else {	//含有子文件,则递归上传
						boolean flag = uploadFile(subFiles, dir);
						if(!flag) {
							return false;
						}
					}
					
				} else {
					boolean flag = uploadSingleFile(file.getAbsolutePath(), file.getName());
					if(!flag) {
						Console.err("ftp传输失败:"+file.getAbsolutePath());
						return false;
					}
				}
			}
			return true;
		} catch(Exception ex) {
			throw ex;
		} finally {
			changeWorkingDirectory(currentWorkingDir);
		}
	}
	
	/** 
     * 下载文件
     * @param remoteFileName 远程文件路径
     * @param locaFileName 保存到本地文件路径
     * @throws FileNotFoundException 文件不存在
	 * @throws IOException 
     */  
    public void download(String targetFilePath,  
            String locaFilePath) throws FileNotFoundException, IOException {  
    	Assert.notNull(client, "该ftpClient已经被释放");
    	if(!fileExist(targetFilePath)) {
    		throw new FileNotFoundException("file not existed:"+targetFilePath);
    	}
        OutputStream out=null;
        try {
	        out = FileUtils.getOutputStream(locaFilePath);  
	        InputStream in = client.retrieveFileStream(encode(targetFilePath));
	        IOUtils.write(in, out, 1024);
        } finally {
        	 IOUtils.close(out);
        	 //详见https://www.jianshu.com/p/a90cc2aeefca
        	 client.completePendingCommand();
        }
    }  
	
    /**
     * 读取文件
     * @param targetPath
     * @param charset
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public List<String> readLines(String targetPath, Charset charset) throws FileNotFoundException,IOException {
    	if(!isFile(targetPath)) {
    		throw new FileNotFoundException("file not existed:"+targetPath);
    	}
    	if(charset == null) {
			charset = Charsets.UTF_8;
		}
		try {
	       InputStream in = readFile(targetPath);	//需要注意编码转换
	       return IOUtils.readLines(in, charset);
		}   finally {
			//详见https://www.jianshu.com/p/a90cc2aeefca
			client.completePendingCommand();
		}
    	
    }
    
    /**
     * 读取文件
     * @param targetPath
     * @param lineSparator
     * @param charset
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String readText(String targetPath, String lineSparator, Charset charset) throws FileNotFoundException, IOException {
    	if(!isFile(targetPath)) {
    		throw new FileNotFoundException("file not existed:"+targetPath);
    	}
    	if(charset == null) {
			charset = Charsets.UTF_8;
		}
    	try {
	       InputStream in = readFile(targetPath);	//需要注意编码转换
	       return IOUtils.readText(in, lineSparator, charset);
		}   finally {
			//详见https://www.jianshu.com/p/a90cc2aeefca
			client.completePendingCommand();
		}
    }
    
    @Deprecated
    public InputStream readFile(String targetPath) throws IOException {
    	return client.retrieveFileStream(encode(targetPath));	//需要注意编码转换
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
		FTPFile[] list = client.listFiles(encode(targetPath));
//		if(list != null) {
//			for (FTPFile ftpFile : list) {
//				ftpFile.setName(decode(ftpFile.getName()));
//			}
//		}
		return list;
	}
	
	public boolean fileExist(String targetPath) throws IOException {
		return client.listFiles(encode(targetPath)).length >0;
	}
	
	public boolean isFile(String targetPath) throws IOException {
		return client.listFiles(encode(targetPath)).length == 1;
	}
	
	public boolean isDirectory(String targetPath) throws IOException {
		return client.listFiles(encode(targetPath)).length > 1;
	}
	
	/**
	 * 删除文件或文件夹(文件夹删除参考了网上的代码)
	 * 
	 * @param targetPath
	 * @return
	 */
	public synchronized boolean delete(String targetPath) {
		
		try {	//文件夹
			FTPFile[] files = client.listFiles(encode(targetPath));
			if(files.length == 0) {	//文件不存在
				return true;
			}
			if(files.length == 1) {	//文件
				return client.deleteFile(encode(targetPath));
			} else {	//文件夹
				return deleteDir(targetPath, "");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 逐级创建目录
	 * @param dirPath
	 * @throws IOException
	 */
	public synchronized void mkDirs(String dirPath) throws IOException {
		mkDirs(dirPath, true);
	}
	
	/*=============内部方法=================*/
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
	
	/**
	 * 删除目录
	 * <p>
	 * 参考代码(需梯子):
	 * http://www.codejava.net/java-se/networking/ftp/how-to-remove-a-non-empty-directory-on-a-ftp-server
	 * </p>
	 * 
	 * @param targetDir
	 * @param currentDir
	 * @return
	 * @throws IOException
	 */
	private boolean deleteDir(String targetDir,
            String currentDir) throws IOException {
        String dirToList = targetDir;
        if (!currentDir.equals("") && (!currentDir.startsWith("/") && !currentDir.startsWith("\\"))) {
            dirToList += "/" + currentDir;
        }
        
        FTPFile[] subFiles = client.listFiles(encode(dirToList));
		
        if (subFiles != null && subFiles.length > 0) {;
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
               
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
               
                if (aFile.isDirectory()) {
                    // remove the sub directory
                	boolean deleted = deleteDir(dirToList, currentFileName);
                	if(!deleted) {
                		return false;
                	}
                } else {
                	//构造文件路径
                	 String filePath ="";
                     if (currentDir.equals("")) {
                         filePath = StringUtils.concat(targetDir, "/", currentFileName);
                     } else {
                     	filePath = StringUtils.concat(
                          		targetDir, "/", currentDir, "/", currentFileName);
                     }
                    // delete the file
                    boolean deleted = client.deleteFile(encode(filePath));
                    if (!deleted) {
                    	System.err.println("删除失败:"+filePath);
                    	return false;
                    }
                }
            }
           
        }
        
        // finally, remove the directory itself
        boolean removed = client.removeDirectory(encode(dirToList));
        if (!removed) {
        	System.err.println("删除目录失败:"+dirToList);
        	return false;
        }
        return true;
    }
	
	/**
	 * 逐级创建目录(失败时回退到原来的工作路径)
	 * @param dirPath 目标路径
	 * @param rebackWorkingDir 是否在创建成功时返回原来的工作目录
	 * @throws IOException
	 */
	private void mkDirs(String dirPath, boolean rebackWorkingDir) throws IOException {
		String currentWorkingDir = client.printWorkingDirectory();
		try {
			dirPath = encode(dirPath);
			if(changeWorkingDirectory(encode(dirPath))) {
				return;
			}
			String[] group = getFileTree(dirPath);
			
			for (String dirName : group) {
				if (!changeWorkingDirectory(dirName)) {
					if(!client.makeDirectory(dirName)) {
						throw new ValidateException("创建目录:"+dirName+"失败");
					}
					if(!changeWorkingDirectory(dirName)) {
						throw new ValidateException("切换至目录:"+dirName+"失败");
					}
				}
			}
		} catch (ValidateException e) {	//创建失败的情况返回根目录
			changeWorkingDirectory(currentWorkingDir);
			throw new IOException(e.getMessage());
		} catch(Throwable t) {
			changeWorkingDirectory(currentWorkingDir);
			throw t;
		} finally {
			if(rebackWorkingDir) {
				changeWorkingDirectory(currentWorkingDir);
			}
		}
	}
	
	/**
	 * "/a/b/c".split("[/\\\\]+")
	 * @param dirPath 目录路径
	 * @return 目录层级
	 */
	private String[] getFileTree(String dirPath) {
		dirPath = dirPath.replaceFirst("^[/\\\\]", "");
		return dirPath.split("[/\\\\]+");
	}
	
	private boolean changeWorkingDirectory(String pathname) throws IOException {
		return client.changeWorkingDirectory(pathname);
	}
	
	/**
	 * 转换文件名/路径编码
	 * @param fileName
	 * @return
	 */
	private String encode(String fileName) {
		if(encodingMode) {
			return new String(fileName.getBytes(ENCODINGS[0]), ENCODINGS[1]);
		}
		return fileName;
	}
    
//	private String decode(String fileName) {
//		if(encodingMode) {
//			return new String(fileName.getBytes(ENCODINGS[1]), ENCODINGS[0]);
//		}
//		return fileName;
//	}
	
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
