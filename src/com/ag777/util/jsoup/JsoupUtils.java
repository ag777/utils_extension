package com.ag777.util.jsoup;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 有关 <code>Jsoup</code> 爬虫工具类
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>jsoup-1.11.3.jar</li>
 * </ul>
 * <ul>
 * <li>更新日志:https://github.com/jhy/jsoup/blob/master/CHANGES</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2017年06月05日,last modify at 2022年08月22日
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
//	private String html;

	public JsoupUtils(Document doc) {
		this.doc = doc;
//		this.html = doc.body().html();

	}

	/*==================入口函数======================*/
	/**
	 * 通过字符串构建
	 * @param html html
	 * @return JsoupUtils
	 */
	public static JsoupUtils parse(String html) {
		return new JsoupUtils(Jsoup.parse(html));
	}
	
	/**
	 * 通过文件构建
	 * @param in 文件
	 * @param charsetName 编码
	 * @return JsoupUtils
	 * @throws IOException io异常
	 */
	public static JsoupUtils parse(File in, String charsetName) throws IOException {
		return new JsoupUtils(Jsoup.parse(in, charsetName));
	}
	
	/**
	 * 连接目标url
	 * @param url url
	 * @return JsoupUtils
	 * @throws IOException io异常
	 */
	public static JsoupUtils connect(String url) throws IOException{
		Document doc = c(url).get();
		return new JsoupUtils(doc);
	}
	
	/**
	 * 连接目标url(post请求)
	 * @param url url
	 * @return JsoupUtils
	 * @throws IOException io异常
	 */
	public static JsoupUtils post(String url) throws IOException{
		Document doc = c(url).post();
		return new JsoupUtils(doc);
	}

	private static Connection c(String url) {
		return Jsoup.connect(url)
				.timeout(DEFAULT_TIME_OUT)
				.maxBodySize(0);	//解决爬取页面不完整的bug
	}

	/**
	 * 根据配置来做连接
	 * @param url url
	 * @param config 连接配置
	 * @return JsoupUtils
	 * @throws IOException io异常
	 */
	public static JsoupUtils connect(String url, JsoupBuilder config) throws IOException {
		Integer retryTimes = config.retryTimes();
		if(retryTimes == null || retryTimes < 1) {
			retryTimes = 1;
		}
		IOException ex = null;
		for(int i=0;i<retryTimes;i++) {
			try {
				Connection conn = getConn(url, config);
				return new JsoupUtils(conn.get());
			} catch(IOException e) {
				ex = e;
			}
		}
		throw ex;
	}
	
	/**
	 * 根据配置来做连接(post请求)
	 * @param url url
	 * @param config 连接配置
	 * @return JsoupUtils
	 * @throws IOException io异常
	 */
	public static JsoupUtils post(String url, JsoupBuilder config) throws IOException {
		Integer retryTimes = config.retryTimes();
		if(retryTimes == null || retryTimes < 1) {
			retryTimes = 1;
		}
		IOException ex = null;
		for(int i=0;i<retryTimes;i++) {
			try {
				Connection conn = getConn(url, config);
				return new JsoupUtils(conn.post());
			} catch(IOException e) {
				ex = e;
			}
		}
		throw ex;
	}
	
	/**
	 * 获取connection对象
	 * @param url url
	 * @param config 连接配置
	 * @return 连接
	 */
	private static Connection getConn(String url, JsoupBuilder config) {
//		Whitelist whitelist = Whitelist.basicWithImages();
//		Cleaner cleaner = new Cleaner(whitelist);
		Connection conn = Jsoup.connect(url).timeout(config.timeOut() == null?DEFAULT_TIME_OUT:config.timeOut());
		if(config.proxy() != null) {
			conn.proxy(config.proxy().ip, config.proxy().port);
		}
		if(config.userAgent() != null) {
			conn.userAgent(config.userAgent());
		}
		if(config.cookies() != null) {
			conn.cookies(config.cookies());
		}
		if(config.headers() != null) {
			conn.headers(config.headers());
		}
		if(config.ignoreContentType()) {
			conn.ignoreContentType(true);
		}
		if(config.ignoreHttpErrors()) {
			conn.ignoreHttpErrors(true);
		}
		if(config.dataMap() != null) {
			conn.data(config.dataMap());
		}
		if(config.maxBodySize() != null) {
			conn.maxBodySize(config.maxBodySize());
		}
		
		return conn;
	}
	
	/*===============接口方法======================*/
	
	public Document getDoc() {
		return doc;
	}

	public String getHtml() {
		return doc.body().html();
	}
	
	/**
	 * @return 格式化的html
	 */
	public String getPrettyHtml() {
		doc.outputSettings().prettyPrint(true);//是否格式化
		return doc.html();
	}

	/**
	 * @return 当前页的地址
	 */
	public String getUrl() {
		return doc.baseUri();
	}

	/**
	 * 利用白名单功能清除javascript标签
	 * <p>
	 * 详见filterTags(String... tags)方法注释
	 * </p>
	 * 
	 * @param tags 标签
	 * @return JsoupUtils
	 */
	public JsoupUtils filterScript(String... tags) {
		return filterTags();
	}
	
	/**
	 * 利用白名单清除标签
	 * <p>
	 * Whitelist本身允许的标签:
	 *  a, b, blockquote, br, caption, cite, code, col, colgroup, dd, dl, dt, em, h1, h2, h3, h4, h5, h6, i, img, li, ol, p, pre, q, small, strike, strong, sub, sup, table, tbody, td, tfoot, th, thead, tr, u, ul。结果不包含标签rel=nofollow ，如果需要可以手动添加
	 * 另外加上span标签,其余的标签均会被删除
	 * </p>
	 * 
	 * @return JsoupUtils
	 */
	public JsoupUtils filterTags(String... tags) {
		Whitelist whitelist = Whitelist.relaxed();
		whitelist.addTags("div", "span");
		whitelist.removeTags(tags);
		//增加可信属性
		whitelist.addAttributes(":all", "style", "class", "id", "name");
		whitelist.addAttributes("object", "width", "height","classid","codebase");
		whitelist.addAttributes("param", "name", "value");
		whitelist.addAttributes("embed", "src","quality","width","height","allowFullScreen","allowScriptAccess","flashvars","name","type","pluginspage");
		
		Cleaner cleaner = new Cleaner(whitelist);
		doc = cleaner.clean(doc);
		return this;
	}
	
	/**
	 * 通过id获取节点
	 * @param id id
	 * @return 节点
	 */
	public Element findById(String id) {
		return doc.getElementById(id);
	}

	/**
	 * 通过class获取节点集合
	 * @param className className
	 * @return 节点
	 */
	public Elements findByClass(String className) {
		return doc.getElementsByClass(className);
	}

	/**
	 * 通过tag获取节点集合
	 * @param tagName tagName
	 * @return 节点
	 */
	public Elements findByTag(String tagName) {
		return doc.getElementsByTag(tagName);
	}

	/**
	 * 通过css选择的器来选节点
	 * @param cssQuery cssQuery
	 * @return 节点
	 */
	public Elements select(String cssQuery) {
		return doc.select(cssQuery);
	}

	public Optional<Element> selectOne(String cssQuery) {
		Elements elements = select(cssQuery);
		if(elements.size() > 0) {
			return Optional.of(elements.get(0));
		}
		return Optional.empty();
	}
	
	/**
	 * @return 页面内所有图片
	 */
	public List<String> findImgUrls() {
		List<String> urls = new ArrayList<>();
		Elements media = doc.select("[src]");

		for (Element src : media) {
			if (src.tagName().equals("img")) {
				String url = src.attr("abs:src");

//				try {
//					String alt = src.attr("alt");
//					int width = Integer.parseInt(src.attr("width"));
//					int height = Integer.parseInt(src.attr("height"));
//				}catch(Exception ex) {
//				}
				urls.add(url);
			}

		}
		return urls;
	}

	/**
	 * 通过正则获取节点列表
	 * @param regex regex
	 * @return 匹配到的节点列表
	 */
	public List<Element> findByReg(String regex) {
		List<Element> list = new ArrayList<>();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(getHtml());

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
	 * @return 结果列表
	 */
	public List<String> findByReg(String cssQuery, String regex, String replacement) {
		List<String> list = new ArrayList<>();
		Elements es = select(cssQuery);
		for (Element element:
			es) {
			if(element != null) {
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(element.html());

				if (matcher.find()) {
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


	}
}