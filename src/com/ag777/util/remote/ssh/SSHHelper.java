package com.ag777.util.remote.ssh;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;
import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.Console;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.interf.Disposable;
import com.ag777.util.lang.interf.ProgressListener;
import com.ag777.util.lang.model.Charsets;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * ssh操作辅助类
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>jsch-0.1.55.jar</li>
 * </ul>
 * 更新日志:http://www.jcraft.com/jsch/ChangeLog
 * </p>
 * 
 * @author ag777
 * @version last modify at 2018年12月07日
 */
public class SSHHelper implements Disposable, Closeable {

	private static Charset DEFAULT_CHARSET = Charsets.UTF_8;
	private static int TIMEOUT_CONNECT = 10000;	//超时时间
	private static int TIME_WAIT = 1000;	//执行完命令后等待一段时间再关闭通道
	
	private Session session;
	
	public SSHHelper(Session session) {
		this.session = session;
	}
	
	public Session getSession() {
		return session;
	}
	
	
	public static SSHHelper connect(String ip, int port, String user, String password) throws JSchException {
        return connect(ip, port, user, password, TIMEOUT_CONNECT);
	}
	
	public static SSHHelper connect(String ip, int port, String user, String password, int timeout) throws JSchException {
		JSch jsch = new JSch(); 
		//采用指定的端口连接服务器  
		Session session = jsch.getSession(user, ip, port);  
		 //设置登陆主机的密码  
        session.setPassword(password);  
        //设置第一次登陆的时候提示，可选值：(ask | yes | no)  
        session.setConfig("StrictHostKeyChecking", "no");
        //设置登陆超时时间
        session.connect(timeout); 
        return new SSHHelper(session);
	}
	
	 /** 
     * 递归删除执行. 
     * @param pathString 文件路径 
     * @param sftp sftp连接 
     * @throws SftpException 
     */  
    private static void deleteFile(final String filePath, final ChannelSftp sftp) throws SftpException {  
        @SuppressWarnings("unchecked")  
        Vector<LsEntry> vector = sftp.ls(filePath);  
        if (vector.size() == 1) { // 文件，直接删除  
            sftp.rm(filePath);  
        } else if (vector.size() == 2) { // 空文件夹，直接删除  
            sftp.rmdir(filePath);  
        } else {  
            String fileName = "";  
            // 删除文件夹下所有文件  
            for (LsEntry en : vector) {  
                fileName = en.getFilename();  
                if (".".equals(fileName) || "..".equals(fileName)) {  
                    continue;  
                } else {  
                	deleteFile(filePath + "/" + fileName, sftp);  
                }  
            }  
            // 删除文件夹  
            sftp.rmdir(filePath);  
        }  
    }  
    
    /**
     * 创建文件夹
     * <p>
     * 支持递归创建
     * </p>
     * @param filePath
     * @param sftp
     * @throws SftpException
     */
    public static void mkdirs(String filePath, final ChannelSftp sftp) throws SftpException { 
    	File file = new File(filePath);
    	mkdirs(file, sftp);
    }
    
    /**
     * 递归创建文件夹
     * @param file
     * @param sftp
     * @throws SftpException
     */
    private static void mkdirs(File file, final ChannelSftp sftp) throws SftpException { 
    	if(file != null) {
    		String path = file.getPath().replace("\\", "/");
    		try {
    			sftp.mkdir(path);
    		} catch(SftpException ex) {
    			mkdirs(file.getParentFile(), sftp);
    			sftp.mkdir(path);
    		}
    		
    	}
    }
    
    /**
     * 重命名文件
     * @param srcPath
     * @param destPath
     * @param sftp
     * @throws SftpException
     */
    public static void rename(String srcPath, String destPath, final ChannelSftp sftp) throws SftpException {  
    	sftp.rename(srcPath, destPath);
    }
    
    /**
	 * 下载文件
	 * @param targetPath
	 * @param localFilePath
	 * @param ftp
     * @throws SftpException 
     * @throws FileNotFoundException 
	 */
    public static void downloadFile(String targetPath, String localFilePath, final ChannelSftp ftp) throws SftpException, FileNotFoundException {  
    	OutputStream os = null;
		try {
			os = FileUtils.getOutputStream(localFilePath);
			ftp.get(targetPath, os); 
		} catch (FileNotFoundException ex) {
			throw ex;
		} catch (SftpException ex) {
			throw ex;
		} finally {
			IOUtils.close(os);
		}
       
    } 
    
    /**
	 * 上传文件
	 * @param loacalFile	本地文件
	 * @param basePath	目标路径
	 * @return
     * @throws IOException 
     * @throws SftpException 
	 */
	public static void uploadFile(File localFile, String targetPath, ChannelSftp ftp) throws IOException, SftpException {
		try {
			//上传文件
			OutputStream out = ftp.put(targetPath);  
			InputStream in = new FileInputStream(localFile);  
            IOUtils.write(in, out, 1024);	//附带关闭流
		}  finally {
		}
	}
	
	/**
	 * 上传文件(带进度监听)
	 * @param localFile 本地文件
	 * @param targetPath 目标路径
	 * @param ftp
	 * @param listener
	 * @throws IOException
	 * @throws SftpException
	 */
	public static void uploadFile(File localFile, String targetPath, ChannelSftp ftp, ProgressListener listener) throws IOException, SftpException {
		try {
			//上传文件
			OutputStream out = ftp.put(targetPath);  
			InputStream in = new FileInputStream(localFile);  
            IOUtils.write(in, out, 1024, listener);	//附带关闭流
		}  finally {
		}
	}
    
	/**
	 * 创建文件
	 * <p>
	 * 支持递归创建(也就是说上级不存在时会先创建上级文件夹)
	 * </p>
	 * @param filePath
	 * @throws SftpException
	 * @throws JSchException
	 */
	public void mkdirs(String filePath) throws SftpException, JSchException {
		ChannelSftp ftp = null;
		try {
			//设置通道
			ftp = getChannelFtp();
			ftp.connect();
			mkdirs(filePath, ftp);
		} finally {
			if(ftp != null) {
				ftp.disconnect();
			}
		}
	}
	
	/**
	 * 删除文件或目录
	 * @param filePath
	 * @return
	 * @throws SftpException 
	 * @throws JSchException 
	 */
	public void deleteFile(String filePath) throws SftpException, JSchException {
		ChannelSftp ftp = null;
		try {
			//设置通道
			ftp = getChannelFtp();
			ftp.connect();
			deleteFile(filePath, ftp);
		} finally {
			if(ftp != null) {
				ftp.disconnect();
			}
		}
	}
	
	/**
	 * 重命名文件
	 * @param srcPath
	 * @param destPath
	 * @throws SftpException
	 * @throws JSchException
	 */
	public void rename(String srcPath, String destPath) throws SftpException, JSchException {
		ChannelSftp ftp = null;
		try {
			//设置通道
			ftp = getChannelFtp();
			ftp.connect();
			rename(srcPath, destPath, ftp);
		} finally {
			if(ftp != null) {
				ftp.disconnect();
			}
		}
	}
	
	/**
	 * 对于ChannelExec,在调用connect()方法之前这个命令提供了setCommand()方法， 
		并且这些命令作为输入将以输入流的形式被发送出去。 
		（通常，你只能有调用setCommand()方法一次，多次调用只有最后一次生效）， 
		但是你可以使用普通shell的分隔符（&，&&，|，||，; , \n, 复合命令）来提供多个命令。 
		这就像在你本机上执行一个shell脚本一样（当然，如果一个命令本身就是个交互式shell，这样就像ChannelShell）
	 * @param command
	 * @param basePath
	 * @return
	 * @throws IOException 
	 * @throws JSchException 
	 */
	public List<String> readLinesExec(String command, String basePath) throws IOException, JSchException {
		
		ChannelExec channel = null;
		try {
			channel  = getChannelExec();
			// Create and connect channel.  
	        channel.setCommand(command);  
	        
	        if(!StringUtils.isBlank(basePath)) {
	        	command = StringUtils.concatFilePathBySeparator("/", basePath, "/")+command;
	        }
	        	
	        channel.setInputStream(null);  
	        InputStream in = channel  
	                .getInputStream();  
	        channel.connect(TIME_WAIT);
	        
	        
	        // Get the output of remote command.  
            List<String> lines = IOUtils.readLines(in, DEFAULT_CHARSET);
            
            return lines;
		}  finally {
			if(channel != null) {
				 // Disconnect the channel and session.  
	            channel.disconnect();
			}
		}
	}
	
	public boolean execShell(String command, String basePath) throws JSchException, IOException {
		ChannelShell channel = null;
		try {
			channel  = getChannelShell();
			// Create and connect channel.  
			if(!StringUtils.isBlank(basePath)) {
	        	command = StringUtils.concatFilePathBySeparator("/", basePath, "/")+command;
	        }
	        ChannelShell channelShell = getChannelShell();
//		    InputStream in = channelShell.getInputStream();
	        OutputStream outputStream = channelShell.getOutputStream();
	        channelShell.setOutputStream(null);

	        channelShell.connect( TIME_WAIT );  
	        //写命令
	        outputStream.write((command + "\n\n").getBytes(DEFAULT_CHARSET));
	        outputStream.flush();
	        
	        
	        Thread.sleep(TIME_WAIT);
            return true;
		} catch (InterruptedException e) {
		} finally {
			if(channel != null) {
				 // Disconnect the channel and session.  
	            channel.disconnect();
			}
		}
		return false;
	}
	
	/**
	 * 执行shell命令,读取控制台输出
	 * <p>
	 * 	请不要用该方法
	 * </p>
	 * @param command
	 * @param basePath
	 */
	@Deprecated
	public void consoleShell(String command) {
		ChannelShell channel = null;
		try {
			channel  = getChannelShell();
			// Create and connect channel.  
			
	        ChannelShell channelShell = getChannelShell();
//		    InputStream in = channelShell.getInputStream();
	        OutputStream outputStream = channelShell.getOutputStream();
	        channelShell.setOutputStream(System.out);

	        channelShell.connect( TIME_WAIT );  
	        //写命令
	        outputStream.write((command + "\n\n").getBytes(DEFAULT_CHARSET));
	        outputStream.flush();
	        
	        while(true) {
	        	Thread.sleep(TIME_WAIT);
	        }
	        
		} catch(JSchException ex) {
			Console.err(ex);
		} catch (IOException ex) {
			Console.err(ex);
		} catch (InterruptedException e) {
			
		} finally {
			if(channel != null) {
				 // Disconnect the channel and session.  
	            channel.disconnect();
			}
		}
	}
	
	/**
	 * 
	 * @param localFile
	 * @param basePath
	 * @param fileRename
	 * @return
	 * @throws JSchException 
	 * @throws SftpException 
	 * @throws IOException 
	 */
	public void uploadFile(File localFile, String targetPath) throws JSchException, IOException, SftpException {
		ChannelSftp ftp = null;
		try {
			//设置通道
			ftp = getChannelFtp();
			ftp.connect();
			
			uploadFile(localFile, targetPath, ftp);
		}  finally {
			if(ftp != null) {
				ftp.disconnect();
			}
		}
	}
	
	/**
	 * 上传文件(带进度监听)
	 * @param localFile
	 * @param targetPath
	 * @param listener
	 * @throws JSchException
	 * @throws IOException
	 * @throws SftpException
	 */
	public void uploadFile(File localFile, String targetPath, ProgressListener listener) throws JSchException, IOException, SftpException {
		ChannelSftp ftp = null;
		try {
			//设置通道
			ftp = getChannelFtp();
			ftp.connect();
			
			uploadFile(localFile, targetPath, ftp, listener);
		}  finally {
			if(ftp != null) {
				ftp.disconnect();
			}
		}
	}
	
	/**
	 * 下载文件
	 * @param targetPath 目标文件路径
	 * @param localFilePath	本地文件路径
	 * @return
	 * @throws JSchException 
	 * @throws SftpException 
	 * @throws FileNotFoundException 
	 */
	public void downLoadFile(String targetPath, String localFilePath) throws JSchException, FileNotFoundException, SftpException {
		ChannelSftp ftp = null;
		try {
			//设置通道
			ftp = getChannelFtp();
			ftp.connect();
			downloadFile(targetPath, localFilePath, ftp);
		}  finally {
			if(ftp != null) {
				ftp.disconnect();
			}
		}
	}
	
	/**
	 * 查询一个文件夹下的所有文件
	 * @param basePath
	 * @return
	 * @throws JSchException 
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public List<String> ls(String basePath) throws JSchException, FileNotFoundException {
		ChannelSftp channel = null;
		try {
			//设置通道
			channel = getChannelFtp();
			channel.connect();
		
			List<String> fileNameList = ListUtils.newArrayList();

			Vector<LsEntry> fileList = channel.ls(basePath); 
			for (LsEntry file : fileList) {
				String fileName = file.getFilename();
				if(".".equals(fileName) || "..".equals(fileName)){ 
		            continue; 
		        }
				if(file.getLongname().startsWith("d")) {	//第一个字母为d是文件夹,如drwxr-xr-x    9 root     root         4096 Jan 31 16:23 ROOT
					fileName += "/";
				}
				fileNameList.add(fileName); 
			}

			return fileNameList; 
		}  catch (SftpException ex) {
			throw new FileNotFoundException(ex.getMessage());
		} finally {
			if(channel != null) {
				channel.disconnect();
			}
		}
	}
	
	private ChannelExec getChannelExec() throws JSchException {
		return (ChannelExec) session.openChannel("exec");
	}
	
	public ChannelShell getChannelShell() throws JSchException {
		return (ChannelShell) session.openChannel("shell");
	}
	
	public ChannelSftp getChannelFtp() throws JSchException {
		return (ChannelSftp) session.openChannel("sftp");
	}
	
	public static void main(String[] args) throws Exception {
		SSHHelper ssh = SSHHelper.connect("192.168.162.100", 22, "root", "111111");
		try {
			ssh.uploadFile(new File("E:\\a.txt"), "/usr/local/");	
			ssh.deleteFile("/usr/local/a.txt");
			ssh.close();
		} finally{
			ssh.dispose();
		}
	}

	@Override
	public void close() throws IOException {
		dispose();
	}

	@Override
	public void dispose() {
		if(session != null) {
			session.disconnect();
			session = null;
		}
	}
}
