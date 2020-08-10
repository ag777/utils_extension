package com.ag777.util.file.compress;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.ag777.util.lang.RegexUtils;
import com.ag777.util.lang.StringUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

/**
 * 有关zip的工具类,对zip4j的二次封装
 * <p>
 * 注意:密码最好不要用中文，否则无法用winrar等工具打开，只能用该工具类解压
 * 
 * <p>需要jar包:
 * <ul>
 * <li>zip4j-x.x.x.jar</li>
 * </ul>
 * 
 * <p>项目路径:https://github.com/srikanth-lingala/zip4j
 * 
 * @author ag777
 * @version create on 2019年08月01日,last modify at 2020年08月07日
 */
public class Zip4jUtils {
	
	/**
	 * 压缩文件和文件夹
	 * @param fileList
	 * @param targetPath
	 * @param password
	 * @return 
	 * @throws ZipException
	 */
	public static File zip(List<File> fileList, String zipPath, String password) throws ZipException {
		new File(zipPath).delete();	//并不完全覆盖，如果原文件存在会保持原来的,参考:https://www.jb51.net/article/125808.htm
		ZipTemp temp = getZipTemp(zipPath, password);
		ZipFile zipFile = temp.zipFile;
		ZipParameters zipParameters = temp.zipParameters;

		for (File file : fileList) {
			if(file.isDirectory()) {
				zipFile.addFolder(file, zipParameters);
			} else {
				zipFile.addFile(file, zipParameters);
			}
			
		}
		
		return zipFile.getFile();
	}
	
	/**
	 * 打包并拆分压缩包(仅支持多个文件)
	 * @param fileList
	 * @param zipPath
	 * @param limitSize Zip file format specifies a minimum of 65536 bytes (64kb) as a minimum length for split files. Zip4j will throw an exception if anything less than this value is specified.
	 * @param password
	 * @return 
	 * @throws ZipException
	 */
	public static ZipFile split(List<File> fileList, String zipPath, Integer limitSize, String password) throws ZipException {
		new File(zipPath).delete();	//并不完全覆盖，如果原文件存在会保持原来的,参考:https://www.jb51.net/article/125808.htm
		ZipTemp temp = getZipTemp(zipPath, password);
		ZipFile zipFile = temp.zipFile;
		ZipParameters zipParameters = temp.zipParameters;
		
		zipFile.createSplitZipFile(fileList, zipParameters, true, limitSize);
		return zipFile;
	}
	
	/**
	 * 打包并拆分压缩包(仅支持单个文件夹)
	 * @param folder
	 * @param zipPath
	 * @param limitSize Zip file format specifies a minimum of 65536 bytes (64kb) as a minimum length for split files. Zip4j will throw an exception if anything less than this value is specified.
	 * @param password
	 * @return 
	 * @throws ZipException
	 */
	public static ZipFile split(File folder, String zipPath, Integer limitSize, String password) throws ZipException {
		new File(zipPath).delete();	//并不完全覆盖，如果原文件存在会保持原来的,参考:https://www.jb51.net/article/125808.htm
		ZipTemp temp = getZipTemp(zipPath, password);
		ZipFile zipFile = temp.zipFile;
		ZipParameters zipParameters = temp.zipParameters;
		
		zipFile.createSplitZipFileFromFolder(folder, zipParameters, true, limitSize);
		return zipFile;
	}
	
	/**
	 * 解压压缩包到文件夹(默认解压到压缩包目录/压缩包文件名的路径下)
	 * @param zipFile
	 * @param destinationDir 目标路径
	 * @throws ZipException
	 */
	public static void unZip(ZipFile zipFile, String destinationDir) throws ZipException {
		if(destinationDir == null) {
			File tempFile = zipFile.getFile();
			String folderName = RegexUtils.find(tempFile.getName(), "^(.+?)(\\.(?:[^\\.])*)?$","$1");
			destinationDir = StringUtils.concat(tempFile.getParent(), File.separator, folderName, File.separator);
		}
		
		zipFile.extractAll(destinationDir);
	}
	
	/**
	 * 解压到目标文件夹
	 * @param file 文件
	 * @param destinationDir 目标文件夹路径
	 * @param password 密码
	 * @throws ZipException
	 */
	public static void unZip(File file, String destinationDir, String password) throws ZipException {
		ZipFile zipFile = getZipTemp(file.getAbsolutePath(), password).zipFile;
		unZip(zipFile, destinationDir);
	}
	
	/**
	 * 解压压缩包中的单个文件
	 * @param zipFile
	 * @param absPath 文件相对于压缩包根目录的位置,比如a/b/c.txt
	 * @param destinationPath 解压的目标路径
	 * @throws ZipException
	 */
	public static void unZipSigleFile(ZipFile zipFile, String absPath, String destinationPath) throws ZipException {
		if(destinationPath == null) {
			destinationPath = StringUtils.concat(zipFile.getFile().getParent(), File.separator, new File(absPath).getName());
		}
		File targetFile = new File(destinationPath);
		String targetDir = targetFile.getParent()+File.separator;
		String newFileName = targetFile.getName();
		
		zipFile.extractFile(absPath, targetDir, newFileName);
	}
	
	/**
	 * 根据目标路径和密码获取数据库实例
	 * @param zipPath
	 * @param password 可以为空
	 * @return
	 */
	public ZipFile getZipFile(String zipPath, String password) {
		if(StringUtils.isEmpty(password)) {	//不带密码
			return new ZipFile(zipPath);
		} else {	//带密码
			return new ZipFile(zipPath, password.toCharArray());
		}
	}
	
	/**
	 * 从压缩包中删除一个文件
	 * @param zipFile
	 * @param absPath 文件相对于压缩包根目录的位置,比如a/b/c.txt
	 * @throws ZipException
	 */
	public static void removeFile(ZipFile zipFile, String absPath) throws ZipException {
		Optional<FileHeader> fileHeader = getFileHeader(zipFile, absPath);
		if(fileHeader.isPresent()) {
			zipFile.removeFile(fileHeader.get());
		}
	}
	
	/**
	 * 获取压缩包目标文件的相对位置信息
	 * @param zipFile
	 * @param absPath 文件相对于压缩包根目录的位置,比如a/b/c.txt
	 * @return
	 * @throws ZipException
	 */
	public static Optional<FileHeader> getFileHeader(ZipFile zipFile, String absPath) throws ZipException {
		return Optional.ofNullable(zipFile.getFileHeader(absPath));
	}
	
	/**
	 * 获取压缩包对应文件的输入流
	 * @param zipFile
	 * @param absPath 文件相对于压缩包根目录的位置,比如a/b/c.txt
	 * @return 
	 * @return
	 * @throws ZipException 
	 */
	public static Optional<ZipInputStream> getInputStream(ZipFile zipFile, String absPath) throws ZipException {
		Optional<FileHeader> fileHeader = getFileHeader(zipFile, absPath);
		if(!fileHeader.isPresent()) {
			Optional.empty();
		}
		return Optional.of(zipFile.getInputStream(fileHeader.get()));
	}
	
	/**
	 * 判断压缩包中是否存在某个文件
	 * @param zipFile
	 * @param absPath 文件相对于压缩包根目录的位置,比如a/b/c.txt
	 * @return
	 * @throws ZipException 
	 */
	public static boolean isExisted(ZipFile zipFile, String absPath) throws ZipException {
		Optional<FileHeader> fileHeader = getFileHeader(zipFile, absPath);
		return fileHeader.isPresent();
	}
	
	/**
	 * 获取压缩包中的文件列表
	 * @param zipFile
	 * @return
	 * @throws ZipException
	 */
	public static List<FileHeader> getAllFileHeader(ZipFile zipFile) throws ZipException {
		return zipFile.getFileHeaders();
	}
	
	
	/**
	 * 根据配置信息获取ZipFile实例和ZipParameters实例的打包
	 * @param zipPath
	 * @param password
	 * @return
	 */
	private static ZipTemp getZipTemp(String zipPath, String password) {
		ZipParameters zipParameters = new ZipParameters();
		ZipFile zipFile = null;
		
		zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
		zipParameters.setCompressionLevel(CompressionLevel.NORMAL);
		
		if(StringUtils.isEmpty(password)) {
			zipFile = new ZipFile(zipPath);
		} else {
			zipParameters.setEncryptFiles(true);
			zipParameters.setEncryptionMethod(EncryptionMethod.AES);
			// Below line is optional. AES 256 is used by default. You can override it to use AES 128. AES 192 is supported only for extracting.
			zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256); 
			zipFile = new ZipFile(zipPath, password.toCharArray());
		}
		
		return new ZipTemp(zipFile, zipParameters);
	}
	
	private static class ZipTemp {
		ZipFile zipFile;
		ZipParameters zipParameters;
		public ZipTemp(ZipFile zipFile, ZipParameters zipParameters) {
			super();
			this.zipFile = zipFile;
			this.zipParameters = zipParameters;
		}
	}

}
