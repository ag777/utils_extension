package com.ag777.util.file.yml;

import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.IOUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
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
	 * 通过文件读取Map数据
	 *
	 * @param file 要读取的文件
	 * @return 读取到的Map数据
	 * @throws FileNotFoundException 如果文件不存在，则抛出此异常
	 */
	public static Map<String, Object> readMapByFile(File file) throws FileNotFoundException {
	    FileInputStream in = null;
	    try {
	        in = FileUtils.getInputStream(file);
	        return yaml().loadAs(in, Map.class);
	    } finally {
	        IOUtils.close(in);
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
	 * 将对象写入到文件中
	 *
	 * @param obj 要写入的对象
	 * @param file 用于写入的文件
	 * @throws IOException 如果发生I/O错误，则抛出此异常
	 */
	public static void write(Object obj, File file) throws IOException {
	    write(obj, new FileWriter(file), null);
	}

	/**
	 * 将对象写入到文件中，支持自定义DumperOptions
	 *
	 * @param obj 要写入的对象
	 * @param file 用于写入的文件
	 * @param options 自定义的DumperOptions
	 * @throws IOException 如果发生I/O错误，则抛出此异常
	 */
	public static void write(Object obj, File file, DumperOptions options) throws IOException {
	    write(obj, new FileWriter(file), options);
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
