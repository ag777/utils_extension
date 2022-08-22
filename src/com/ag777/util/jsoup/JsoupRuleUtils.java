package com.ag777.util.jsoup;

import com.ag777.util.jsoup.model.Rule;
import com.ag777.util.lang.Console;
import com.ag777.util.lang.RegexUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.MapUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @version create on 2022年08月22日,last modify at 2022年08月22日
 */
public class JsoupRuleUtils {
    private JsoupRuleUtils() {}

    /**
     * 通过规则集合来提取静态页面信息
     * @param element html节点
     * @param ruleMap 规则集合
     * @return { key: 提取结果 }
     */
    public static Map<String, Object> findByRuleMap(Element element, Map<String, Rule> ruleMap) {
        if (MapUtils.isEmpty(ruleMap)) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>(ruleMap.size());
        for (String key : ruleMap.keySet()) {
            Rule rule =  ruleMap.get(key);
            resultMap.put(key, findByRule(element, rule));
        }
        return resultMap;
    }

    /**
     * 根据规则来提取信息
     * @param element html节点
     * @param rule 规则
     * @return 提取结果, 提取不到返回null
     */
    public static Object findByRule(Element element, Rule rule) {
        Pattern p = null;
        if (rule.getRegex() != null) {
            p = Pattern.compile(rule.getRegex());
        }
        return findByRule(element, rule.getCssQuery(), rule.isMultiple(), rule.getFunc(), rule.getAttrName(), p, rule.getReplacement());
    }

    /**
     * 根据规则来提取信息
     * @param element html节点
     * @param cssQuery 定位元素用的cssQuery
     * @param multiple 当cssQuery匹配到多个节点时，是否解析多个节点，否则只匹配第一个节点
     * @param func 提取方法，文字，属性等
     * @param attrName 如果提取方法是属性，则需要给予要提取的属性名
     * @param p 对提取结果进行正则匹配
     * @param replacement 对正则匹配结果进行正则替换
     * @return 提取结果, 提取不到返回null
     */
    public static Object findByRule(Element element, String cssQuery, boolean multiple, int func, String attrName, Pattern p, String replacement) {
        Elements es = element.select(cssQuery);
        if (ListUtils.isEmpty(es)) {
            return null;
        }
        if (multiple) {
            List<String> list = new ArrayList<>(es.size());
            for (Element e : es) {
                list.add(findOneByRule(e, func, attrName, p, replacement));
            }
            return list;
        } else {
            return findOneByRule(es.get(0), func, attrName, p, replacement);
        }
    }

    /**
     * 根据规则从html节点中提取单个信息
     * @param element html节点
     * @param func 提取方法，文字，属性等
     * @param attrName 如果提取方法是属性，则需要给予要提取的属性名
     * @param p 对提取结果进行正则匹配
     * @param replacement 对正则匹配结果进行正则替换
     * @return 提取结果, 提取不到返回null
     */
    private static String findOneByRule(Element element, int func, String attrName, Pattern p, String replacement) {
        //通过【fun】来提取对应属性
        String result;
        if (func == Rule.FUNC_ATTR) {
            result = element.attr(attrName);
        } else if (func == Rule.FUNC_HTML) {
            result = element.html();
        } else if (func == Rule.FUNC_TEXT) {
            result = element.text();
        } else {
            result = element.toString();
        }
        if (StringUtils.isEmpty(result)) {
            return null;
        }
        if (p != null) {
            Matcher matcher = p.matcher(result);
            if (matcher.find()) {
                if (StringUtils.isEmpty(replacement)) {
                    return matcher.group();
                } else {
                    return RegexUtils.getReplacement(matcher, replacement);
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        JsoupUtils u = JsoupUtils.connect("http://10.35.8.11/");
        /*
        {
          "a": [
            "全网"
          ],
          "b": "平台"
        }
         */
        Map<String, Object> map = findByRuleMap(u.getDoc().root(), MapUtils.of(
                String.class, Rule.class,
                "a", new Rule("#areabutton > ul > li > span")
                        .multiple()
                        .attr("onclick")
                        .regex("\\('(.+?)'\\)", "$1"),
                "b", new Rule("body > title")
        ));
        Console.prettyLog(map);
    }
}
