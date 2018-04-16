package com.ag777.util.file.compress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import com.ag777.util.file.compress.base.BaseApacheCompressUtils;

/**
 * 有关zip文件的压缩和解压的工具基类,commons-compress二次封装
 * <p>
 * 为什么不用原生的zip库:https://blog.csdn.net/yk614294861/article/details/78961013
 * 简单来说:Windows 压缩的时候使用的是系统的编码 GB2312，而 Mac 系统默认的编码是 UTF-8，于是出现了乱码。
 * Apache commons-compress 解压 zip 文件是件很幸福的事，可以解决 zip 包中文件名有中文时跨平台的乱码问题，不管文件是在 Windows 压缩的还是在 Mac，Linux 压缩的，解压后都没有再出现乱码问题了。
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>commons-compress-1.16.1.jar</li>
 * </ul>
 * </p>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年04月12日,last modify at 2018年04月16日
 */
public class ZipUtils extends BaseApacheCompressUtils{

	private static ZipUtils mInstance;
	
	public static ZipUtils getInstance() {
		if(mInstance == null) {
			synchronized (ZipUtils.class) {
				if(mInstance == null) {
					mInstance = new ZipUtils();
				}
			}
		}
		return mInstance;
	}
	
	private ZipUtils() {}
	
	/*============压缩==================*/
	/**
	 * 
	 * @param files
	 * @param zipPath
	 * @return
	 * @throws IOException
	 */
	public  File zip(File[] files, String zipPath) throws IOException {
		return compress(files, zipPath);	//调用父类方法压缩文件
	}
	
	/*============解压==================*/
	/**
	 * 解压zip包到指定路径
	 * @param zipPath
	 * @param targetPath
	 * @throws IOException
	 */
	public  void unZip(String zipPath, String targetPath) throws IOException {
		decompress(zipPath, targetPath);	//调用父类方法解压文件
	}
	
	/*============实现父类方法==================*/
	
	@Override
	public ArchiveEntry getArchiveEntry(String filePath, File file, boolean isFile) {
		ZipArchiveEntry entry = new ZipArchiveEntry(filePath);
		return entry;
	}

	@Override
	public ArchiveOutputStream getArchiveOutputStream(String filePath) throws FileNotFoundException, IOException {
		ZipArchiveOutputStream stream = new ZipArchiveOutputStream(new File(filePath));
		stream.setUseZip64(Zip64Mode.AsNeeded); 
		return stream;
	}

	@Override
	public ArchiveInputStream getArchiveInputStream(InputStream is) throws FileNotFoundException {
		return new ZipArchiveInputStream(is);
	}
	
	public static void main(String[] args) throws Exception {
		ZipUtils.getInstance().zip(new File[]{new File("f:\\临时")}, "f:\\a.zip");
//		ZipUtils.getInstance().unZip("f:\\a.zip", "e:\\");
		
//		FileUtils.delete("f:\\a.zip");
//		FileUtils.delete("e:\\a\\");
	}
	
}
