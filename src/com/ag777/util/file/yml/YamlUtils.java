package com.ag777.util.file.yml;

import com.ag777.util.lang.IOUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * yml工具类(对snakeyaml的使用进行做简单的参考)
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>snakeyaml-1.26.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2020年08月17日,last modify at 2024年10月03日
 */
public class YamlUtils {

	/**
	 * 通过文件名读取 YAML 文件内容并转换为 Map 对象
	 * 使用系统默认字符集
	 *
	 * @param file YAML 文件对象
	 * @return 将 YAML 文件内容转换得到的 Map 对象
	 * @throws IOException 如果读取文件发生错误
	 */
	public static Map<String, Object> readMapByFile(File file) throws IOException {
	    // 使用 try-with-resources 确保文件流在使用后能被正确关闭
	    try (InputStream in = new FileInputStream(file)) {
	        Yaml yaml = new Yaml();
	        return yaml.loadAs(in, Map.class);
	    }
	}

	/**
	 * 通过文件名读取 YAML 文件内容并转换为 Map 对象
	 * 指定字符集进行解码
	 *
	 * @param file YAML 文件对象
	 * @param charset 指定的字符集
	 * @return 将 YAML 文件内容转换得到的 Map 对象
	 * @throws IOException 如果读取文件发生错误
	 */
	public static Map<String, Object> readMapByFile(File file, Charset charset) throws IOException {
	    // 使用 try-with-resources 确保文件流和字符流在使用后能被正确关闭
	    try (InputStream in = new FileInputStream(file);
	         InputStreamReader reader = new InputStreamReader(in, charset)) {
	        Yaml yaml = new Yaml();
	        return yaml.loadAs(reader, Map.class);
	    }
	}

	/**
	 * 通过文本读取Map数据
	 *
	 * @param text 要读取的文本
	 * @return 读取到的Map数据
	 */
	public static Map<String, Object> readMap(String text) {
	    return yaml().loadAs(text, Map.class);
	}

	/**
	 * 将对象写入到Writer中
	 *
	 * @param obj 要写入的对象
	 * @param writer 用于写入的Writer
	 */
	public static void write(Object obj, Writer writer) {
	    write(obj, writer, null);
	}

	/**
	 * 将对象写入到Writer中，支持自定义DumperOptions
	 *
	 * @param obj 要写入的对象
	 * @param writer 用于写入的Writer
	 * @param options 自定义的DumperOptions
	 */
	public static void write(Object obj, Writer writer, DumperOptions options) {
	    try {
	        yaml(options).dump(obj, writer);
	    } finally {
	        IOUtils.close(writer);
	    }
	}

	/**
	 * 将对象转换为字符串并写入文件
	 * 该方法使用指定的字符集对对象进行序列化，并将其写入到指定的文件中
	 *
	 * @param obj 要写入的对象，可以是任何实现了toString()方法的对象
	 * @param file 要写入的文件对象，如果文件不存在，会被尝试创建；如果文件已存在，会被覆盖
	 * @param charset 指定的字符集，用于编码对象字符串
	 * @throws IOException 如果文件无法被正常写入，或者对象无法被序列化为字符串，则抛出IO异常
	 */
	public static void write(Object obj, File file, Charset charset) throws IOException {
	    write(obj, new FileWriter(file, charset), null);
	}

	/**
	 * 将对象转换为字符串并写入文件，支持自定义序列化选项
	 * 该方法类似于write(Object,File,Charset)，但增加了对序列化格式的自定义选项支持
	 *
	 * @param obj 要写入的对象，可以是任何实现了toString()方法的对象
	 * @param file 要写入的文件对象，如果文件不存在，会被尝试创建；如果文件已存在，会被覆盖
	 * @param charset 指定的字符集，用于编码对象字符串
	 * @param options 序列化选项对象，用于自定义序列化的格式和行为如果为null，则使用默认选项
	 * @throws IOException 如果文件无法被正常写入，或者对象无法被序列化为字符串，则抛出IO异常
	 */
	public static void write(Object obj, File file, Charset charset, DumperOptions options) throws IOException {
	    write(obj, new FileWriter(file, charset), options);
	}

	/**
	 * 将对象转换为字符串
	 *
	 * @param obj 要转换的对象
	 * @return 转换后的字符串
	 */
	public static String write2Str(Object obj) {
	    return write2Str(obj, null);
	}

	/**
	 * 将对象转换为字符串，支持自定义DumperOptions
	 *
	 * @param obj 要转换的对象
	 * @param options 自定义的DumperOptions
	 * @return 转换后的字符串
	 */
	public static String write2Str(Object obj, DumperOptions options) {
	    StringWriter sw = null;
	    try {
	        sw = new StringWriter();
	        yaml(options).dump(obj, sw);
	        return sw.toString();
	    } finally {
	        IOUtils.close(sw);
	    }
	}

	/**
	 * 创建默认配置的DumperOptions
	 *
	 * @return 默认配置的DumperOptions
	 */
	public static DumperOptions optionBlock() {
		// 创建 DumperOptions 并设置缩进和流式风格
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // 使用块状风格
		options.setIndent(2); // 设置缩进为2个空格
		return options;
	}

	/**
	 * 创建默认配置的Yaml实例
	 *
	 * @return 默认配置的Yaml实例
	 */
	private static Yaml yaml() {
	    return new Yaml();
	}

	/**
	 * 创建带有指定DumperOptions配置的Yaml实例
	 *
	 * @param options 指定的DumperOptions
	 * @return 带有指定DumperOptions配置的Yaml实例
	 */
	private static Yaml yaml(DumperOptions options) {
	    if (options == null) {
	        return yaml();
	    }
	    return new Yaml(options);
	}
}
