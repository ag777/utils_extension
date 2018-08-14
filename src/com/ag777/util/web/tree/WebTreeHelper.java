package com.ag777.util.web.tree;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.ag777.util.lang.ObjectUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.web.tree.model.ZTree;

/**
 * web目录树辅助构建类,用于目录树导出
 * 
 * @author ag777
 * @version create on 2017年07月26日,last modify at 2018年08月09日
 */
public class WebTreeHelper {
	private ZtreeBuilder treeBuilder;
	private Pattern p1;
	
	/**
	 * 获取树列表
	 * @return
	 */
	public List<ZTree> getTreeList() {
		List<ZTree> treeList = treeBuilder.getTreeList();
		/*排序*/
		treeList.sort((item1, item2)->{
			boolean isParent1 = ObjectUtils.isBooleanTrue(item1.getIsParent());
			boolean isParent2 = ObjectUtils.isBooleanTrue(item2.getIsParent());
			if(isParent1 && !isParent2) {	//前一个是目录,后一个是叶节点，不变
				return 0;
			} else if(!isParent1 && isParent2) {	//前一个是叶节点,后一个是目录,后一个上移
				return -1;
			}
			return item1.getName().compareTo(item2.getName());	//同级直接暗字符串升序排列
		});
		return treeList;
	}
	
	public WebTreeHelper() {
		p1 = Pattern.compile("^/+");
		treeBuilder = new ZtreeBuilder();
	}
	
	/**
	 * 解析并添加项到目录树
	 * <p>
	 * 注意:所有url都会被转化为小写
	 * protocol://host:port/path?query#ref
	 * http://tester:123456@www.baidu.com?a=b&b=c&c=d#abc
	 * </p>
	 * 
	 * @param urlStr
	 * @return
	 */
	public void add(String urlStr, Map<String, Object> extraData) {
		try {
			if(urlStr == null) {
				return;
			}
			urlStr = urlStr.toLowerCase();
			
			if(!urlStr.startsWith("http")) {
				urlStr = "http://"+urlStr;
			}
			URL url = new URL(urlStr);
			String protocol = url.getProtocol();
			String domain = url.getHost();
			int port = url.getPort();
			String path = p1.matcher(url.getPath()).replaceFirst("");	//去除path开头的/
			String baseUrl =StringUtils.concat(
					protocol, "://", domain,  port!=-1?(':'+port):"");
			
			String[] group = null;
			
			if(!path.isEmpty()) {
				String[] temp = path.split("(/+)");
				group = new String[temp.length+1];
				
				//将temp数据塞到group下标1的后面
				System.arraycopy(temp, 0, group, 1, temp.length);
				
			} else {
				group = new String[1];
			}
			//第一项为根url
			group[0] = baseUrl;
			
			treeBuilder.add(group, extraData);
			
		} catch (MalformedURLException ex) {
			System.err.println(urlStr+"解析失败:"+ex.getMessage());
		}
		
	}

	
	
	
	
}
