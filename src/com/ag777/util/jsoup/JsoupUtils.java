package com.ag777.util.jsoup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import com.ag777.util.jsoup.interf.RuleInterf;
import com.ag777.util.jsoup.model.Rule;
import com.ag777.util.lang.Console;
import com.ag777.util.lang.collection.MapUtils;

/**
 * 有关 <code>Jsoup</code> 爬虫工具类
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>jsoup-1.10.2.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2017年06月05日,last modify at 2018年05月16日
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
	public static JsoupUtils connect(String url) throws IOException{
		try {
			Document doc = Jsoup.connect(url).timeout(DEFAULT_TIME_OUT).get();
			return new JsoupUtils(doc);
		} catch(IOException ex) {
			throw ex;
		}
	}


	/**
	 * 根据配置来做连接
	 * @param url
	 * @param config
	 * @return
	 * @throws IOException
	 */
	public static JsoupUtils connect(String url, JsoupBuilder config) throws IOException {
		Integer retryTimes = config.retryTimes();
		if(retryTimes == null) {
			retryTimes = 1;
		}
//		Whitelist whitelist = Whitelist.basicWithImages();
//		Cleaner cleaner = new Cleaner(whitelist);
		IOException ex = null;
		for(int i=0;i<retryTimes;i++) {
			try {
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
				if(config.dataMap() != null) {
					conn.data(config.dataMap());
				}
				return new JsoupUtils(conn.get());
			} catch(IOException e) {
				ex = e;
			}
		}
		throw ex;
	}
	
	/*===============接口方法======================*/
	
	public Document getDoc() {
		return doc;
	}

	public String getHtml() {
		return html;
	}
	
	/**
	 * 获取格式化的html
	 * @return
	 */
	public String getPrettyHtml() {
		doc.outputSettings().prettyPrint(true);//是否格式化
		return doc.html();
	}

	/**
	 * 获取当前页的地址
	 * @return
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
	 * @param tags
	 * @return
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
	 * @return 
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

	public Optional<Element> selectOne(String cssQuery) {
		Elements elements = select(cssQuery);
		if(elements.size() > 0) {
			return Optional.of(elements.get(0));
		}
		return Optional.ofNullable(null);
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
	 * 通过rulemap查找结果列表
	 * @param ruleMap
	 * @param cssQuery	缩小查找范围
	 * @return
	 * @throws java.util.regex.PatternSyntaxException
	 */
	public List<Map<String, Object>> findListByRuleMap(Map<String, RuleInterf> ruleMap, String cssQuery) throws java.util.regex.PatternSyntaxException {
		List<Map<String, Object>> result = new ArrayList<>();
		Elements elements = doc.select(cssQuery);
		if(elements != null && !elements.isEmpty()) {
			for (Element element : elements) {
				Map<String, Object> item = new HashMap<String, Object>();
				Iterator<String> itor = ruleMap.keySet().iterator();
				while(itor.hasNext()) {
					String key = itor.next();
					RuleInterf ruleInterf = ruleMap.get(key);
					item.put(key, findByRule(ruleInterf, element));
				}
				result.add(item);
			}
		}
		return result;
	}
	
	/**
	 * 通过rulemap查找结果
	 * @param ruleMap
	 * @param cssQuery	缩小查找范围
	 * @return
	 * @throws java.util.regex.PatternSyntaxException
	 */
	public Map<String, Object> findByRuleMap(Map<String, RuleInterf> ruleMap, String cssQuery) throws java.util.regex.PatternSyntaxException {
		Elements elements = doc.select(cssQuery);
		if(elements != null && !elements.isEmpty()) {
			Map<String, Object> result = new HashMap<String, Object>();
			Iterator<String> itor = ruleMap.keySet().iterator();
			while(itor.hasNext()) {
				String key = itor.next();
				RuleInterf ruleInterf = ruleMap.get(key);
				result.put(key, findByRule(ruleInterf, elements.get(0)));
			}
			return result;
		}
		
		return null;
	}
	
	/**
	 * 通过rulemap查找结果列表
	 * @param ruleMap
	 * @param elements
	 * @return
	 * @throws java.util.regex.PatternSyntaxException
	 */
	public List<Map<String, Object>> findListByRuleMap(Map<String, RuleInterf> ruleMap, Elements elements) throws java.util.regex.PatternSyntaxException {
		List<Map<String, Object>> result = new ArrayList<>();
		if(elements != null) {
			for (Element element : elements) {
				result.add(findByRuleMap(ruleMap, element));
			}
		}
		return result;
	}
	
	/**
	 * 通过rulemap查找结果
	 * @param ruleMap key以及对应的map
	 * @param element
	 * @return
	 * @throws java.util.regex.PatternSyntaxException
	 */
	public Map<String, Object> findByRuleMap(Map<String, RuleInterf> ruleMap, Element element) throws java.util.regex.PatternSyntaxException {
		Map<String, Object> result = new HashMap<String, Object>();
		Iterator<String> itor = ruleMap.keySet().iterator();
		while(itor.hasNext()) {
			String key = itor.next();
			RuleInterf ruleInterf = ruleMap.get(key);
			result.put(key, findByRule(ruleInterf, element));
		}
		return result;
	}
	
	/**
	 * 通过jsonMap(特定规则的map)来获取结果
	 * @param jsonMap
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> findByJsonMap(Map<String, Object> jsonMap) throws Exception {
		return findByJsonMap(jsonMap, doc, "data");
	}
	
	/**
	 * 通过jsonMap(特定规则的map)来获取结果
	 * @param jsonMap 
	 * @param topElement 根遍历节点，默认为doc
	 * @param defaultKey 根节点默认的key(根节点直接对应列表)
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String,Object> findByJsonMap(Map<String, Object> jsonMap, Element topElement, String defaultKey) throws Exception {
		try {
			Map<String,Object> result = new HashMap<>();
			if(jsonMap.containsKey("item")) {
				List<Map<String,Object>> list = new ArrayList<>();
				Map<String,Object> selectorMap = (Map<String, Object>) jsonMap.get("item");
				String selector = (String) selectorMap.get("selector");
//				jsonMap.remove("item");
				Elements elements = topElement.select(selector);
				
				for (Element element : elements) {
					Iterator<String> itor = jsonMap.keySet().iterator();
					Map<String, Object> item = new HashMap<>();
					while(itor.hasNext()) {
						String key = itor.next();
						if("item".equals(key)) {
							continue;
						}
						Map<String, Object> params = (Map<String, Object>) jsonMap.get(key);
						item.put(key, findByJsonMap(element, params));
					}
					list.add(item);
				}
				result.put(defaultKey, list);
			} else if(!jsonMap.containsKey("selector")){
			
				Iterator<String> itor = jsonMap.keySet().iterator();
				while(itor.hasNext()) {
					String key = itor.next();
					if("item".equals(key)) {
						continue;
					}
					Map<String, Object> map = (Map<String, Object>) jsonMap.get(key);
					result.putAll(findByJsonMap(map, topElement, key));
				}
			} else {
				result.put(defaultKey, findByJsonMap(topElement, jsonMap));
			}
			return result;
		} catch(Exception ex) {
			Console.err(ex);
			throw new Exception("json不正确");
		}
	}
	
	/**
	 * 通过规则得到节点中匹配的值
	 * 流程:
		 * 1.通过select找到对应节点
		 * 2.提取对应属性中的值
		 * 3.正则提取结果字符串中的相应部分
	 * @param ruleInterf
	 * @param cssQuery 缩小查找范围
	 * @return
	 * @throws java.util.regex.PatternSyntaxException
	 */
	public List<String> findListByRule(RuleInterf ruleInterf, String cssQuery) throws java.util.regex.PatternSyntaxException{
		List<String> result = new ArrayList<>();
		Elements elements = doc.select(cssQuery);
		if(elements != null && !elements.isEmpty()) {
			for (Element element : elements) {
				String item = findByRule(ruleInterf, element);
				if(item != null) {
					result.add(item);
				}
			}
		}
		return result;
	}
	
	/**
	 * 通过规则得到节点中匹配的值
	 * 流程:
		 * 1.通过select找到对应节点
		 * 2.提取对应属性中的值
		 * 3.正则提取结果字符串中的相应部分
	 * @param ruleInterf
	 * @param element	 查找范围,传null则为默认的doc
	 * @return
	 * @throws java.util.regex.PatternSyntaxException 正则表达式错误
	 */
	public String findByRule(RuleInterf ruleInterf, Element element) throws java.util.regex.PatternSyntaxException{
		if(element == null) {
			element = doc;
		}
		
		String result = null;
		Elements target = element.select(ruleInterf.getSelector());
		if(target != null) {	//通过【selector】找到节点

			//通过【fun】来提取对应属性
			if ("attr".equals(ruleInterf.getFun())) {
				result = element.attr(ruleInterf.getParam());
			} else if ("html".equals(ruleInterf.getFun())) {
				result = element.html();
			} else if ("text".equals(ruleInterf.getFun())) {
				result = element.text();
			} else {
				result = element.toString();
			}

			if(ruleInterf.getRegex() != null && ruleInterf.getPattern() == null) {
				ruleInterf.setPattern(Pattern.compile(ruleInterf.getRegex()));
			}
			result = get(result, ruleInterf.getPattern(), ruleInterf.getReplacement());

		}

		return result;
	}
	
	
	/**
	 * 根据规则及对应节点获取结果
	 * @param element
	 * @param params
	 * @return
	 */
	private String findByJsonMap(Element element, Map<String, Object> params) {
		String result = null;
		Elements target = element.select(MapUtils.getStr(params, "selector"));
		if(target != null) {	//通过【selector】找到节点

			//通过【fun】来提取对应属性
			String fun = MapUtils.getStr(params, "fun");
			if ("attr".equals(fun)) {
				result = target.attr(MapUtils.getStr(params, "param"));
			} else if ("html".equals(fun)) {
				result = target.html();
			} else if ("text".equals(fun)) {
				result = target.text();
			} else {
				result = target.toString();
			}
			//预存pattern,实际上直接用正则也能获取到结果
			String reg = MapUtils.getStr(params, "regex");
			if(reg != null && !params.containsKey("pattern")) {
				params.put("pattern", Pattern.compile(reg));
			}
			result = get(result, (Pattern) params.get("pattern"), MapUtils.getStr(params, "replacement"));

		}
		return result;
	}

	/**
	 * 通过正则表达式对应的Pattern提取相应的结果
	 * @param target
	 * @param pattern
	 * @param replacement
	 * @return
	 */
	private String get(String target, Pattern pattern, String replacement) {
		if(target != null && pattern != null) {
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
		params.put("标题", new Rule("a>h1.title, h2>a.title", "html", null, null, null));
		params.put("封面", new Rule("div._layout-thumbnail>img", null, null, "\"(https?://[^\"]*?\\.(?:jpg|jpeg|png|bmp))\"", null));
////		params.put("日期", new Rule("a.work img._thumbnail", "attr", "data-src", ".*img/(\\d{4})/(\\d{2})/(\\d{2})/(\\d{2})/(\\d{2})/(\\d{2})", "$1-$2-$3 $4:$5:$6"));
		try {
			//需要登录后的cookie
			List<Map<String, Object>> result = connect("https://www.pixiv.net/search.php?s_mode=s_tag_full&word=東方&p={page:1}")
					.findListByRuleMap(params, "ul._image-items>li.image-item, section.ranking-item");
			Console.log(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}