package com.ag777.util.lang.system;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.List;
import java.util.Optional;

import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.cmd.CmdUtils;
import com.ag777.util.lang.collection.ListUtils;

/**
 * windows系统操作工具类
 *
 * @author ag777
 * @version last modify at 2023年09月15日
 */
public class WindowsUtils {
	private WindowsUtils() {}
	
	/**
	 * 打开目标文件夹
	 */
	public static void open(String filePath) {
    	try {
//			Runtime.getRuntime().exec(
//			        "rundll32 SHELL32.DLL,ShellExec_RunDLL " +
//			        "Explorer.exe /select," + filePath);
    		String[] cmd = new String[5];  
            cmd[0] = "cmd";  
            cmd[1] = "/c";  
            cmd[2] = "start";  
            cmd[3] = " ";  
            cmd[4] = filePath;  
            Runtime.getRuntime().exec(cmd);  
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	/**
	 * 打开默认浏览器
	 * @param url 访问的url
	 * @return 是否成功打开
	 */
	public static boolean browser(String url) {
		if (java.awt.Desktop.isDesktopSupported()) {
			try {
				// 创建一个URI实例
				java.net.URI uri = java.net.URI.create(url);
				// 获取当前系统桌面扩展
				java.awt.Desktop dp = java.awt.Desktop.getDesktop();
				// 判断系统桌面是否支持要执行的功能
				if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
					// 获取系统默认浏览器打开链接
					dp.browse(uri);
					return true;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static Optional<String> getInstallPath(String regPath) throws IOException {
		/*
		HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\chrome.exe
    	Path    REG_SZ    C:\Program Files\Google\Chrome\Application
		 */
		List<String> lines = CmdUtils.getInstance().readLines("reg query \""+regPath+"\" /v Path", null);
		if (ListUtils.isEmpty(lines)) {
			return Optional.empty();
		}
		for (String line : lines) {
			// 目的是去除行头的空格
			line = line.trim();
			// 找到以Path开头的行
			if (line.startsWith("Path")) {
				// Path, REG_SZ, C:\\Program Files\\Google\\Chrome\\Application
				String[] group = line.split("\\s+", 3);
				if (group.length < 3) {
					return Optional.empty();
				}
				return Optional.of(group[2]);
			}
		}
		// 没找到
		return Optional.empty();
	}
	
	public static boolean available(int port) {
        if (port <= 0) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        } else {
            ServerSocket ss = null;
            DatagramSocket ds = null;

            try {
                ss = new ServerSocket(port);
                ss.setReuseAddress(true);
                ds = new DatagramSocket(port);
                ds.setReuseAddress(true);
                boolean var3 = true;
                return var3;
            } catch (IOException ex) {
               ex.printStackTrace();
            } finally {
                IOUtils.close(ds, ss);
            }

            return false;
        }
    }

	public static void main(String[] args) throws IOException {
		// 获取chrome浏览器安装目录
		System.out.println(getInstallPath("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe"));
	}
}
