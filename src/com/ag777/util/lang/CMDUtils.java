package com.ag777.util.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import com.ag777.util.lang.model.Charsets;


/**
 * cmd命令执行辅助类(针对linux)
 * <p>
 * 		执行一个cmd命令会产生三个流（input/output/err），其中一个不处理就有可能产生程序挂起问题，永远不可能得到返回了
 * </p>
 * @author ag777
 * @version last modify at 2018年04月04日
 */
public class CMDUtils {
	
	private static Charset DEFAULT_CHARSET = Charsets.UTF_8;//"gb2312";
	
	public static Charset getReadCharset() {
		return DEFAULT_CHARSET;
	}
	public static void setReadCharset(Charset charset) {
		DEFAULT_CHARSET = charset;
	}
	
	private CMDUtils() {}
	
	/**
	 * 压缩文件
     * <p>
     * 例如：
     * </p>
     * 
     * <pre>
     * 		CMDUtils.tar("a", "/dir", "/usr/temp/");
     * 		意思就是将/usr/temp/下的/dir目录压缩为a.tar.gz置于/usr/temp/下
     * </pre>
     * </p>
     * </p>
     * 
	 * @param fileName 压缩后的文件名(不带后缀,自动添加.tar.gz)
	 * @param regex	要压缩的路径或者文件名
	 * @param basePath	执行命令的路径名,如果传空，则参数regex要带上完整的路径
	 * @return 压缩失败或文件不存在返回<code>Optional.empty()</code>
	 */
	public static Optional<File> tar(String fileName, String regex, String basePath) {
		if(!fileName.endsWith(".tar.gz")) {
			fileName += ".tar.gz";
		}
		if(!basePath.endsWith("/") || !basePath.endsWith("\\")) {
			basePath += SystemUtils.fileSeparator();
		}
		if(doCmd(StringUtils.concat("tar zcf ",  fileName, ' ', regex), basePath)) {
			File f = new File(basePath + fileName);
			if(f.exists() && f.isFile()) {
				return Optional.of(f);
			}
		}
		return Optional.empty();
	}

	/**
	 * 解压文件
	 * 
	 * @param fileName
	 * @param basePath
	 * @return
	 */
	public static boolean tarExtract(String fileName, String basePath) {
		return doCmd("tar zxvf " + fileName, basePath);
	}

	/**
     * 解压文件
     * <p>
     * 例如：
     * </p>
     * 
     * <pre>
     * 		CMDUtils.tarExtract("a.tar.gz", "/usr/temp/dir/", "/usr/temp/");
     * 		意思就是将/usr/temp/dir/下的a.tar.gz压缩包解压到/usr/temp/目录下
     * </pre>
     * </p>
     * </p>
     * @param fileName 压缩包的文件名/路径(要带后缀)
	 * @param basePath	 命令执行目录,这个为空的哈fileName参数是要带路径的
	 * @param targetPath	解压的目标路径
     */
	public static boolean tarExtract(String fileName, String basePath, String targetPath) {
		return doCmd(
				StringUtils.concat("tar zxvf ", fileName, " -C ", targetPath), basePath);
	}

	/**
	 * 将filePath底下的所有文件打成war包
	 * @param filePath
	 * @param warPath
	 * @return
	 */
	public static boolean war(String filePath, String warPath) {
		return CMDUtils.doCmd(
    			StringUtils.concat("jar -cvf ", warPath, " *"), filePath);
	}
	
	/**
	 * 将warPath对应的war包解压到targetPath下
	 * @param warPath 需要解压的war包路径
	 * @param targetPath	解压到的目标路径
	 * @param bathPath 执行命令的基础路径;若传该值前面两个参数都为相对路径，否则为绝对路径
	 * @return
	 */
	public static boolean unWar(String warPath, String targetPath, String bathPath) {
    	return doCmd(StringUtils.concat("unzip ", warPath, " -d ", targetPath), bathPath);
    }
	
	/**
     * 复制文件
     * <p>
     * 例如：
     * </p>
     * 
     * <pre>
     * 		CMDUtils.copy("/usr/temp/a.txt", "/usr/temp2/b.txt");
     * 		意思就是将/usr/temp/a.txt文件复制到/usr/temp2/目录下并重名为b.txt
     * </pre>
     * </p>
     * </p>
     * @param sourcePath 复制前的路径
	 * @param targetPath	 复制后的路径
     */
	public static boolean copy(String sourcePath, String targetPath) {
		return doCmd(
				StringUtils.concat("cp -r ", sourcePath, ' ', targetPath), targetPath);
	}

	
	/**
     * 执行cmd命令(防进程挂起),只关心成功与否,不关心返回
     * 
     * @param cmd 命令内容
	 * @param basePath	 执行命令的路径
     */
	public static boolean doCmd(String cmd, String basePath) {
		try {
			Process shellPro = null;
			if(basePath == null) {
				shellPro = Runtime.getRuntime().exec(cmd);
			} else {
				shellPro = Runtime.getRuntime().exec(cmd, null, new File(basePath));// 执行删除默认路由命令
			}
			try {
				InputStream fis = shellPro.getInputStream();
				final BufferedReader brError = new BufferedReader(
						new InputStreamReader(shellPro.getErrorStream(), "gb2312"));
				InputStreamReader isr = new InputStreamReader(fis, "gb2312");
				final BufferedReader br = new BufferedReader(isr);
				Thread t1 = new Thread() {
					public void run() {
						@SuppressWarnings("unused")
						String line = null;
						try {
							while ((line = brError.readLine()) != null) {
							}
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								if (brError != null)
									brError.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				};
				Thread t2 = new Thread() {
					public void run() {
						@SuppressWarnings("unused")
						String line = null;
						try {
							while ((line = br.readLine()) != null) {
							}
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								if (br != null)
									br.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				};
				t1.start();
				t2.start();

				int exitValue = shellPro.waitFor();
				if (0 == exitValue) {
					return true;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if (null != shellPro) {
					shellPro = null;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 执行cmd命令获取返回的所有行
	 * 
	 * @param cmd
	 * @param basePath
	 * @return
	 * @throws IOException
	 */
	public static List<String> readLines(String cmd, String basePath) throws IOException {
		InputStream in = doCmdForStream(cmd, basePath);
		return IOUtils.readLines(in, DEFAULT_CHARSET);
	}
	
	/**
	 * 执行shell命令获取所有返回行
	 * @param cmd
	 * @param isShell
	 * @return
	 * @throws IOException
	 */
	public static List<String> readShellLines(String cmd) throws IOException {
		InputStream in = doShellForStream(cmd);
		return IOUtils.readLines(in, DEFAULT_CHARSET);
	}
	
	/**
	 * 执行cmd获取结果
	 * 
	 * @param cmd				cmd命令
	 * @param basePath			执行命令的路径
	 * @param lineSparator	每一行的末尾插入的字符，传null则不含换行符
	 * @return
	 * @throws IOException
	 */
	public static String readText(String cmd, String basePath, String lineSparator) throws IOException {
		InputStream in = doCmdForStream(cmd, basePath);
		return IOUtils.readText(in, lineSparator, DEFAULT_CHARSET);
	}
	
	/**
	 * 执行shell获取结果
	 * 
	 * @param cmd
	 * @param lineSparator
	 * @return
	 * @throws IOException 
	 * @throws IOException
	 */
	public static String readShellText(String cmd, String lineSparator) throws IOException  {
		InputStream in = doShellForStream(cmd);
		return IOUtils.readText(in, lineSparator, DEFAULT_CHARSET);
	}
	
	/**
	 * 执行cmd命令，从返回结果中配合正则表达式拉取结果字符串
	 * <p>
	 * 	正则部分用的方法为RegexUtils.find(String, String)
	 * </p>
	 * 
	 * @param cmd
	 * @param basePath
	 * @param regex
	 * @param replacement 如$1-$2-$3
	 * @return
	 * @throws IOException
	 */
	public static String find(String cmd, String basePath, String regex, String replacement) throws IOException {
		InputStream in = doCmdForStream(cmd, basePath);
		return IOUtils.find(in, regex, replacement, DEFAULT_CHARSET);
	}
	
	/**
	 * 执行cmd命令，从返回结果中配合正则表达式拉取结果字符串,并转为Long
	 * <p>
	 * 	正则部分用的方法为RegexUtils.findLong(String, String)
	 * </p>
	 * 
	 * @param cmd
	 * @param basePath
	 * @param regex
	 * @param replacement
	 * @return
	 * @throws IOException
	 */
	public static Long findLong(String cmd, String basePath, String regex, String replacement) throws IOException {
		InputStream in = doCmdForStream(cmd, basePath);
		return IOUtils.findLong(in, regex, replacement, DEFAULT_CHARSET);
	}
	
	/**
	 * 执行cmd命令，从返回结果中配合正则表达式拉取所有匹配的结果字符串列表
	 * <p>
	 * 	正则部分用的方法为RegexUtils.get(String, String)
	 * </p>
	 * 
	 * @param cmd
	 * @param basePath
	 * @param regex
	 * @param replacement 如$1-$2-$3
	 * @return
	 * @throws IOException
	 */
	public static List<String> findAll(String cmd, String basePath, String regex, String replacement) throws IOException {
		InputStream in = doCmdForStream(cmd, basePath);
		return IOUtils.findAll(in, regex, replacement, DEFAULT_CHARSET);
	}
	
	/**
	 * 读取cmd返回时先关输出流，开子线程读错误流
	 * <p>
	 *	 关于OutputStream,程序用不到,直接在开始的时候就应该close掉.对于errStream应该分别用一个线程来读取出IO流中的内容. 
			注意:必须使用线程,否则依然会阻塞.
		</p>
		
	 * 参考文章:http://xiaohuafyle.iteye.com/blog/1562786
	 * @param pro
	 */
	private static void pre(Process pro) {
		closeOutput(pro);
		readErr(pro);
	}

	
	/**
	 * 关闭输出流
	 * 
	 * @param pro
	 */
	private static void closeOutput(Process pro) {
		try {
			pro.getOutputStream().close();
		} catch (IOException e) {
		}  
	}
	
	/**
	 * 执行cmd命令得到返回流
	 * 
	 * @param cmd
	 * @param basePath
	 * @return
	 * @throws IOException
	 */
	private static InputStream doCmdForStream(String cmd, String basePath) throws IOException {
		Process pro = null;
		if(basePath == null) {
			pro = Runtime.getRuntime().exec(cmd);
		} else {
			pro = Runtime.getRuntime().exec(cmd, null, new File(basePath));// 执行删除默认路由命令
		}
		pre(pro);
		return pro.getInputStream();
	}
	
	/**
	 * 通过shell执行cmd命令得到返回流
	 * 
	 * @param cmd
	 * @return
	 * @throws IOException 
	 */
	private static InputStream doShellForStream(String cmd) throws IOException {
		Process pro = null;
		String[] cmdA = { "/bin/sh", "-c", cmd };
		pro = Runtime.getRuntime().exec(cmdA);
		pre(pro);
		return pro.getInputStream();
	}
	
	/**
	 * 子线程读取错误流
	 * 
	 * @param pro 进程
	 */
	private static void readErr(Process pro) {
		try {
			Thread t = new Thread() {
				public void run() {
					try {
						IOUtils.readLines(pro.getErrorStream(), "gb2312");
					} catch (IOException e) {
					}
				}
			};
			t.start();
		} catch(Exception ex) {
		}
	}
}
