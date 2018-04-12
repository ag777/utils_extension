package com.ag777.util.file.compress;

import java.io.File;
import java.io.IOException;

import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.exception.Assert;

/**
 * 有关解压的工具基类,commons-compress二次封装
 * <p>
 * ①支持对tar.gz和zip文件的压缩解压
 * ②使用前请对参数文件的非空和存在性做校验，否则会抛出异常
 * ③压缩时空文件夹不会被丢弃,放心食用
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>commons-compress-1.16.1.jar</li>
 * </ul>
 * </p>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年04月12日,last modify at 2018年04月12日
 */
public class CompressUtils {

	/*============压缩================*/
	//--tar.gz文件压缩
	/**
	 * 将文件列表打包成tar包(临时),并压缩成tar.gz包
	 * <p>
	 * -中间产生的tar文件在gz的同级目录下，用uuid.tar.temp命名
	 * 事后会被删除
	 * 
	 * -windows下文件中文乱码没关系,用工具打也会,
	 *  将getTarArchiveOutputStream设置成gbk编码就不会乱码但是会产生如下问题
	 *  ①只能在解压时也采用这种编码才能正常解压
	 *  ②linux下文件名乱码(ls命令会得到一堆乱码)
	 * </p>
	 * 
	 * @param paths
	 * @param gzPath
	 * @return
	 * @throws IOException
	 */
	public static File targz(String[] paths, String gzPath) throws IOException {
		Assert.notEmpty(paths, "至少选择压缩一个文件");
		File[] files = new File[paths.length];
		for(int i=0;i<paths.length;i++) {
			files[i] = new File(paths[i]);
		}
		return targz(files, gzPath);
	}
	
	/**
	 * 将文件列表打包成tar包(临时),并压缩成tar.gz包
	 * <p>
	 * -中间产生的tar文件在gz的同级目录下，用uuid.tar.temp命名
	 * 事后会被删除
	 * 
	 * -windows下文件中文乱码没关系,用工具打也会,
	 *  将getTarArchiveOutputStream设置成gbk编码就不会乱码但是会产生如下问题
	 *  ①只能在解压时也采用这种编码才能正常解压
	 *  ②linux下文件名乱码(ls命令会得到一堆乱码)
	 * </p>
	 * 
	 * @param files
	 * @param gzPath
	 * @return
	 * @throws IOException
	 */
	public static File targz(File[] files, String gzPath) throws IOException {
		String tarPath = StringUtils.concat(new File(gzPath).getParent(), File.separator, StringUtils.uuid(), ".tar.temp");
		try {
			File file = TarUtils.getInstance().tar(files, tarPath);
			return GzUtils.gz(file.getPath(), gzPath);
		} catch(RuntimeException|IOException ex) {
			throw ex;
		} finally {
			FileUtils.delete(tarPath);	//删除临时的tar文件
		}
	}
	
	//--zip压缩
	/**
	 * 将文件或文件夹压缩成zip包
	 * 
	 * @param filePath
	 * @param zipPath
	 * @return
	 * @throws IOException
	 */
	public static File zip(String filePath, String zipPath) throws IOException {
		return ZipUtils.getInstance().zip(filePath, zipPath);
	}
	
	/**
	 * 将zip包解压到指定目录
	 * 
	 * @param paths
	 * @param zipPath
	 * @return
	 * @throws IOException
	 */
	public static File zip(String[] paths, String zipPath) throws IOException {
		return ZipUtils.getInstance().zip(paths, zipPath);
	}
	
	/**
	 * 
	 * @param files
	 * @param zipPath
	 * @return
	 * @throws IOException
	 */
	public static File zip(File[] files, String zipPath) throws IOException {
		return ZipUtils.getInstance().zip(files, zipPath);
	}
	
	/*============解压================*/
	//tar.gz解压
	/**
	 * 解压缩tar.gz包为tar包(临时)，解包tar包到指定路径
	 * <p>
	 * 中间产生的tar文件在gz的同级目录下，用uuid.tar.temp命名
	 * 事后会被删除
	 * </p>
	 * 
	 * @param gzPath
	 * @param targetPath
	 * @throws Exception
	 */
	public static void unTargz(String gzPath, String targetPath) throws IOException {
		Assert.notExisted(gzPath, "需要被解压的文件不存在:"+gzPath);
		String tarPath = StringUtils.concat(new File(gzPath).getParent(), File.separator, StringUtils.uuid(), ".tar.temp");
		try {
			GzUtils.unGz(gzPath, tarPath);
			TarUtils.getInstance().unTar(tarPath, targetPath);
		} catch(Exception ex) {
			throw ex;
		} finally {
			FileUtils.delete(tarPath);
		}
	}
	
	/**
	 * 解压zip包到指定路径
	 * 
	 * @param zipPath
	 * @param targetPath
	 * @throws IOException
	 */
	public static void unZip(String zipPath, String targetPath) throws IOException {
		ZipUtils.getInstance().unZip(zipPath, targetPath);
	}
	
	public static void main(String[] args) throws Exception {
		targz(new String[]{"f:\\a\\"}, "f:\\a.tar.gz");
		unTargz("f:\\a.tar.gz", "e:\\");
		
		FileUtils.delete("f:\\a.tar.gz");
//		FileUtils.delete("e:\\a\\");
	}
}
