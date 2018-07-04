package com.ag777.util.lang.cmd;

import java.io.File;
import java.util.Optional;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.SystemUtils;

/**
 * 一些常用命令(Cmd/Shell)的执行工具类
 * 
 * @author ag777
 * @version create on 2018年07月04日,last modify at 2018年07月04日
 */
public class CmdEasy {

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
	 * @param baseDir	执行命令的路径名,如果传空，则参数regex要带上完整的路径
	 * @return 压缩失败或文件不存在返回<code>Optional.empty()</code>
	 */
	public Optional<File> tar(String fileName, String regex, String baseDir) {
		if(!fileName.endsWith(".tar.gz")) {
			fileName += ".tar.gz";
		}
		if(!baseDir.endsWith("/") || !baseDir.endsWith("\\")) {
			baseDir += SystemUtils.fileSeparator();
		}
		if(CmdUtils.getInstance().exec(StringUtils.concat("tar zcf ",  fileName, ' ', regex), baseDir)) {
			File f = new File(baseDir + fileName);
			if(f.exists() && f.isFile()) {
				return Optional.of(f);
			}
		}
		return Optional.empty();
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
	 * @param baseDir	 命令执行目录,这个为空的哈fileName参数是要带路径的
	 * @param targetPath	解压的目标路径,如果为null则表示解压到baseDir路径
     */
	public boolean tarExtract(String fileName, String baseDir, String targetPath) {
		StringBuilder cmd = new StringBuilder();
		cmd.append("tar zxvf ").append(fileName);
		if(targetPath != null) {
			cmd.append(" -C ").append(targetPath);
		}
		return CmdUtils.getInstance().exec(
				cmd.toString(), baseDir);
	}
	
	/**
	 * 将filePath底下的所有文件打成war包
	 * @param filePath
	 * @param warPath
	 * @return
	 */
	public static boolean war(String filePath, String warPath) {
		return CmdUtils.getInstance().exec(
   			StringUtils.concat("jar -cvf ", warPath, " *"), filePath);
	}
}
