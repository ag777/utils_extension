package com.ag777.util.web.servlet;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;

/**
 * servlet文件下载,防止乱码
 * <p>
 *	需要jar包(从tomcat/lib下拉取对应jar包):
 * <ul>
 * <li>servlet-api.jar</li>
 * </ul>
 *	</p>
 * 
 * @author ag777
 * @version create on 2018年11月16日,last modify at 2018年11月16日
 *
 */
public class FileDownUtils {

	/**
	 * 文件下载
	 * @param request
	 * @param response
	 * @param fileName
	 * @param filePath
	 * @throws Exception
	 */
	public static void fileDown(HttpServletRequest request,HttpServletResponse response,String fileName,String filePath) throws Exception {
		FileInputStream in = null;
		OutputStream out = null;
		try {
			// 设置响应头，控制浏览器下载该文件
			response.setHeader("content-disposition", "attachment;filename=\""
					+ encodeFilename(fileName,request)+"\"");
			
			// 读取要下载的文件，保存到文件输入流
			in = new FileInputStream(filePath);
			// 创建输出流
			out = response.getOutputStream();
			// 创建缓冲区
			byte buffer[] = new byte[1024];
			int len = 0;
			// 循环将输入流中的内容读取到缓冲区当中
			while ((len = in.read(buffer)) > 0) {
				// 输出缓冲区的内容到浏览器，实现文件下载
				out.write(buffer, 0, len);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			// 关闭文件输入流
			if(in != null) {
				in.close();
				in = null;
			}
			
			// 关闭输出流
			if(out != null) {
				out.close();
				out = null;
			}
			
		}
		
	}
	
	/**
	 * 判断是否是ie浏览器
	 * <p>
	 * 原帖地址:https://blog.csdn.net/fengchao2016/article/details/55188805
	 * </p>
	 * @param request
	 * @return
	 */
	private static boolean isMSBrowser(HttpServletRequest request) {
		String[] IEBrowserSignals = {"MSIE", "Trident", "Edge"};
		String userAgent = request.getHeader("User-Agent");
		for (String signal : IEBrowserSignals) {
			if (userAgent.contains(signal))
				return true;
		}
		return false;
	}

	
	public static String encodeFilename(String filename,
				HttpServletRequest request) {
			
		/** 
		 * 获取客户端浏览器和操作系统信息 
		 * 在IE浏览器中得到的是：User-Agent=Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Maxthon; Alexa Toolbar) 
		 * 在Firefox中得到的是：User-Agent=Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.7.10) Gecko/20050717 Firefox/1.0.6 
		 */
		String agent = request.getHeader("USER-AGENT");
		try {
			if (agent != null) {
				if(isMSBrowser(request)) {
					String newFileName = URLEncoder.encode(filename, "UTF-8");
//						newFileName = org.apache.commons.lang.StringUtils.replace(newFileName, "+", "%20");
//						if (newFileName.length() > 150) {
//							newFileName = new String(filename.getBytes("UTF-8"),
//									"ISO8859-1");
//							newFileName = org.apache.commons.lang.StringUtils.replace(newFileName, " ", "%20");
//							return newFileName;
//						}
					return newFileName;
				} else if(agent.contains("Firefox")){
					String newFileName  = "=?UTF-8?B?" + (new String(Base64.getEncoder().encode(filename.getBytes("UTF-8")))) + "?=";
					return newFileName;
				} else if(-1 != agent.indexOf("Mozilla")) {
					return MimeUtility.encodeText(filename, "UTF-8", "B");
				}
				
				
			}

			return filename;
		} catch (Exception ex) {
			return filename;
		}
	}
}
