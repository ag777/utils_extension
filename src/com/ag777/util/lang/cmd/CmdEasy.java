package com.ag777.util.lang.cmd;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.ag777.util.lang.ObjectUtils;
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
 * @version create on 2018年07月04日,last modify at 2019年06月20日
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
	public static Optional<File> tar(String fileName, String regex, String baseDir) {
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
	public static boolean tarExtract(String fileName, String baseDir, String targetPath) {
		StringBuilder cmd = new StringBuilder();
		cmd.append("tar zxvf ").append(fileName);
		if(targetPath != null) {
			cmd.append(" -C ").append(targetPath);
		}
		return CmdUtils.getInstance().exec(
				cmd.toString(), baseDir);
	}
	
	/**
	 * 获取cpuId
	 * <p>
	 * 去除id为"00 00 00 00 00 00 00 00"的行
	 * </p>
	 * 
	 * @return
	 * @throws IOException 
	 */
	public static List<String> getCpuIds() throws IOException {
		String cmd = "dmidecode -t 4 | grep ID |sort -u |awk -F': ' '{print $2}'";
		//"dmidecode -t processor |grep ID|sort -u |awk -F': ' '{print $2}'"	//效果一样
		List<String> lines = ShellUtils.getInstance().readLines(cmd, null);
		for (int i = lines.size() - 1; i >= 0; i--) { // 倒序遍历，为了能删除数据
			String line = lines.get(i);
			if ("00 00 00 00 00 00 00 00".equals(line)) {
				lines.remove(line);
			}
		}
		return lines;
		
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
	
	/**
	 * 获取硬盘使用情况
	 * <p>
	 * 使用命令df -m实现
	 * 单位为GB
	 * 实际环境中总空间比(已用空间+可用空间)的和来的大，所以这里已用空间为总空间-可用空间
	 * </p>
	 * @return [硬盘总大小, 已使用大小, 可用大小]
	 * @throws IOException
	 */
	public static Double[] getDiskRateGroup() throws IOException {
		/*
		 * Filesystem     1M-blocks  Used Available Use% Mounted on
			/dev/sda3          38074  8499     27635  24% /
			tmpfs               1885     0      1885   0% /dev/shm
			/dev/sda1             93    39        49  45% /boot
		 */
		long total = 0;
//		long used = 0;
		long available = 0;
		List<String> lines = CmdUtils.getInstance().readLines("df -m", null);
		lines.remove(0);	//去除第一行
		for (String line : lines) {
			String[] groups = line.split("\\s+");
			total += ObjectUtils.toLong(groups[1], 0);
//			used += ObjectUtils.toLong(groups[2], 0);
			available += ObjectUtils.toLong(groups[3], 0);
		}
		double totald = convertDiskRate(total);
		double availabled = convertDiskRate(available);
		double used = totald-availabled;
		return new Double[]{
				totald, 
				used, 
				availabled};
	}
	
	
	/**
	 * 执行ps命令，查找关键词，杀死对应进程
	 * @param searchKey 关键词 执行{@code ps -ef|grep #{searchKey} }方法查询数据
	 * @param predicate 匹配对应的行，如果test方法返回true则杀死对应
	 * @throws IOException IO异常
	 */
    public static void kill9ByPsBySearch(String searchKey, Predicate<String> predicate) throws IOException {
        StringBuilder command = new StringBuilder("ps -ef");
        if(searchKey != null) {
            command.append("|grep ").append("'").append(searchKey).append("'");
        }
        List<String> lines = CmdUtils.getInstance().readLines(command.toString(), null);
        for (String line:
                lines) {
           if(predicate.test(line)) {
        	   String id = line.split("\\s+")[1];
        	   kill9(id);
           }
        }
    }
	
	/**
     * 杀死进程
     * @param id 进程编号
     * @throws IOException 异常信息
     */
    public static void kill9(String id) throws IOException {
        CmdUtils.getInstance().exec("kill -9 "+id, null);
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
	
	/*=======内部方法===========*/
	/**
	 * 根据硬盘大小(M)转化为GB
	 * <p>
	 * 将误差考虑在内	--by C++工程师
	 * </p>
	 * @param rate
	 * @return
	 */
	private static double convertDiskRate(long rate) {
		double hd_total = Math.ceil(rate*1.0/1024);
		if(hd_total>1800 && hd_total<2000) {
			return 2048;
		} else if(hd_total>900 && hd_total<1000) {
			return 1024;
		} else if(hd_total>400 && hd_total<500) {
			return 512;
		} else if(hd_total>200 && hd_total <250) {
			return 256;
		} else if(hd_total>100 && hd_total <120) {
			return 128;
		} else {
			return hd_total;
		}

	}
    
}
