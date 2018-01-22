package com.ag777.util.file.csv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.csvreader.CsvWriter;

/**
 * csv文件写入辅助类
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>javacsv.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年01月22日,last modify at 2018年01月22日
 */
public class CsvBuilder {

	private final static String CHARSET = "utf-8";
	private OutputStream os;
	private CsvWriter csvWriter;
	
	public CsvBuilder(OutputStream os) {
		this.os = os;
		csvWriter = new CsvWriter(os, ',', Charset.forName(CHARSET));
	}
	
	public static CsvBuilder file(String filePath) throws FileNotFoundException {
		return new CsvBuilder(FileUtils.getOutputStream(filePath));
	}
	
	public void save() throws IOException {
		try {
			os.write(new byte[] { (byte) 0xEF, (byte) 0xBB,(byte) 0xBF });	//手动添加bom信息解决中文乱码
			csvWriter.flush();
			csvWriter.close();
		} catch (IOException e) {
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
	
	/**
	 * 写一行
	 * @param datas
	 * @throws IOException
	 */
	public void writeLine(String[] datas) throws IOException {
		csvWriter.writeRecord(datas);
	}
	
	/**
	 * 写一行
	 * @param data
	 * @throws IOException
	 */
	public void writeLine(String data) throws IOException {
		writeLine(new String[]{data});
	}
	
	/**
	 * 空一行
	 * @throws IOException
	 */
	public void writeEmptyLine() throws IOException {
		writeLine("");
	}
	
	/**
	 * 批量写入
	 * @param dataList
	 * @throws IOException
	 */
	public void writeList(String[][] dataList) throws IOException {
		//写每一行的数据
		for (String[] rowObj : dataList) {
			writeLine(rowObj);
		}
	}
	
	/**
	 * 批量写入
	 * @param dataList
	 * @throws IOException
	 */
	public void writeList(List<List<String>> dataList) throws IOException {
		//写每一行的数据
		for (List<String> rowObj : dataList) {
			writeLine(ListUtils.toArray(rowObj, String.class));
		}
	}
	
	/**
	 * 批量写入
	 * @param dataList
	 * @param keys
	 * @throws IOException
	 */
	public void writeListMap(List<Map<String, Object>> dataList, String[] keys) throws IOException {
		//写每一行的数据
		for (Map<String, Object> rowObj : dataList) {
			writeLine(getContent(rowObj, keys));
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
