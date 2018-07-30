package com.ag777.util.other.webtree;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;
import com.ag777.util.other.webtree.model.ZTreeItem;


/**
 * 网站目录树辅助构建类,可以配合jquery的zTree生成html的目录树
 * 
 * @author ag777
 * @version create on 2017年07月26日,last modify at 2018年07月27日
 */
public class WebTreeHelper {
	private Map<String, Object> treeMap;
	private int id=0;	//保证id自增
	
	private Pattern p1 = Pattern.compile("^/+");
	
	public Map<String, Object> get() {
		return treeMap;
	}
	
	public List<ZTreeItem> getItemTree() {
		List<ZTreeItem> itemList = ListUtils.newArrayList();
		id = 0;
		convert2TreeItem(id, treeMap, itemList);
		/*按字母顺序排序*/
		itemList.sort((item1, item2)->{
			return item1.getName().compareTo(item2.getName());
		});
		return itemList;
	}
	
	public WebTreeHelper() {
		treeMap = MapUtils.newHashMap();
	}
	
	/**
	 * 解析并添加url到目录树
	 * <p>
	 * 注意:所有url都会被转化为小写
	 * protocol://host:port/path?query#ref
	 * http://tester:123456@www.baidu.com?a=b&b=c&c=d#abc
	 * </p>
	 * 
	 * @param urlStr
	 * @return
	 */
	public void add(String urlStr) {
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
			String path = p1.matcher(url.getPath()).replaceFirst("");			//去除path开头的/
			String baseUrl =StringUtils.concat(
					protocol, "://", domain,  port!=-1?(':'+port):"");
			
			if(!path.isEmpty()) {
				String[] group = path.split("(/+)");
				add(baseUrl, group);
			} else {
				add(baseUrl, (String[])null);
			}
			
		} catch (MalformedURLException ex) {
			System.err.println(urlStr+"解析失败:"+ex.getMessage());
		}
		
	}
	
	/**
	 * 正则构建法
	 * @param urlStr
	 */
//	public void addByReg(String urlStr) {
//		String pre = RegexUtils.find(urlStr, "^(https?://)?([^/]+)(:\\d+)?/?");	//取出http://www.baidu.com/
//		System.out.println(pre);
//		urlStr = urlStr.substring(pre.length(), urlStr.length());	//源字符串变为/a?para1=2#s
//		System.out.println(urlStr);
//		String path = RegexUtils.find(urlStr, "^[^?#]+");	//取出字符串/a
//		System.out.println(path);
//		path.replaceFirst("/$", "");	//去除最后一个/
//		String[] group = path.split("(/+)");
//		Console.prettyLog(group);
//	}
	
	/**
	 * 转化树为ztree用的list，递归调用
	 * @param pid
	 * @param nodeMap
	 * @param itemList
	 */
	@SuppressWarnings("unchecked")
	private void convert2TreeItem(int pid, Map<String, Object> nodeMap, List<ZTreeItem> itemList) {
		Iterator<String> itor = nodeMap.keySet().iterator();
		while(itor.hasNext()) {
			String key = itor.next();
			Map<String, Object> childNodeMap = (Map<String, Object>) nodeMap.get(key);
			id++;
			ZTreeItem item = new ZTreeItem()
					.setId(id)
					.setpId(pid)
					.setName(key);
			
			if(!MapUtils.isEmpty(childNodeMap)) {	//子节点不为空，继续递归
				item.setOpen(true);
				item.setIsParent(true);
				itemList.add(item);
				convert2TreeItem(id, childNodeMap, itemList);
			} else {
				itemList.add(item);
			}
			
		}
	}
	
	/**
	 * 添加url及其对应的子节点到节点树
	 * @param url
	 * @param group
	 * @return 叶节点
	 */
	private Map<String, Object> add(String url, String[] group) {
		Map<String, Object> childNodeMap = add(url, treeMap);
		if(ListUtils.isEmpty(group)) {
			return childNodeMap;
		}
		return add(childNodeMap, group, 0);
	}
	
	private Map<String, Object> add(Map<String, Object> nodeMap, String[] group, int i) {
		Map<String, Object> childNodeMap = add(group[i], nodeMap);
		int nextIndex = i+1;
		if(nextIndex < group.length) {
			return add(childNodeMap, group, nextIndex);
		} else {
			return nodeMap;
		}
	}
	
	/**
	 * 往当前节点下增加节点,返回子节点对应节点
	 * @param nodeStr
	 * @param nodeMap
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> add(String nodeStr, Map<String, Object> nodeMap) {
		Map<String, Object> childNodeMap = null;
		if(!nodeMap.containsKey(nodeStr)) {
			childNodeMap = MapUtils.newHashMap();
			nodeMap.put(nodeStr, childNodeMap);
		} else {
			childNodeMap = (Map<String, Object>) nodeMap.get(nodeStr);
		}
		return childNodeMap;
	}
	
	public static void main(String[] args) {
		WebTreeHelper helper = new WebTreeHelper();
		String[] urls = new String[]{
//				"www.baidu.com/a/b/c?param1=2#s",
//				"http://192.168.162.100:8080/",
				"http://www.treejs.cn/v3/main.php#_zTreeInfo",
				"http://www.treejs.cn/v3/demo.php#_101",
				"http://www.treejs.cn/v3/api.php"
		};
		for (String url : urls) {
			helper.add(url);
		}
//		FileUtils.write(
//				"f:/临时/tree.json", 
//				GsonUtils.get().prettyPrinting().toJson(helper.getItemTree()),
//				StandardCharsets.UTF_8,
//				true);
	}
	
}
