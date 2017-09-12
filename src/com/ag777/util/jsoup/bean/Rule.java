package com.ag777.util.jsoup.bean;

import java.util.regex.Pattern;

/**
 * Created by ag777 on 2017/2/14.
 */
public class Rule implements RuleInterf{
    
    public String selector;
    
    public String fun;
    
    public String param;
    
    public String regex;
    
    public String replacement;
    
    public Pattern pattern;	//预处理

    public Rule(String selector, String fun, String param, String regex, String replacement) {
        this.selector = selector;
        this.fun = fun;
        this.param = param;
        this.regex = regex;
        this.replacement = replacement;
    }

    public Rule() {
    }

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getFun() {
		return fun;
	}

	public void setFun(String fun) {
		this.fun = fun;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	@Override
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public Pattern getPattern() {
		return pattern;
	}
	
}
