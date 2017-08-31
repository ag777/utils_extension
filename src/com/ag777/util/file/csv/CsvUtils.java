package com.ag777.util.file.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvWriter;

/**
 * csv文件读写工具(暂时只提供写的方法)
 * 需要jar包 javacsv.jar
 * @author ag777
 *
 */
public class CsvUtils {

	private final static String CHARSET = "utf-8";
	private CsvUtils() {}
	
	/**
	 * 将数据写成csv格式并写出到输出流,最后关闭输出流
	 * @param dataList	数据列表
	 * @param keys	列表中数据对应的key,如果为null则为列表中第一项所有的键组成的数组
	 * @param titles	每列的标题，如果不需要标题则整个传null
	 * @param os	输出流
	 * @throws Exception
	 */
	public static void create(List<Map<String, Object>> dataList, String[] keys, String[] titles, OutputStream os)
			throws Exception {
		/*参数处理*/
		if(dataList == null) {
			dataList = new ArrayList<>();
		}
		
		if(keys == null && dataList.isEmpty()) {
			Map<String, Object> firstMap = dataList.get(0);
			if(!firstMap.isEmpty()) {
				keys = new String[firstMap.size()];
				int i=0;
				Iterator<String> itor = firstMap.keySet().iterator();
				while(itor.hasNext()) {
					keys[i] = itor.next();
					i++;
				}
			}
		}
		/*参数处理结束*/
		try {
			// 创建CSV写对象
			CsvWriter csvWriter = new CsvWriter(os, ',', Charset.forName(CHARSET));
			if (titles != null) {
				// 写表头
				csvWriter.writeRecord(titles);
			}

			//写每一行的数据
			for (Map<String, Object> rowObj : dataList) {
				csvWriter.writeRecord(getContent(rowObj, keys));
			}
			
			os.write(new byte[] { (byte) 0xEF, (byte) 0xBB,(byte) 0xBF });	//手动添加bom信息解决中文乱码
			csvWriter.flush();
			csvWriter.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if(os != null) {
				try {
					os.flush();
					os.close();
				} catch (IOException e) {
				}
			}
		}
	
	}

	/*--------内部工具方法---------------------*/
	/**
	 * 通过map和keys构造每一行的数据
	 * @param rowObj
	 * @param keys
	 * @return
	 */
	private static String[] getContent(Map<String, Object> rowObj, String[] keys) {
		String[] content = new String[keys.length];
		int i = 0;
		for (String key : keys) {
			if (rowObj.containsKey(key) && rowObj.get(key) != null) {
				content[i] = rowObj.get(key).toString();
			} else {
				content[i] = "";
			}
			i++;
		}
		return content;
	}
}
