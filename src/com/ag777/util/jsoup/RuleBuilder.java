package com.ag777.util.jsoup;

import com.ag777.util.jsoup.model.Rule;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version create on 2022年08月22日,last modify at 2022年08月22日
 */
public class RuleBuilder {
    public Rule rule;

    public RuleBuilder(Rule rule) {
        this.rule = rule;
    }

    public static RuleBuilder getInstance(String cssQuery) {
        return new RuleBuilder(new Rule(cssQuery));
    }

    public Rule build() {
        return rule;
    }

    public RuleBuilder multiple() {
        rule.setMultiple(true);
        return this;
    }

    public RuleBuilder str() {
        rule.setFunc(Rule.FUNC_STR);
        return this;
    }

    public RuleBuilder text() {
        rule.setFunc(Rule.FUNC_TEXT);
        return this;
    }

    public RuleBuilder html() {
        rule.setFunc(Rule.FUNC_HTML);
        return this;
    }

    public RuleBuilder attr(String attrName) {
        rule.setFunc(Rule.FUNC_ATTR);
        rule.setAttrName(attrName);
        return this;
    }

    public RuleBuilder regex(String regex) {
        rule.setRegex(regex);
        return this;
    }

    public RuleBuilder regex(String regex, String replacement) {
        rule.setRegex(regex);
        rule.setReplacement(replacement);
        return this;
    }
}
