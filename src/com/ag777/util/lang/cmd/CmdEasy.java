package com.ag777.util.lang.cmd;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.SystemUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;

/**
 * 一些常用命令(Cmd/Shell)的执行工具类
 * <p>
 * 针对linux操作系统!
 * </p>
 * 
 * @author ag777
 * @version create on 2018年07月04日,last modify at 2018年07月05日
 */
public class CmdEasy {

	private CmdEasy() {}
	
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
	 * 获取默认网关
	 * <p>
	 * 思路:
	 * ①执行route -n 命令
	 * ②匹配Destination为0.0.0.0
	 * ③该行Gateway对应的值就是默认路由
	 * </p>
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String gateWayDefault() throws IOException {
		/*
		 * Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
			192.168.167.0   0.0.0.0         255.255.255.0   U     0      0        0 eth0
			0.0.0.0         192.168.167.1   0.0.0.0         UG    0      0        0 eth0
		 */
		List<String> lines = CmdUtils.getInstance().readLines("route -n", null);
		for (String line : lines) {
			if(line.startsWith("0.0.0.0")) {
				String[] groups = line.split("\\s+");
				return groups[1];
			}
		}
		return null;
	}
	
	/**
	 * 获取路由表
	 * @return
	 * @throws IOException
	 */
	public static List<Map<String, Object>> routhList() throws IOException {
		List<Map<String, Object>> routeList = ListUtils.newArrayList();
		/*
		 * Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
			192.168.167.0   0.0.0.0         255.255.255.0   U     0      0        0 eth0
			0.0.0.0         192.168.167.1   0.0.0.0         UG    0      0        0 eth0
		 */
		List<String> lines = CmdUtils.getInstance().readLines("route -n", null);
		
		String [] titles =  null;
		for (String line : lines) {
			String[] groups = line.split("\\s+");
			if(titles == null) {
				titles = groups;
			} else {
				Map<String, Object> routeMap = MapUtils.newHashMap();
				for (int i = 0; i < groups.length; i++) {
					routeMap.put(titles[i], groups[i]);
				}
				routeList.add(routeMap);
			}
		}
		return routeList;
	}
	
	/*=======通用区=============*/
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
