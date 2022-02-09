package com.ag777.util.file.yml;

import java.io.*;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.IOUtils;

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
 * @version create on 2020年08月17日,last modify at 2020年08月17日
 */
public class YamlUtils {

	@SuppressWarnings("unchecked")
	public static Map<String, Object> readMapByFile(File file) throws FileNotFoundException {
		FileInputStream in = null;
		try {
			in = FileUtils.getInputStream(file);
			return yaml().loadAs(in, Map.class);
		} finally {
			IOUtils.close(in);
		}
		
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> readMap(String text) {
		return yaml().loadAs(text, Map.class);
	}
	
	public static void write(Object obj, Writer writer) {
		try {
			yaml().dump(obj, writer);
		} finally {
			IOUtils.close(writer);
		}
	}
	
	public static void write(Object obj, File file) throws IOException {
		write(obj, new FileWriter(file));
	}
	
	public static void toString(Object obj) {
		StringWriter sw = null;
		try {
			sw = new StringWriter();
			yaml().dump(obj, sw);
		} finally {
			IOUtils.close(sw);
		}
	}
	
	private static Yaml yaml() {
		return new Yaml();
	}
}
