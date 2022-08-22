package com.ag777.util.jsoup.model;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version create on 2022年08月22日,last modify at 2022年08月22日
 */
public class Rule {
    public final static int FUNC_STR = 0;  // toString
    public final static int FUNC_TEXT = 1;  // 节点内容
    public final static int FUNC_HTML = 2;  // 节点html
    public final static int FUNC_ATTR = 3;  // 节点属性

    private String cssQuery;    // html节点
    private boolean multiple;   // 当cssQuery匹配到多个节点时，是否解析多个节点，否则只匹配第一个节点
    private int func;   // 提取方法，文字，属性等
    private String attrName;    // 如果提取方法是属性，则需要给予要提取的属性名
    private String regex;   // 对提取结果进行正则匹配
    private String replacement; // 对正则匹配结果进行正则替换

    public Rule(String cssQuery) {
        this.cssQuery = cssQuery;
        this.multiple = false;
        this.func = FUNC_TEXT;
    }

    public Rule cssQuery(String cssQuery) {
        this.cssQuery = cssQuery;
        return this;
    }

    public Rule multiple() {
        this.multiple = true;
        return this;
    }

    public Rule str() {
        this.func = FUNC_STR;
        return this;
    }

    public Rule text() {
        this.func = FUNC_TEXT;
        return this;
    }

    public Rule html() {
        this.func = FUNC_HTML;
        return this;
    }

    public Rule attr(String attrName) {
        this.func = FUNC_ATTR;
        this.attrName = attrName;
        return this;
    }

    public Rule regex(String regex) {
        this.regex = regex;
        return this;
    }

    public Rule regex(String regex, String replacement) {
        this.regex = regex;
        this.replacement = replacement;
        return this;
    }

    public String getCssQuery() {
        return cssQuery;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public int getFunc() {
        return func;
    }

    public String getAttrName() {
        return attrName;
    }

    public String getRegex() {
        return regex;
    }

    public String getReplacement() {
        return replacement;
    }
}
