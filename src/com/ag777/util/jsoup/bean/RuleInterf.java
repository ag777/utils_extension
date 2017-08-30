package com.ag777.util.jsoup.bean;

/**
 * @author ag777
 * @Description 网页信息辅助类,提取整理自某开源爬虫库
 * Time: created at 2017/6/19. last modify at 2017/6/19.
 * Mark: 
 */
public interface RuleInterf {

	//模块筛选(大致范围)
	String getSelector();
	//获取方法,是html还是attr
	String getFun();
	//参数，配合attr使用
	String getParam();
	//匹配或捕获用的正则表达式
	String getRegex();
	//替换用的正则表达式(结果格式)
	String getReplacement();

}
