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

    public final static String FUNC_NAME_STR = "str";  // toString
    public final static String FUNC_NAME_TEXT = "text";  // 节点内容
    public final static String FUNC_NAME_HTML = "html";  // 节点html
    public final static String FUNC_NAME_ATTR = "attr";  // 节点属性

    private String cssQuery;    // html节点
    private boolean multiple;   // 当cssQuery匹配到多个节点时，是否解析多个节点，否则只匹配第一个节点
    private int func;   // 提取方法，文字，属性等
    private String funcName;    // 提取方法 方便用更加友好的字符串的形式配置,优先于func
    private String attrName;    // 如果提取方法是属性，则需要给予要提取的属性名
    private String regex;   // 对提取结果进行正则匹配
    private String replacement; // 对正则匹配结果进行正则替换

    public Rule() {
    }

    public Rule(String cssQuery) {
        this.cssQuery = cssQuery;
        this.multiple = false;
        this.func = FUNC_TEXT;
    }

    public String getCssQuery() {
        return cssQuery;
    }

    public void setCssQuery(String cssQuery) {
        this.cssQuery = cssQuery;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public int getFunc() {
        return func;
    }

    public void setFunc(int func) {
        this.func = func;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
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
}
