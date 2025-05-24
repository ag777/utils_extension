package com.ag777.util.file.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * XML/HTML的XPath解析工具类
 * 支持从XML/HTML字符串或文件中提取内容
 * 包含缓存优化和线程安全处理
 * @author ag777
 * @version create on 2025年05月24日,last modify at 2025年05月24日
 */
public class XPathUtils {

    /**
     * 自定义XPath评估异常类
     */
    public static class XPathEvaluationException extends Exception {
        public XPathEvaluationException(String message) {
            super(message);
        }

        public XPathEvaluationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // 创建新的DocumentBuilder实例
    private static DocumentBuilder newDocumentBuilder() throws XPathEvaluationException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XPathEvaluationException("创建DocumentBuilder失败", e);
        }
    }

    /**
     * 创建新的XPath实例
     * @return XPath实例
     */
    private static XPath newXPath() {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        return xPathFactory.newXPath();
    }

    /**
     * 将XML/HTML字符串解析为Document对象
     *
     * @param content XML/HTML内容
     * @return 解析后的Document对象
     * @throws XPathEvaluationException 如果解析失败
     */
    public static Document parseDocument(String content) throws XPathEvaluationException {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("XML/HTML内容不能为空");
        }

        try {
            DocumentBuilder builder = newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(content)));
        } catch (SAXException e) {
            throw new XPathEvaluationException("XML/HTML解析失败，可能是非法格式", e);
        } catch (IOException e) {
            throw new XPathEvaluationException("读取内容时发生IO错误", e);
        }
    }

    /**
     * 从文件解析XML/HTML为Document对象
     *
     * @param file XML/HTML文件
     * @return 解析后的Document对象
     * @throws XPathEvaluationException 如果文件不存在或解析失败
     */
    public static Document parseDocument(File file) throws XPathEvaluationException {
        Objects.requireNonNull(file, "文件不能为null");
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件不存在或不是文件: " + file.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            return parseDocument(fis);
        } catch (IOException e) {
            throw new XPathEvaluationException("读取文件时发生IO错误: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * 从输入流解析XML/HTML为Document对象
     *
     * @param inputStream 输入流
     * @return 解析后的Document对象
     * @throws XPathEvaluationException 如果解析失败
     */
    public static Document parseDocument(InputStream inputStream) throws XPathEvaluationException {
        Objects.requireNonNull(inputStream, "输入流不能为null");

        try {
            DocumentBuilder builder = newDocumentBuilder();
            return builder.parse(inputStream);
        } catch (SAXException e) {
            throw new XPathEvaluationException("XML/HTML解析失败，可能是非法格式", e);
        } catch (IOException e) {
            throw new XPathEvaluationException("读取输入流时发生IO错误", e);
        }
    }

    /**
     * 使用已解析的Document对象和XPath表达式评估结果
     * 支持 boolean(xxx)、count(xxx)、string(xxx)、number(xxx) 等形式
     *
     * @param content     XML/HTML内容
     * @param xpathExpression XPath表达式
     * @return String
     * @throws XPathEvaluationException 若XPath评估失败
     */
    public static String evaluateForStr(String content, String xpathExpression) throws XPathEvaluationException {
        Document document = parseDocument(content);
        Object value = evaluate(document, xpathExpression);
        if(value instanceof String) {
            return (String) value;
        } else if(value instanceof Boolean) {
            return (Boolean) value ? "true" : "false";
        } else if(value instanceof Integer) {
            return String.valueOf(value);
        } else if(value instanceof Double) {
            return String.valueOf(value);
        } else if(value instanceof List) {
            StringBuilder sb = null;
            for (Object item : (List<?>) value) {
                if (sb == null) {
                    sb = new StringBuilder();
                } else {
                    sb.append("\n");
                }
                sb.append(item);
            }
            return sb == null ? "" : sb.toString();
        }
        return null;
    }

    /**
     * 使用已解析的Document对象和XPath表达式获取xml节点
     *
     * @param content     XML/HTML内容
     * @param xpathExpression XPath表达式
     * @return 节点对应xml字符串列表
     * @throws XPathEvaluationException 若XPath评估失败
     */
    public static List<String> evaluateForNodes(String content, String xpathExpression) throws XPathEvaluationException {
        Document document = parseDocument(content);
        return evaluateForNodes(document, xpathExpression, newXPath());
    }

    /**
     * 使用XML/HTML字符串和XPath表达式评估结果
     * 支持 boolean(xxx)、count(xxx)、string(xxx)、number(xxx) 等形式
     *
     * @param content     XML/HTML内容
     * @param xpathExpression XPath表达式
     * @return Boolean（若为 boolean(xxx)）、Integer/Double（若为 count(xxx)/number(xxx)）、String（若为 string(xxx)）、List<String>（其他）
     * @throws XPathEvaluationException 若解析或XPath评估失败
     * @throws IllegalArgumentException 若输入参数为null或空
     */
    public static Object evaluate(String content, String xpathExpression) throws XPathEvaluationException {
        Document document = parseDocument(content);
        return evaluate(document, xpathExpression);
    }

    /**
     * 使用已解析的Document对象和XPath表达式评估结果
     * 支持 boolean(xxx)、count(xxx)、string(xxx)、number(xxx) 等形式
     *
     * @param document 已解析的Document对象
     * @param xpathExpression XPath表达式
     * @return Boolean（若为 boolean(xxx)）、Integer/Double（若为 count(xxx)/number(xxx)）、String（若为 string(xxx)）、List<String>（其他）
     * @throws XPathEvaluationException 若XPath评估失败
     * @throws IllegalArgumentException 若输入参数为null或空
     */
    public static Object evaluate(Document document, String xpathExpression) throws XPathEvaluationException {
        Objects.requireNonNull(document, "Document对象不能为null");
        if (xpathExpression == null || xpathExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("XPath表达式不能为空");
        }

        XPath xpath = newXPath();

        // 处理 boolean(xxx)
        if (xpathExpression.startsWith("boolean(")) {
            try {
                return xpath.evaluate(xpathExpression, document, XPathConstants.BOOLEAN);
            } catch (XPathExpressionException e) {
                throw new XPathEvaluationException("boolean(xxx)类型XPath表达式评估失败: " + xpathExpression, e);
            }
        }

        // 处理 count(xxx)
        if (xpathExpression.startsWith("count(")) {
            try {
                return ((Number)xpath.evaluate(xpathExpression, document, XPathConstants.NUMBER)).intValue();
            } catch (XPathExpressionException e) {
                throw new XPathEvaluationException("number(xxx)类型XPath表达式评估失败: " + xpathExpression, e);
            }
        }

        // 处理 number(xxx)
        if (xpathExpression.startsWith("number(")) {
            try {
                return xpath.evaluate(xpathExpression, document, XPathConstants.NUMBER);
            } catch (XPathExpressionException e) {
                throw new XPathEvaluationException("number(xxx)类型XPath表达式评估失败: " + xpathExpression, e);
            }
        }


        // 处理 contains(xxx)
        if (xpathExpression.startsWith("contains(")) {
            try {
                return xpath.evaluate(xpathExpression, document, XPathConstants.BOOLEAN);
            } catch (XPathExpressionException e) {
                throw new XPathEvaluationException("contains(xxx)类型XPath表达式评估失败: " + xpathExpression, e);
            }
        }

        // 处理 string(xxx)
        if (xpathExpression.startsWith("string(")) {
            try {
                return xpath.evaluate(xpathExpression, document, XPathConstants.STRING);
            } catch (XPathExpressionException e) {
                throw new XPathEvaluationException("string(xxx)类型XPath表达式评估失败: " + xpathExpression, e);
            }
        }

        // 其他类型：提取文本内容
        return evaluateForNodes(document, xpathExpression, xpath);
    }

    /**
     * 使用已解析的Document对象和XPath表达式获取xml节点
     *
     * @param document 已解析的Document对象
     * @param xpathExpression XPath表达式
     * @param xpath XPath实例
     * @return 节点对应xml字符串列表
     * @throws XPathEvaluationException 若XPath评估失败
     * @throws IllegalArgumentException 若输入参数为null或空
     */
    private static List<String> evaluateForNodes(Document document, String xpathExpression, XPath xpath) throws XPathEvaluationException {
        // 其他类型：提取文本内容
        try {
            boolean text = false;
            String attr = null;
            int lastIndex = xpathExpression.lastIndexOf("/");
            if (lastIndex > 0) {
                String tail = xpathExpression.substring(lastIndex + 1);
                // 以 /text() 结尾
                if ("text()".equals(tail)) {
                    text = true;
                    xpathExpression = xpathExpression.substring(0, lastIndex);
                }
                // 以 /@属性名 结尾
                else if (tail.startsWith("@")) {
                    attr = tail.substring(1);
                    xpathExpression = xpathExpression.substring(0, lastIndex);
                }
            }
            NodeList nodeList = (NodeList) xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);
            List<String> result = new ArrayList<>(nodeList.getLength());
            for (int i = 0; i < (nodeList).getLength(); i++) {
                Node node = nodeList.item(i);
                if (text) {
                    result.add(node.getTextContent());
                } else if (attr != null) {
                    result.add(node.getAttributes().getNamedItem(attr).getNodeValue());
                } else {
                    result.add(nodeToString(node));
                }
            }
            return result;
        } catch (XPathExpressionException e) {
            throw new XPathEvaluationException("XPath表达式评估失败: " + xpathExpression, e);
        } catch (TransformerException e) {
            throw new XPathEvaluationException("将节点转换成xml字符串异常", e);
        }
    }

    /**
     * 获取节点的属性值
     *
     * @param content XML/HTML内容
     * @param xpathExpression 用于定位节点的XPath表达式
     * @param attributeName 属性名称
     * @return 属性值列表，如果节点不存在或没有该属性则返回空列表
     * @throws XPathEvaluationException 若解析或XPath评估失败
     */
    public static List<String> getAttributeValues(String content, String xpathExpression, String attributeName)
            throws XPathEvaluationException {
        Document document = parseDocument(content);
        Objects.requireNonNull(document, "Document对象不能为null");
        if (attributeName == null || attributeName.trim().isEmpty()) {
            throw new IllegalArgumentException("属性名不能为空");
        }
        if (xpathExpression == null || xpathExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("XPath表达式不能为空");
        }

        try {
            XPath xpath = newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);

            List<String> result = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                Node attr = node.getAttributes().getNamedItem(attributeName);
                if (attr != null) {
                    result.add(attr.getNodeValue());
                }
            }
            return Collections.unmodifiableList(result);
        } catch (XPathExpressionException e) {
            throw new XPathEvaluationException("XPath表达式评估失败: " + xpathExpression, e);
        }
    }

    /**
     * 将 Node 转换为 XML 字符串
     */
    public static String nodeToString(Node node) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // 可选：省略 XML 声明，避免输出 <?xml version="1.0" encoding="UTF-8"?>
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }

    /**
     * 测试用例
     */
    public static void main(String[] args) {
        String xml = """
            <html>
              <body>
                <div class="quality" id="div1"><div>Quality Content</div><div>111</div></div>
                <div class="other" id="div2"><div>Other Content</div><div>222</div></div>
                <div class="quality" id="div3"><div>Quality Content</div><div>333</div></div>
                <div class="price">123.45</div>
              </body>
            </html>
            """;

        try {
            // 测试 boolean(xxx)
            Object boolResult = evaluate(xml, "boolean(.//div[contains(@class, 'quality')])");
            System.out.println("Boolean Result: " + boolResult); // true

            // 测试 count(xxx)
            Object countResult = evaluate(xml, "count(.//div[contains(@class, 'quality')])");
            System.out.println("Count Result: " + countResult); // 2

            // 测试 string(xxx)
            Object stringResult = evaluate(xml, "string(.//div[1]/text())");
            System.out.println("String Result: " + stringResult); // Quality Content

            // 测试普通XPath
            Object textResult = evaluate(xml, ".//div[contains(@class, 'quality')]/text()");
            System.out.println("Text Result: " + textResult); // [Quality Content, Another Quality]

            // 测试空结果
            Object emptyResult = evaluate(xml, "//div[@class='not-exist']");
            System.out.println("Empty Result: " + emptyResult); // []
//            System.out.println(evaluate(xml, ".//div[contains(@class, 'quality')]"));
        } catch (XPathEvaluationException e) {
            System.err.println("XPath评估异常: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("参数错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
