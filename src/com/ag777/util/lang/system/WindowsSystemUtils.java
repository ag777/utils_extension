package com.ag777.util.lang.system;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * windows系统相关工具类
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/10/14 上午9:58
 */
public class WindowsSystemUtils {

    /**
     * 用默认浏览器打开指定uurl
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

            } catch (Exception ignored) {
            }
        }
        return false;
    }

    /**
     * 尝试使用系统默认程序打开指定文件
     *
     * @param file 需要打开的文件对象
     * @return 如果文件成功打开，则返回true；否则返回false
     */
    public static boolean open(File file) {
        // 检查文件是否存在且是一个文件，如果满足条件，则不尝试打开，并返回false
        if (file.exists() && file.isFile()) {
            return false;
        }
        // 检查当前系统是否支持Java桌面扩展
        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                // 获取当前系统桌面扩展
                java.awt.Desktop dp = java.awt.Desktop.getDesktop();
                // 判断系统桌面是否支持要执行的功能
                if (dp.isSupported(Desktop.Action.OPEN)) {
                    // 使用系统默认程序（如浏览器）打开文件
                    dp.open(file);
                    return true;
                }
            } catch (Exception ignored) {
                // 异常情况，忽略并处理下一个逻辑
            }
        }
        // 如果上述条件都不满足，则返回false，表示文件无法被打开
        return false;
    }

    /**
     * 打开文件所在目录并在资源管理器中定位到该文件
     *
     * @param file 需要定位的文件对象
     * @return 执行操作的进程对象
     * @throws IOException 如果无法创建进程或文件不存在，则抛出IOException
     */
    public static Process openDirAndLocateFile(File file) throws IOException {
        // 检查文件是否存在且为普通文件
        if (file.exists() && file.isFile()) {
            try {
                // 使用列表来构建命令行参数
                ProcessBuilder processBuilder = new ProcessBuilder(
                        Arrays.asList("explorer.exe", "/select,\"" + file.getAbsolutePath() + "\"")
                );

                // 启动进程
                return processBuilder.start();
            } catch (IOException e) {
                // 如果发生IOException，重新抛出以便外部处理
                throw e;
            }
        } else {
            // 如果文件不是普通文件或不存在，则抛出FileNotFoundException
            throw new FileNotFoundException("文件不存在: " + file.getAbsolutePath());
        }
    }

}
