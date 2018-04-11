package com.ag777.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.exception.Assert;

/**
 * 有关压缩和解压的工具类
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>javacsv.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年04月10日,last modify at 2018年04月11日
 */
public class CompressUtils {

	private final static int BUFFER = 1024;
	
	/*============压缩==================*/
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
			File file = tar(files, tarPath);
			return gz(file.getPath(), gzPath);
		} catch(RuntimeException|IOException ex) {
			throw ex;
		} finally {
			FileUtils.delete(tarPath);	//删除临时的tar文件
		}
	}
	
	/**
	 * 将文件打成tar包
	 * <p>
	 *  windows下文件中文乱码没关系,用工具打也会,
	 *  将getTarArchiveOutputStream设置成gbk编码就不会乱码但是会产生如下问题
	 *  ①只能在解压时也采用这种编码才能正常解压
	 *  ②linux下文件名乱码(ls命令会得到一堆乱码)
	 * </p>
	 * 
	 * @param filePath
	 * @param targetPath 目标tar文件路径
	 * @return
	 * @throws IOException
	 */
	public static File tar(String filePath, String tarPath) throws IOException {
		return tar(new String[]{filePath}, tarPath);
	}
	
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
	public static File tar(File[] files, String tarPath) throws IOException {
		Assert.notEmpty(files, "至少选择压缩一个文件");
		for (File f : files) {
			Assert.notExisted(f, "需要压缩的文件不存在:" + f.getAbsolutePath());
		}
		TarArchiveOutputStream tos = null;
		try {
			tos = getTarArchiveOutputStream(tarPath);
			for (File fi : files) {
				if (fi.isDirectory()) {
					archiveDir(fi, tos, fi.getName());
				} else {
					archiveHandle(tos, fi, null);
				}
			}
			return new File(tarPath);
		} catch (Exception ex) {
			FileUtils.delete(tarPath);
			throw ex;
		} finally {
			IOUtils.close(tos);
		}
	}
	
	/**
	 * 将一系列文件打包成tar包
	 * <p>
	 *  windows下文件中文乱码没关系,用工具打也会,
	 *  将getTarArchiveOutputStream设置成gbk编码就不会乱码但是会产生如下问题
	 *  ①只能在解压时也采用这种编码才能正常解压
	 *  ②linux下文件名乱码(ls命令会得到一堆乱码)
	 * </p>
	 * 
	 * @param paths    文件路径列表(绝对路径)
	 * @param tarPath 目标tar文件路径
	 * @return
	 * @throws IOException
	 */
	public static File tar(String[] paths, String tarPath) throws IOException {
		Assert.notEmpty(paths, "至少选择压缩一个文件");
		File[] files = new File[paths.length];
		for(int i=0;i<paths.length;i++) {
			files[i] = new File(paths[i]);
		}
		return tar(files, tarPath);
	}
    
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
			byte data[] = new byte[BUFFER];
			while ((count = is.read(data, 0, BUFFER)) != -1) {
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
	
	/*=================解压====================*/
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
	public static void unTargz(String gzPath, String targetPath) throws Exception {
		Assert.notExisted(gzPath, "需要被解压的文件不存在:"+gzPath);
		String tarPath = StringUtils.concat(new File(gzPath).getParent(), File.separator, StringUtils.uuid(), ".tar.temp");
		try {
			unGz(gzPath, tarPath);
			unTar(tarPath, targetPath);
		} catch(Exception ex) {
			throw ex;
		} finally {
			FileUtils.delete(tarPath);
		}
	}
	
    /**
     * 解tar包
     * 
     * @param tarPath
     * @param targetPath
     */
	public static void unTar(String tarPath, String targetPath) {
		Assert.notExisted(tarPath, "需要解压的文件不存在:" + tarPath);
		BufferedOutputStream bos = null;
		TarArchiveInputStream tais = null;
		try {
			File tarFile = new File(tarPath);
			Assert.notExisted(tarFile, "压缩包不存在:" + tarPath);
			tais = new TarArchiveInputStream(new FileInputStream(tarFile));
			TarArchiveEntry entry = null;
			while ((entry = tais.getNextTarEntry()) != null) {

				// 文件
				String dir = targetPath + File.separator + entry.getName();

				File dirFile = new File(dir);

				// 文件检查
				FileUtils.makeDir(dirFile.getParent(), true);

				if (entry.isDirectory()) {
					dirFile.mkdirs();
				} else {
					bos = new BufferedOutputStream(new FileOutputStream(dirFile));

					int count;
					byte data[] = new byte[BUFFER];
					while ((count = tais.read(data, 0, BUFFER)) != -1) {
						bos.write(data, 0, count);
					}

				}

			}
		} catch (Exception ex) {

		} finally {
			IOUtils.close(tais, bos);
		}
	}
    
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
			bos = new BufferedOutputStream(FileUtils.getOutputStream(tarPath));  
	        gcis = new GzipCompressorInputStream(
	        		new BufferedInputStream(FileUtils.getInputStream(gzPath)));
	
	        byte[] buffer = new byte[BUFFER];
	        int read = -1;
	        while((read = gcis.read(buffer)) != -1){
	            bos.write(buffer, 0, read);
	        }
		} catch(Exception ex) {
			throw ex;
		} finally {
			IOUtils.close(gcis, bos);
		}

	}
	
    /*=================内部方法==============*/
	private static TarArchiveOutputStream getTarArchiveOutputStream(String tarPath) throws FileNotFoundException {
		TarArchiveOutputStream stream = new TarArchiveOutputStream(FileUtils.getOutputStream(tarPath));
		stream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
		return stream;
	}
	
	/**
     * 递归处理，准备好路径
     * @param file
     * @param tos
     * @param base 
     * @throws IOException
     * @author yutao
     * @date 2017年5月27日下午1:48:40
     */
	private static void archiveDir(File file, TarArchiveOutputStream tos, String basePath) throws IOException {
		/* 先创建文件夹，防止空文件夹不计入tar包 */
		TarArchiveEntry tEntry = new TarArchiveEntry(basePath + File.separator);
		tos.putArchiveEntry(tEntry);
		/* 将文件夹的子文件列表放入tar包 */
		File[] listFiles = file.listFiles();
		for (File fi : listFiles) {
			if (fi.isDirectory()) {
				archiveDir(fi, tos, basePath + File.separator + fi.getName());
			} else {
				archiveHandle(tos, fi, basePath);
			}
		}
	}

    /**
     * 具体归档处理（文件）
     * @param tos
     * @param fi
     * @param base
     * @throws IOException
     * @author yutao
     * @date 2017年5月27日下午1:48:56
     */
    private static void archiveHandle(TarArchiveOutputStream tos, File fi, String basePath) throws IOException {
    	String path = fi.getName();
    	if(basePath != null) {
    		path = basePath + File.separator + path;
    	}
        TarArchiveEntry tEntry = new TarArchiveEntry(path);
        tEntry.setSize(fi.length());

        tos.putArchiveEntry(tEntry);

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fi));

        byte[] buffer = new byte[BUFFER];
        int read = -1;
        while((read = bis.read(buffer)) != -1){
            tos.write(buffer, 0 , read);
        }
        bis.close();
        tos.closeArchiveEntry();//这里必须写，否则会失败
    }
	
	
    public static void main(String[] args) throws Exception {
    	File gzFile = targz(new String[]{"f:\\a\\"}, "f:\\a.tar.gz");
    	System.out.println(gzFile.getPath());
    	unTargz(gzFile.getPath(), "e:\\");
	}
}
