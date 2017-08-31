package com.ag777.util.jsoup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.ag777.util.jsoup.bean.JSiteInterf;
import com.ag777.util.jsoup.bean.Rule;
import com.ag777.util.jsoup.bean.RuleInterf;
import com.ag777.util.lang.Console;
import com.ag777.util.lang.reflection.ReflectionHelper;

/**
 * @Description 爬虫工具类
 * 需要jar包 jsoup-1.10.2.jar
 * @author ag777
 * Time: created at 2017/6/5. last modify at 2017/6/27.
 * Mark: 
 */
public class JsoupUtils {

	private static int DEFAULT_TIME_OUT = 5000;	//默认超时时间
	
	public static int defaultTimeOut() {
		return DEFAULT_TIME_OUT;
	}

	public static void defaultTimeOut(int defaultTimeOut) {
		DEFAULT_TIME_OUT = defaultTimeOut;
	}

	private Document doc;
	private String html;

	public JsoupUtils(Document doc) {
		this.doc = doc;
		this.html = doc.body().html();

	}

	/*==================入口函数======================*/
	/**
	 * 通过字符串构建
	 * @param html
	 * @return
	 */
	public static JsoupUtils parse(String html) {
		return new JsoupUtils(Jsoup.parse(html));
	}
	/**
	 * 通过文件构建
	 * @param in
	 * @param charsetName
	 * @return
	 * @throws IOException
	 */
	public static JsoupUtils parse(File in, String charsetName) throws IOException {
		try {
			return new JsoupUtils(Jsoup.parse(in, charsetName));
		} catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * 连接目标url
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static JsoupUtils connect(String url) throws Exception{
		return connect(url, DEFAULT_TIME_OUT);
	}
	/**
	 * 连接目标url
	 * @param url
	 * @param timeOut
	 * @return
	 * @throws Exception
	 */
	public static JsoupUtils connect(String url, int timeOut) throws Exception{
		try {
			Whitelist whitelist = Whitelist.basicWithImages();
			Cleaner cleaner = new Cleaner(whitelist);
			Document doc = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
					.timeout(timeOut).get();

//			doc = cleaner.clean(doc);
			return new JsoupUtils(doc);

		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * 连接url(通过代理)
	 * @param url
	 * @param proxyIp		代理ip
	 * @param proxyPort	代理端口号
	 * @return
	 * @throws Exception
	 */
	public static JsoupUtils connect(String url, String proxyIp, int proxyPort) throws Exception{
		return connect(url, proxyIp, proxyPort, DEFAULT_TIME_OUT);
	}
	/**
	 * 连接url(通过代理)
	 * @param url
	 * @param proxyIp		代理ip
	 * @param proxyPort	代理端口号
	 * @param timeOut
	 * @return
	 * @throws Exception
	 */
	public static JsoupUtils connect(String url, String proxyIp, int proxyPort, int timeOut) throws Exception{
		try {
			Whitelist whitelist = Whitelist.basicWithImages();
			Cleaner cleaner = new Cleaner(whitelist);
			Document doc = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36")
					.proxy(proxyIp, proxyPort).timeout(timeOut).get();

//			doc = cleaner.clean(doc);
			return new JsoupUtils(doc);

		} catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * 通过站点信息获取列表信息
	 * 流程:
	 * 	1.拉取JSiteInterf所有类型为RuleInterf(及其子类)的成员变量
	 * 2.爬取网页
	 * 3.调用findByRule方法拉取结果
	 * @param JSiteInterf
	 * @return
	 */
	public static List<Map<String,Object>> findBySite(JSiteInterf JSiteInterf) throws Exception {
		return findBySite(JSiteInterf, DEFAULT_TIME_OUT);
	}
	/**
	 * 通过站点信息获取列表信息
	 * @param JSiteInterf
	 * @param timeOut
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>> findBySite(JSiteInterf JSiteInterf,  int timeOut) throws Exception {
		Map<String, RuleInterf> params = new ReflectionHelper<>(JSiteInterf.class)
				.getFieldMap(JSiteInterf, RuleInterf.class);
		return connect(JSiteInterf.getUrl(), timeOut).findByRule(JSiteInterf.getCssQuery(), params);
	}
	
	/*===============接口方法======================*/
	
	public Document getDoc() {
		return doc;
	}

	public String getHtml() {
		return html;
	}

	/**
	 * 通过id获取节点
	 * @param id
	 * @return
	 */
	public Element findById(String id) {
		return doc.getElementById(id);
	}

	/**
	 * 通过class获取节点集合
	 * @param className
	 * @return
	 */
	public Elements findByClass(String className) {
		return doc.getElementsByClass(className);
	}

	/**
	 * 通过tag获取节点集合
	 * @param tagName
	 * @return
	 */
	public Elements findByTag(String tagName) {
		return doc.getElementsByTag(tagName);
	}

	/**
	 * 通过css选择的器来选节点
	 * @param cssQuery
	 * @return
	 */
	public Elements select(String cssQuery) {
		return doc.select(cssQuery);
	}

	/**
	 * 寻找页面内所有图片
	 * @return
	 */
	public List<String> findImgUrls() {
		List<String> urls = new ArrayList<String>();
		Elements media = doc.select("[src]");

		for (Element src : media) {
			if (src.tagName().equals("img")) {
				String url = src.attr("abs:src");

				try {
					String alt = src.attr("alt");
					int width = Integer.parseInt(src.attr("width"));
					int height = Integer.parseInt(src.attr("height"));
				}catch(Exception ex) {
				}
				urls.add(url);
			}

		}
		return urls;
	}

	/**
	 * 通过正则获取节点列表
	 * @param regex
	 * @return
	 */
	public List<Element> findByReg(String regex) {
		List<Element> list = new ArrayList<Element>();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(html);

		while (matcher.find()) {
			String html = matcher.group();
			Element element = Jsoup.parseBodyFragment(html).body();
			list.add(element);
		}
		return list;
	}

	/**
	 * 通过正则获取匹配的信息集合
	 * 流程:
	 * 1.通过cssQuery获取目标节点的html
	 * 2.直接进行正则匹配
	 * 3.通过replacement拼接结果添加到集合里并返回
	 * @param cssQuery		css选择器，想找到目标容器的大致范围(目标的父容器)
	 * @param regex			匹配用的正则表达式
	 * @param replacement	置换规则（将正则匹配的结果替换为想要的格式）
	 * @return
	 */
	public List<String> findByReg(String cssQuery, String regex, String replacement) {
		List<String> list = new ArrayList<>();
		Elements es = select(cssQuery);
		for (Element element:
			es) {
			if(element != null) {
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(element.html());

				if (!matcher.find()) {

				} else if (matcher.groupCount() >= 1) {

					for (int i = 1; i <= matcher.groupCount(); i++) {
						String replace = matcher.group(i);
						replacement = replacement.replaceAll("\\$" + i, (replace != null) ? replace : "");
					}
					list.add(replacement);
				}
			}
		}

		return list;
	}


	

	/**
	 * 先找到对应节点再通过finderMap找到结果集
	 * 流程:
	 * 	1.通过cssQuery找到html中的对应节点
	 * 	2.遍历params,寻找finder对应的结果并回填到map里
	 * @param cssQuery
	 * @param params
	 * @return
	 */
	public List<Map<String,Object>> findByRule(String cssQuery, Map<String, RuleInterf> params) {
		
		List<Map<String,Object>> result = new ArrayList<>();
		Elements es = select(cssQuery);
		for (Element item:
				es) {
			Map<String,Object> map = new HashMap<>();
			Iterator<String> iter = params.keySet().iterator();
			while(iter.hasNext()){
			    String key = iter.next();
				RuleInterf RuleInterf = params.get(key);
				if(RuleInterf != null) {
					Elements es2 = item.select(RuleInterf.getSelector());

					String target = findByRule(es2, RuleInterf);
					map.put(key, target);
				}

			}
			result.add(map);
		}

		return result;
	}

	/**
	 * 从节点集中找到对应结果，一旦找到一个匹配的就直接返回
	 * @param items
	 * @param RuleInterf
	 * @return
	 */
	public String findByRule(Elements items, RuleInterf RuleInterf) {
		for (Element item:
			items) {
			try {
				String result = findByRule(item, RuleInterf);
				if(result == null) {
					continue;
				} else {
					return result;
				}
			} catch(Exception ex) {
				continue;
			}
		}
		return null;
	}

	/**
	 * 通过规则得到节点中匹配的值
	 * 流程:
		 * 1.通过select找到对应节点
		 * 2.提取对应属性中的值
		 * 3.正则提取结果字符串中的相应部分
	 * @param item
	 * @param RuleInterf
	 * @return
	 */
	public String findByRule(Element item, RuleInterf RuleInterf) {
		
		String result = null;
		Elements target = item.select(RuleInterf.getSelector());
		if(target != null) {	//通过【selector】找到节点

			//通过【fun】来提取对应属性
			if ("attr".equals(RuleInterf.getFun())) {
				result = item.attr(RuleInterf.getParam());
			} else if ("html".equals(RuleInterf.getFun())) {
				result = item.html();
			} else if ("text".equals(RuleInterf.getFun())) {
				result = item.text();
			} else {
				result = item.toString();
			}

			result = get(result, RuleInterf.getRegex(), RuleInterf.getReplacement());

		}

		return result;
	}


	/**
	 * 正则-通过字符串提取相应的结果
	 * @param target
	 * @param regex
	 * @param replacement
	 * @return
	 */
	private String get(String target, String regex, String replacement) {
		if(target != null && regex != null) {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(target);

			if (!matcher.find()) {

			} else if (matcher.groupCount() >= 1) {
				if (replacement != null) {
					for (int i = 1; i <= matcher.groupCount(); i++) {
						String replace = matcher.group(i);
						replacement =  replacement.replaceAll("\\$" + i, (replace != null) ? replace : "");
					}
					return replacement;
				}else {	//不需要替换
					return matcher.group(1);
				}

			}

		} else {
			return target;
		}
		return null;
	}



	public static void main(String[] args) {
//		List<Element> html = connect("https://tieba.baidu.com/f?kw=%C1%E3%D6%AE%D3%C0%BA%E3").findByReg("<span[^>]+?>([\\s\\S]*?)</span>");
//		Elements list = connect("https://www.pixiv.net/search.php?s_mode=s_tag_full&word=%E6%9D%B1%E6%96%B9&p=%7Bpage%3A1%7D").select("a[href]");
//		for (Element element : list) {
//			System.out.println(element.toString());
//		}

//		JsoupUtils util = connect("https://www.pixiv.net/search.php?s_mode=s_tag_full&word=東方&p={page:1}");
////		System.out.println(util.getHtml());
//		Elements es = util.select("ul._image-items>li.image-item, section.ranking-item");
//		for (Element item:
//			 es) {
//			String title = item.select("a>h1.title, h2>a.title").html();
//			String cover = item.select("div._layout-thumbnail>img").attr("src");
//			String datetime = item.select("a.work img._thumbnail").attr("data-src");
//
//			String reg = ".*img/(\\d{4})/(\\d{2})/(\\d{2})/(\\d{2})/(\\d{2})/(\\d{2})";
//			datetime = datetime.replace(reg, "$1-$2-$3 $4:$5:$6");
//			System.out.println(datetime);
////			System.out.println(title+"||"+cover+"||"+datetime);
//		}

		Map<String, RuleInterf> params = new HashMap<>();
//		params.put("标题", new Rule("a>h1.title, h2>a.title", "html", null, null, null));
		params.put("封面", new Rule("div._layout-thumbnail>img", null, null, "\"(https?://[^\"]*?\\.(?:jpg|jpeg|png|bmp))\"", null));
//		params.put("日期", new Rule("a.work img._thumbnail", "attr", "data-src", ".*img/(\\d{4})/(\\d{2})/(\\d{2})/(\\d{2})/(\\d{2})/(\\d{2})", "$1-$2-$3 $4:$5:$6"));
		try {
			List<Map<String, Object>> result = connect("https://www.pixiv.net/search.php?s_mode=s_tag_full&word=東方&p={page:1}")
					.findByRule("ul._image-items>li.image-item, section.ranking-item", params);
			Console.log(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}