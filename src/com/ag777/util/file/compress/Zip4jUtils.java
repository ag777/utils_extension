package com.ag777.util.file.compress;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.ag777.util.lang.IOUtils;
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
 * <p><a href="https://github.com/srikanth-lingala/zip4j">项目地址</a>
 *
 * @author ag777
 * @version create on 2019年08月01日,last modify at 2024年05月17日
 */
public class Zip4jUtils {
	
	/**
	 * 压缩文件和文件夹
	 * @param fileList 文件列表
	 * @param zipPath 压缩文件路径
	 * @param password 密码
	 * @return  压缩文件
	 * @throws ZipException 压缩异常
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
	 * @param fileList 文件列表
	 * @param zipPath 压缩文件路径
	 * @param limitSize Zip file format specifies a minimum of 65536 bytes (64kb) as a minimum length for split files. Zip4j will throw an exception if anything less than this value is specified.
	 * @param password 密码
	 * @return 压缩文件
	 * @throws ZipException 压缩异常
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
	 * @param folder 文件夹
	 * @param zipPath 压缩文件路径
	 * @param limitSize Zip file format specifies a minimum of 65536 bytes (64kb) as a minimum length for split files. Zip4j will throw an exception if anything less than this value is specified.
	 * @param password 密码
	 * @return 压缩文件
	 * @throws ZipException 压缩异常
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
	 * 尝试使用提供的密码打开指定的Zip文件。
	 *
	 * @param file 指定的Zip文件。
	 * @param password 尝试打开Zip文件的密码，如果为空则不使用密码。
	 * @return 如果密码正确，且Zip文件中至少包含一个非目录项，则返回true；否则返回false。
	 * @throws ZipException 如果处理Zip文件时发生错误。
	 */
	public static boolean testPassword(File file, String password) throws ZipException {
	    // 尝试打开Zip文件，根据是否提供了密码来选择不同的方式
	    ZipFile zipFile;
	    if (StringUtils.isEmpty(password)) {
	        // 没有提供密码，直接打开Zip文件
	        zipFile = new ZipFile(file);
	    } else {
	        // 提供了密码，使用密码打开Zip文件
	        zipFile = new ZipFile(file, password.toCharArray());
	    }

	    // 获取Zip文件的所有文件头信息
	    List<FileHeader> headers = zipFile.getFileHeaders();
	    // 寻找第一个非目录的文件头，用于后续的密码验证
	    Optional<FileHeader> fileHeader = headers.stream().filter(header -> !header.isDirectory()).findAny();

	    if (fileHeader.isPresent()) {
	        try {
	            // 尝试使用找到的文件头和密码获取Zip文件的输入流，并关闭它来验证密码是否正确
	            ZipInputStream in = zipFile.getInputStream(fileHeader.get());
	            IOUtils.close(in);
	            // 如果能够成功获取并关闭输入流，说明密码正确
	            return true;
	        } catch (ZipException e) {
	            // 如果打开输入流时抛出ZipException异常，检查是否是因为密码错误
	            if ("Wrong password!".equalsIgnoreCase(e.getMessage())) {
	                return false;
	            }
	            // 抛出其他类型的ZipException异常
	            throw e;
	        }
	    }
	    // 如果Zip文件中没有找到任何非目录项，也认为密码是有效的
	    return true;
	}
	
	/**
	 * 解压压缩包到文件夹(默认解压到压缩包目录/压缩包文件名的路径下)
	 * @param zipFile 压缩文件
	 * @param destinationDir 目标路径
	 * @throws ZipException 解压异常
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
	 * @throws ZipException 解压异常
	 */
	public static void unZip(File file, String destinationDir, String password) throws ZipException {
		ZipFile zipFile = getZipTemp(file.getAbsolutePath(), password).zipFile;
		unZip(zipFile, destinationDir);
	}

	/**
	 * 从Zip文件中解压单个文件。
	 *
	 * @param zipFile 需要解压的Zip文件对象。
	 * @param absPath 在Zip文件中的绝对路径。
	 * @param destinationPath 解压后文件的目标路径。如果为null，则默认为Zip文件所在目录下同名文件。
	 * @throws ZipException 如果解压过程中发生错误。
	 */
	public static void unZipSigleFile(ZipFile zipFile, String absPath, String destinationPath) throws ZipException {
	    // 如果目标路径未指定，则默认设置为Zip文件所在目录下同名文件
	    if(destinationPath == null) {
	        destinationPath = StringUtils.concat(zipFile.getFile().getParent(), File.separator, new File(absPath).getName());
	    }
	    File targetFile = new File(destinationPath);
	    // 构造解压后文件的存放目录和新文件名
	    String targetDir = targetFile.getParent()+File.separator;
	    String newFileName = targetFile.getName();

	    // 执行解压操作，将指定路径的文件解压到目标目录
	    zipFile.extractFile(absPath, targetDir, newFileName);
	}
	
	/**
	 * 根据目标路径和密码获取数据库实例
	 * @param zipPath 压缩文件路径
	 * @param password 可以为空
	 * @return 压缩文件
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
	 * @param zipFile 压缩文件
	 * @param absPath 文件相对于压缩包根目录的位置,比如a/b/c.txt
	 * @throws ZipException 压缩异常
	 */
	public static void removeFile(ZipFile zipFile, String absPath) throws ZipException {
		Optional<FileHeader> fileHeader = getFileHeader(zipFile, absPath);
		if(fileHeader.isPresent()) {
			zipFile.removeFile(fileHeader.get());
		}
	}
	
	/**
	 * 获取压缩包目标文件的相对位置信息
	 * @param zipFile 压缩文件
	 * @param absPath 文件相对于压缩包根目录的位置,比如a/b/c.txt
	 * @return Optional<FileHeader>
	 * @throws ZipException 解析异常
	 */
	public static Optional<FileHeader> getFileHeader(ZipFile zipFile, String absPath) throws ZipException {
		return Optional.ofNullable(zipFile.getFileHeader(absPath));
	}
	
	/**
	 * 获取压缩包对应文件的输入流
	 * @param zipFile 压缩文件
	 * @param absPath 文件相对于压缩包根目录的位置,比如a/b/c.txt
	 * @return Optional<ZipInputStream>
	 * @throws ZipException 解析异常
	 */
	public static Optional<ZipInputStream> getInputStream(ZipFile zipFile, String absPath) throws ZipException {
		Optional<FileHeader> fileHeader = getFileHeader(zipFile, absPath);
		if(!fileHeader.isPresent()) {
			return Optional.empty();
		}
		return Optional.of(zipFile.getInputStream(fileHeader.get()));
	}
	
	/**
	 * 判断压缩包中是否存在某个文件
	 * @param zipFile 压缩文件
	 * @param absPath 文件相对于压缩包根目录的位置,比如a/b/c.txt
	 * @return 是否存在
	 * @throws ZipException 解析异常
	 */
	public static boolean isExisted(ZipFile zipFile, String absPath) throws ZipException {
		Optional<FileHeader> fileHeader = getFileHeader(zipFile, absPath);
		return fileHeader.isPresent();
	}
	
	/**
	 * 获取压缩包中的文件列表
	 * @param zipFile 压缩文件
	 * @return List<FileHeader>
	 * @throws ZipException 解析异常
	 */
	public static List<FileHeader> getAllFileHeader(ZipFile zipFile) throws ZipException {
		return zipFile.getFileHeaders();
	}
	
	
	/**
	 * 根据配置信息获取ZipFile实例和ZipParameters实例的打包
	 * @param zipPath 压缩文件路径
	 * @param password 密码
	 * @return ZipTemp
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
	
	/**
	 * ZipTemp类用于暂时存储Zip文件和其参数。
	 * 该类主要用于封装对Zip文件的操作过程中需要的文件本身和相关参数。
	 */
	private static class ZipTemp {
	    ZipFile zipFile; // 存储Zip文件对象
	    ZipParameters zipParameters; // 存储Zip文件的参数设置

	    /**
	     * ZipTemp的构造函数，用于初始化ZipTemp对象。
	     *
	     * @param zipFile 用于指定要操作的Zip文件。
	     * @param zipParameters 用于指定操作Zip文件时的参数设置，如压缩级别等。
	     */
	    public ZipTemp(ZipFile zipFile, ZipParameters zipParameters) {
	        super();
	        this.zipFile = zipFile;
	        this.zipParameters = zipParameters;
	    }
	}

}
