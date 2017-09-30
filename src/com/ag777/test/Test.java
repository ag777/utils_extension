package com.ag777.test;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.ag777.util.UtilsExtension;
import com.ag777.util.file.FileUtils;
import com.ag777.util.file.PathUtils;
import com.ag777.util.file.excel.ExcelReadUtils;
import com.ag777.util.gson.GsonUtils;
import com.ag777.util.http.HttpUtils;
import com.ag777.util.jsoup.JsoupUtils;
import com.ag777.util.jsoup.model.UserAgent;
import com.ag777.util.lang.Console;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;
import com.ag777.util.lang.thread.ThreadHelper;

public class Test {
	public static void main(String[] args) throws Exception {
//		ExcelReadHelper rh = ExcelReadHelper.Load("E://单位站点导入示例.xls");
//		Console.log(
//				rh.read(new String[]{"啊哈哈"}, false)
//		);
//		System.out.println(ExcelReadHelper.isExcel2007("E://a.xlsx"));
		
//		ExcelWriteHelper wh = new ExcelWriteHelper("aaa");
//		wh.createRow("第一行", null);
//		wh.write(new FileOutputStream(new File("E://a.xls")));
		
//		java.net.URL url = new URL("https://baidu.com/a");
//		System.out.println(url.getDefaultPort());
//		InetAddress[] addresses = InetAddress.getAllByName(url.getHost());
//		for (InetAddress address : addresses) {
//			System.out.println(
//					address.getHostAddress());
//		}
//		String[] ipStr = "123.125.114.144".split("\\.");
//		byte[] ipBytes=new byte[4];             //声明存储转换后IP地址的字节数组  
//        for (int i = 0; i < 4; i++) {  
//            int m=Integer.parseInt(ipStr[i]);   //转换为整数  
//            byte b=(byte)(m&0xff);              //转换为字节  
//            ipBytes[i]=b;  
//        }  
//        InetAddress baidu = InetAddress.getByAddress(addresses[0].getAddress());
//        System.out.println(
//        		baidu.getCanonicalHostName());
		
//		System.out.println(InetAddress.getByName("123.125.114.144").getHostName());
//		Console.log(
//			JsoupUtils.connect("http://123.125.114.144/").getHtml());
//		System.out.println(HttpUtils.doGet("http://123.125.114.144/"));
//		 Document d = Jsoup.connect("http://123.125.114.144/").get();
//		 System.out.println(d.html());
		
//		String json = FileUtils.readText("E://ling.json", null);
//		Map<String, Object> map = GsonUtils.get().toMap(json);
//		String url = "http://www.lingyu.me/bizhi/huawushaonvdiannaobizhi18p/1";//"http://www.lingyu.me/page/1"
//		JsoupUtils u = JsoupUtils.connect(url);
//		System.out.println(u.getHtml());
//		Console.log(
//				u.findByJsonMap(map));
		
//		try {
//			Pattern p = Pattern.compile("^^.**");
//			System.out.println(p == null);
//		} catch(Exception ex) {
//			System.out.println(ex.getClass());
//		}
//		String content = "{\"item\":{\"selector\":\"#thread_list > .j_thread_list\"},\"title\":{\"fun\":\"html\",\"regex\":\"^(.{0,3})\",\"replacement\":\"$1\",\"selector\":\".threadlist_title a\"}}";
//		Map<String, Object> jsonMap = new Hashtable<>(GsonUtils.get().toMap(content));
//		
//		ThreadHelper<List<Map<String, Object>>> th = new ThreadHelper<>();
//		for(int i=0;i<2;i++) {
//			th.addTask(new Callable<List<Map<String, Object>>>() {
//				@Override
//				public List<Map<String, Object>> call() throws Exception {
//					try{
//						String url = "https://tieba.baidu.com/f?kw=%C9%D9%C5%AE%C7%B0%CF%DF&fr=ala0&tpl=5";
//						JsoupUtils u = JsoupUtils.connect(url, UserAgent.IE9);
//						Map<String, Object> result = u.findByJsonMap(jsonMap);
//						List<Map<String, Object>> list = MapUtils.get(result, "data");
//						return list;
//					} catch(Exception ex) {
//						ex.printStackTrace();
//					}
//					return null;
//				}
//			});
//		}
//	
//		Console.setFormatMode(true);
//		Console.log(th.getResult());
		
//		String filePath = "e:\\test.xls";
//		Map<String, Object> map = GsonUtils.get().toMap("{\"${a}\":1,\"${b}\":222}");
//		ExcelTemplateHelper.load(filePath).replaceAll(map).write();
//		Console.log(
//				ExcelReadUtils.read("e:\\test.xls", new String[]{"a","b"}, true));
		File f = new File("e:\\test1.xls");
		System.out.println(f.exists());
//		Console.log(ExcelReadUtils.read("e:\\test1.xls", true));
	}
}
