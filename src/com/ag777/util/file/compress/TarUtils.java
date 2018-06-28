package com.ag777.util.file.compress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import com.ag777.util.file.FileUtils;
import com.ag777.util.file.compress.base.BaseApacheCompressUtils;

/**
 * 有关tar文件的压缩和解压的工具基类,commons-compress二次封装
 * <p>
 * 防止压缩文件名(或路径)过长时报错,但是这么压缩会导致不支持的系统解压不了
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>commons-compress-1.17.jar</li>
 * </ul>
 * </p>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年04月12日,last modify at 2018年04月16日
 */
public class TarUtils extends BaseApacheCompressUtils{

	public static TarUtils mInstance = new TarUtils();
	
	public static TarUtils getInstance() {
		if(mInstance == null) {
			synchronized (ZipUtils.class) {
				if(mInstance == null) {
					mInstance = new TarUtils();
				}
			}
		}
		return mInstance;
	}
	
	private TarUtils() {}
	
	/*============压缩==================*/
	/**
	 * 将一系列文件打包成tar包
	 * <p>
	 *  windows下文件中文乱码没关系,用工具打也会,
	 *  将getTarArchiveOutputStream设置成gbk编码就不会乱码但是会产生如下问题
	 *  ①只能在解压时也采用这种编码才能正常解压
	 *  ②linux下文件名乱码(ls命令会得到一堆乱码)
	 * </p>
	 * 
	 * @param files
	 * @param tarPath 目标tar文件路径
	 * @return
	 * @throws IOException
	 */
	public  File tar(File[] files, String tarPath) throws IOException {
		return compress(files, tarPath);	//调用父类方法压缩文件
	}
	
	/*=================解压====================*/
    /**
     * 解tar包
     * 
     * @param tarPath
     * @param targetPath
     * @throws IOException 
     */
	public  void unTar(String tarPath, String targetPath) throws IOException {
		decompress(tarPath, targetPath);	//调用父类方法解压文件
	}
	
	/*============实现父类方法==================*/
	
	@Override
	public ArchiveEntry getArchiveEntry(String filePath, File file, boolean isFile) {
		TarArchiveEntry entry = new TarArchiveEntry(filePath);
		entry.setSize(file.length());
		return entry;
	}
	
	@Override
	public ArchiveOutputStream getArchiveOutputStream(String filePath) throws FileNotFoundException {
		TarArchiveOutputStream stream = new TarArchiveOutputStream(FileUtils.getOutputStream(filePath));
		stream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);	//防止压缩文件名(或路径)过长时报错,但是这么压缩会导致不支持的系统解压不了
		return stream;
	}
	
	@Override
	public ArchiveInputStream getArchiveInputStream(InputStream is) throws FileNotFoundException {
		return new TarArchiveInputStream(is);
	}
}
