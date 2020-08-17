package com.ag777.util.file.yml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.io.Writer;
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
public class YmlUtils {

	@SuppressWarnings("unchecked")
	public static Map<String, Object> readMap(File file) throws FileNotFoundException {
		FileInputStream in = null;
		try {
			in = FileUtils.getInputStream(file);
			return yaml().loadAs(in, Map.class);
		} finally {
			IOUtils.close(in);
		}
		
	}
	
	public static void write(Object obj, Writer writer) {
		try {
			yaml().dump(obj, writer);
		} finally {
			IOUtils.close(writer);
		}
	}
	
	public static void write(Object obj, File file) {
		write(obj, file);
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
