package com.ag777.util.file.compress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import com.ag777.util.file.FileUtils;
import com.ag777.util.file.compress.base.BaseApacheCompressUtils;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.exception.Assert;

/**
 * 有关tar.gz文件的压缩和解压的工具基类,java原生库的二次封装
 * 
 * @author ag777
 * @version create on 2018年04月12日,last modify at 2018年04月12日
 */
public class GzUtils {

	private GzUtils() {}
	
	/*============压缩==================*/
	/**
	 * 将文件压缩成gz包
	 * 
	 * @param filePath
	 * @param gzPath
	 * @return
	 * @throws IOException
	 */
	public static File gz(String filePath, String gzPath) throws IOException {
		Assert.notExisted(filePath, "需要压缩成.gz的文件不存在:"+filePath);
		GZIPOutputStream gos = null;
		InputStream is = null;
		try {
			is = FileUtils.getInputStream(filePath);
			gos = new GZIPOutputStream(FileUtils.getOutputStream(gzPath));
	
			int count;
			byte data[] = new byte[BaseApacheCompressUtils.BUFFER];
			while ((count = is.read(data, 0, BaseApacheCompressUtils.BUFFER)) != -1) {
				gos.write(data, 0, count);
			}
			
			gos.finish();
			return new File(gzPath);
		} catch(Exception ex) {
			throw ex;
		} finally {
			IOUtils.close(is, gos);
		}
		
	}
	
	
	/*============解压==================*/
	/**
	 * 将gz包解压成tar包
     * 
	 * @param gzPath
	 * @param tarPath
	 * @throws IOException
	 */
	public static void unGz(String gzPath, String tarPath) throws IOException {
		Assert.notExisted(gzPath, "需要解压的文件不存在:"+gzPath);
		GzipCompressorInputStream gcis = null;
		BufferedOutputStream bos = null;
		try {
			bos = FileUtils.getBufferedOutputStream(tarPath);  
	        gcis = new GzipCompressorInputStream(
	        		new BufferedInputStream(FileUtils.getInputStream(gzPath)));
	        
	        IOUtils.write(gcis, bos, BaseApacheCompressUtils.BUFFER);
		} catch(Exception ex) {
			throw ex;
		} finally {
			IOUtils.close(gcis, bos);
		}

	}
}
