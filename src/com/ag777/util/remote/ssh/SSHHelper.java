package com.ag777.util.remote.ssh;

import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.interf.Disposable;
import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
 * @version last modify at 2019年04月18日
 */
public class SSHHelper implements Disposable, Closeable {

	private int timeoutConnect;	//连接超时时间
	private Charset charset;
	private Session session;
	
	public SSHHelper(Session session) {
		this(session, 10000);
	}
	
	public SSHHelper(Session session, int timeoutConnect) {
		this.session = session;
		this.timeoutConnect = timeoutConnect;
		charset = StandardCharsets.UTF_8;
	}

	public Charset getCharset() {
		return charset;
	}
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public int getTimeoutConnect() {
		return timeoutConnect;
	}
	public void setTimeoutConnect(int timeoutConnect) {
		this.timeoutConnect = timeoutConnect;
	}

	public Session getSession() {
		return session;
	}

	/**
	 * 默认连接超时时间为10秒
	 * @param ip ip
	 * @param port 端口号
	 * @param user 账号
	 * @param password 密码
	 * @return SSHHelper
	 * @throws JSchException JSchException
	 */
	public static SSHHelper connect(String ip, int port, String user, String password) throws JSchException {
		return connect(ip, port, user, password, 10000);
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
		return new SSHHelper(session, timeout);
	}

	/**
	 * 创建文件
	 * <p>
	 * 支持递归创建(也就是说上级不存在时会先创建上级文件夹)
	 * </p>
	 * @param filePath 文件夹路径
	 * @throws SftpException SftpException
	 * @throws JSchException JSchException
	 */
	public void mkdirs(String filePath) throws SftpException, JSchException {
		ChannelSftp ftp = null;
		try {
			//设置通道
			ftp = getChannelFtp();
			ftp.connect();
			mkdirs(filePath, ftp);
		} finally {
			disconnect(ftp);
		}
	}

	/**
	 * 删除文件或目录
	 * @param filePath 文件路径
	 * @throws SftpException SftpException
	 * @throws JSchException JSchException
	 */
	public void deleteFile(String filePath) throws SftpException, JSchException {
		ChannelSftp ftp = null;
		try {
			//设置通道
			ftp = getChannelFtp();
			ftp.connect();
			deleteFile(filePath, ftp);
		} finally {
			disconnect(ftp);
		}
	}

	/**
	 * 重命名文件
	 * @param srcPath 源文件路径
	 * @param destPath 目标文件路径
	 * @throws SftpException SftpException
	 * @throws JSchException JSchException
	 */
	public void rename(String srcPath, String destPath) throws SftpException, JSchException {
		ChannelSftp ftp = null;
		try {
			//设置通道
			ftp = getChannelFtp();
			ftp.connect();
			rename(srcPath, destPath, ftp);
		} finally {
			disconnect(ftp);
		}
	}

	/**
	 * 对于ChannelExec,在调用connect()方法之前这个命令提供了setCommand()方法，
	 并且这些命令作为输入将以输入流的形式被发送出去。
	 （通常，你只能有调用setCommand()方法一次，多次调用只有最后一次生效），
	 但是你可以使用普通shell的分隔符（&，&&，|，||，; , \n, 复合命令）来提供多个命令。
	 这就像在你本机上执行一个shell脚本一样（当然，如果一个命令本身就是个交互式shell，这样就像ChannelShell）
	 * @param command 命令
	 * @return 返回
	 * @throws IOException IOException
	 * @throws JSchException JSchException
	 */
	public List<String> readLinesExec(String command) throws IOException, JSchException {
		ChannelExec channel = null;
		try {
			channel  = getChannelExec();
			// Create and connect channel.
			channel.setCommand(command.getBytes(charset));

			channel.setInputStream(null);
			InputStream in = channel
					.getInputStream();
			channel.connect(timeoutConnect);


			// Get the output of remote command.
			return IOUtils.readLines(in, charset);
		}  finally {
			disconnect(channel);
		}
	}

	public void execShell(String command, Consumer<String> lineConsumer) throws JSchException, IOException {
		ChannelShell channel = null;
		try {
			channel  = getChannelShell();
			ChannelShell channelShell = getChannelShell();
//		    InputStream in = channelShell.getInputStream();
			OutputStream outputStream = channelShell.getOutputStream();
			channelShell.setOutputStream(null);

			channelShell.connect( timeoutConnect );
			//写命令
			outputStream.write((command + "\n\n").getBytes(charset));
			outputStream.flush();
			if (lineConsumer != null) {
				InputStream in = channel
						.getInputStream();
				IOUtils.readLines(in, lineConsumer, charset);
			}
		} finally {
			disconnect(channel);
		}
	}

	/**
	 * 执行shell命令,读取控制台输出
	 * <p>
	 * 	请不要用该方法
	 * </p>
	 * @param command 命令
	 */
	@Deprecated
	public void consoleShell(String command) throws JSchException, IOException, InterruptedException {
		ChannelShell channel = null;
		try {
			channel  = getChannelShell();
			// Create and connect channel.

			ChannelShell channelShell = getChannelShell();
//		    InputStream in = channelShell.getInputStream();
			OutputStream outputStream = channelShell.getOutputStream();
			channelShell.setOutputStream(System.out);

			channelShell.connect( timeoutConnect );
			//写命令
			outputStream.write((command + "\n\n").getBytes(charset));
			outputStream.flush();

			while(true) {
				TimeUnit.SECONDS.sleep(1);
			}

		} finally {
			disconnect(channel);
		}
	}

	/**
	 *
	 * @param localFile 本地文件路径
	 * @param targetPath 目标路径
	 * @throws JSchException JSchException
	 * @throws SftpException SftpException
	 * @throws IOException io异常
	 */
	public void uploadFile(File localFile, String targetPath) throws JSchException, IOException, SftpException {
		ChannelSftp ftp = null;
		try {
			//设置通道
			ftp = getChannelFtp();
			ftp.connect();

			uploadFile(localFile, targetPath, ftp);
		}  finally {
			disconnect(ftp);
		}
	}

	/**
	 * 下载文件
	 * @param targetPath 目标文件路径
	 * @param localFilePath    本地文件路径
	 * @return 下载到本地的文件
	 * @throws JSchException JSchException
	 * @throws SftpException SftpException
	 * @throws FileNotFoundException 未找到用于上传的文件
	 */
	public File downLoadFile(String targetPath, String localFilePath) throws JSchException, FileNotFoundException, SftpException {
		ChannelSftp ftp = null;
		try {
			//设置通道
			ftp = getChannelFtp();
			ftp.connect();
			downloadFile(targetPath, localFilePath, ftp);
			return new File(localFilePath);
		}  finally {
			disconnect(ftp);
		}
	}

	/**
	 * 查询一个文件夹下的所有文件
	 * @param basePath 目标路径
	 * @return 文件名列表, 目录名称后面会跟上/
	 * @throws JSchException JSchException
	 * @throws FileNotFoundException 文件夹不存在
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
			disconnect(channel);
		}
	}

	/**
	 * 递归删除执行.
	 * @param filePath 文件路径
	 * @param sftp sftp连接
	 * @throws SftpException SftpException
	 */
	private static void deleteFile(final String filePath, final ChannelSftp sftp) throws SftpException {
		@SuppressWarnings("unchecked")
		Vector<LsEntry> vector = sftp.ls(filePath);
		if (vector.size() == 1) { // 文件，直接删除
			sftp.rm(filePath);
		} else if (vector.size() == 2) { // 空文件夹，直接删除
			sftp.rmdir(filePath);
		} else {
			String fileName;
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
	 * @param filePath 文件路径
	 * @param sftp sftp
	 * @throws SftpException SftpException
	 */
	public static void mkdirs(String filePath, final ChannelSftp sftp) throws SftpException {
		File file = new File(filePath);
		mkdirs(file, sftp);
	}

	/**
	 * 递归创建文件夹
	 * @param file 文件
	 * @param sftp sftp
	 * @throws SftpException SftpException
	 */
	public static void mkdirs(File file, final ChannelSftp sftp) throws SftpException {
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
	 * @param srcPath 源路径
	 * @param destPath 目标路径
	 * @param sftp sftp
	 * @throws SftpException SftpException
	 */
	public static void rename(String srcPath, String destPath, final ChannelSftp sftp) throws SftpException {
		sftp.rename(srcPath, destPath);
	}

	/**
	 * 上传文件
	 * @param localFile	本地文件
	 * @param targetPath	目标路径
	 * @param ftp ftp
	 * @throws IOException  io异常
	 * @throws SftpException 远程操作异常
	 */
	public static void uploadFile(File localFile, String targetPath, ChannelSftp ftp) throws IOException, SftpException {
		//上传文件
		OutputStream out = ftp.put(targetPath);
		InputStream in = new FileInputStream(localFile);
		IOUtils.write(in, out, 1024);	//附带关闭流
	}

	/**
	 * 下载文件
	 * @param targetPath 目标路径
	 * @param localFilePath 本地文件路径
	 * @param ftp ftp
	 * @throws SftpException SftpException
	 * @throws FileNotFoundException FileNotFoundException
	 */
	public static void downloadFile(String targetPath, String localFilePath, final ChannelSftp ftp) throws SftpException, FileNotFoundException {
		OutputStream os = FileUtils.getOutputStream(localFilePath);
		ftp.get(targetPath, os);
	}

	private ChannelExec getChannelExec() throws JSchException {
		return (ChannelExec) session.openChannel("exec");
	}

	public ChannelShell getChannelShell() throws JSchException {
		return (ChannelShell) session.openChannel("shell");
	}

	public ChannelSftp getChannelFtp() throws JSchException {
		ChannelSftp ftp = (ChannelSftp) session.openChannel("sftp");
		// 抛异常 The encoding can not be changed for this sftp server.
//            ftp.setFilenameEncoding(charset.toString());
		try {
			//解决无法设置服务端文件编码为gbk
			// https://blog.csdn.net/liqiang458473/article/details/80834092
			Class<ChannelSftp> cl = ChannelSftp.class;
			Field f1 =cl.getDeclaredField("server_version");
			f1.setAccessible(true);
			f1.set(ftp, 2);
			ftp.setFilenameEncoding(charset.toString());
		} catch (NoSuchFieldException | IllegalAccessException | SftpException e) {
			e.printStackTrace();
		}
		return ftp;
	}

	private void disconnect(Channel channel) {
		if (channel != null) {
			channel.disconnect();
		}
	}

	@Override
	public void dispose() {
		if(session != null) {
			session.disconnect();
			session = null;
		}
	}

	@Override
	public void close() {
		dispose();
	}
}
