package com.ag777.util.file.yml;

import com.ag777.util.lang.collection.MapUtils;
import org.yaml.snakeyaml.DumperOptions;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
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
 * @version create on 2020年08月27日,last modify at 2024年10月03日
 */
public class YamlHelper {

	private Map<String, Object> map;
	
	public YamlHelper() {
		init();
	}
	public YamlHelper(Map<String, Object> map) {
		if(map != null) {
			this.map = map;
		} else {
			init();
		}
		
	}

	/**
	 * 从指定文件中读取YAML内容，并将其转换为YamlHelper对象
	 * 该方法使用系统默认字符集进行文件读取
	 *
	 * @param file 要读取的YAML文件
	 * @return 转换后的YamlHelper对象
	 * @throws IOException 如果文件读取过程中发生错误
	 */
	public static YamlHelper read(File file) throws IOException {
	    Map<String, Object> map = YamlUtils.readMapByFile(file);
	    return new YamlHelper(map);
	}

	/**
	 * 从指定文件中读取YAML内容，并将其转换为YamlHelper对象
	 * 该方法允许指定文件的字符集进行读取
	 *
	 * @param file 要读取的YAML文件
	 * @param charset 文件的字符集
	 * @return 转换后的YamlHelper对象
	 * @throws IOException 如果文件读取过程中发生错误
	 */
	public static YamlHelper read(File file, Charset charset) throws IOException {
	    Map<String, Object> map = YamlUtils.readMapByFile(file, charset);
	    return new YamlHelper(map);
	}

	/**
	 * 初始化 YamlHelper 实例
	 */
	private void init() {
	    this.map = MapUtils.newHashMap();
	}

	/**
	 * 获取 YAML 数据的 Map 表示
	 *
	 * @return 包含 YAML 数据的 Map
	 */
	public Map<String, Object> getMap() {
	    return map;
	}

	/**
	 * 获取 YAML 数据的扁平化 Map 表示
	 *
	 * @return 扁平化的 Map，其中嵌套的 Map 被展开，键为嵌套路径
	 */
	public Map<String, Object> getFlatMap() {
	    return flatMap(null, map, new LinkedHashMap<>(map.size()));
	}

	/**
	 * 保存 YAML 数据到指定的 Writer
	 *
	 * @param writer 用于写入 YAML 数据的 Writer
	 */
	public void save(Writer writer) {
	    YamlUtils.write(map, writer);
	}

	/**
	 * 保存 YAML 数据到指定的 Writer，使用给定的 DumperOptions
	 *
	 * @param writer 用于写入 YAML 数据的 Writer
	 * @param options Yaml 序列化选项
	 */
	public void save(Writer writer, DumperOptions options) {
	    YamlUtils.write(map, writer, options);
	}

	/**
	 * 递归地将嵌套的 Map 扁平化为一个 Map
	 *
	 * @param curKey 当前键的前缀，用于构建嵌套键
	 * @param map 需要扁平化的 Map
	 * @param resultMap 扁平化后的 Map
	 * @return 扁平化后的 Map
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> flatMap(String curKey, Map<String, Object> map, Map<String, Object> resultMap) {
        for (String key : map.keySet()) {
            Object value = map.get(key);
            // 拼接当前键和前缀键，形成完整的嵌套键
            if (curKey != null) {
                key = curKey + "." + key;
            }
            // 如果值是 Map，则递归扁平化
            if (value instanceof Map) {
                flatMap(key, (Map<String, Object>) value, resultMap);
            } else {
                resultMap.put(key, value);
            }
        }
	    return resultMap;
	}
	
}
