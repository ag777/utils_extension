package com.ag777.util.file.compress.base;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;

import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.exception.Assert;

/**
 * 有关压缩和解压的工具基类,commons-compress二次封装
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>commons-compress-1.16.1.jar</li>
 * </ul>
 * </p>
 * 
 * @author ag777
 * @version create on 2018年04月12日,last modify at 2018年04月12日
 */
public abstract class BaseCompressUtils {
	public final static int BUFFER = 1024;
	
	/**
	 * 打包压缩包
	 * @param files
	 * @param packagePath
	 * @return
	 * @throws IOException
	 */
	protected File compress(File[] files, String packagePath) throws IOException {
		Assert.notEmpty(files, "至少选择压缩一个文件");
		for (File f : files) {
			Assert.notExisted(f, "需要压缩的文件不存在:" + f.getAbsolutePath());
		}
		ArchiveOutputStream tos = null;
		try {
			tos = getArchiveOutputStream(packagePath);
			for (File fi : files) {
				if (fi.isDirectory()) {
					addDir(tos, fi, null);
				} else {
					addFile(tos, fi, null);
				}
			}
			return new File(packagePath);
		} catch (Exception ex) {
			FileUtils.delete(packagePath);
			throw ex;
		} finally {
			IOUtils.close(tos);
		}
	}
    
	/**
	 * 解压压缩包
	 * @param packagePath
	 * @param targetPath
	 * @throws IOException
	 */
	protected void decompress(String packagePath, String targetPath) throws IOException {
		Assert.notExisted(packagePath, "需要解压的文件不存在:" + packagePath);
		ArchiveInputStream tais = null;
		try {
			File tarFile = new File(packagePath);
			Assert.notExisted(tarFile, "压缩包不存在:" + packagePath);
			tais = getArchiveInputStream(FileUtils.getInputStream(tarFile));
			ArchiveEntry entry = null;
			while ((entry = tais.getNextEntry()) != null) {
				
				// 文件
				String dir = targetPath + File.separator + entry.getName();

				File dirFile = new File(dir);

				// 文件检查
//				FileUtils.makeDir(dirFile.getParent(), true);

				if (entry.isDirectory()) {
					dirFile.mkdirs();
				} else {
					BufferedOutputStream bos = null;
					try {	//必须在这层包try-catch并及时关闭输出流，不然会导致输出空文件，而且解压后也无法删除文件(被占用)
						bos = new BufferedOutputStream(new FileOutputStream(dirFile));
	
						int count;
						byte data[] = new byte[BUFFER];
						while ((count = tais.read(data, 0, BUFFER)) != -1) {
							bos.write(data, 0, count);
						}
					} catch(Exception ex) {
						throw ex;
					} finally {
						IOUtils.close(bos);
					}
				}

			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			IOUtils.close(tais);
		}
	}
	
	/**
	 * 归档目录
	 * @param os
	 * @param file
	 * @param baseDir
	 * @throws IOException
	 */
	protected void addDir(ArchiveOutputStream os, File file, String baseDir) throws IOException {
		/* 先创建文件夹，防止空文件夹不计入tar包 */
		String dir = file.getName()+File.separator;
    	if(baseDir != null) {
    		dir = baseDir + dir;
    	}
    	ArchiveEntry dirEntry = getArchiveEntry(dir, file, false);
    	if(dirEntry != null) {
    		os.putArchiveEntry(dirEntry);
    	}
		
		
		/* 将文件夹的子文件列表放入tar包 */
		File[] listFiles = file.listFiles();
		for (File f : listFiles) {
			if (f.isDirectory()) {
				addDir(os, f, dir);
			} else {
				addFile(os, f, dir);
			}
		}
	}
	
	/**
	 * 归档单个文件
	 * @param os
	 * @param file
	 * @throws IOException
	 */
    protected void addFile(ArchiveOutputStream os, File file, String baseDir) throws IOException {
    	BufferedInputStream bis = null;
    	try {
    		String path = file.getName();
        	if(baseDir != null) {
        		path = baseDir + path;
        	}
        	ArchiveEntry entry = getArchiveEntry(path, file, true);	//压缩包内对象
        	if(entry != null) {
        		os.putArchiveEntry(entry);
        	}
	    	bis = FileUtils.getBufferedInputStream(file);
	    	
	
	        byte[] buffer = new byte[BUFFER];
	        int read = -1;
	        while((read = bis.read(buffer)) != -1){
	            os.write(buffer, 0 , read);
	        }
    	} catch(Exception ex) {
    		
    	} finally {
    		IOUtils.close(bis);
    		os.closeArchiveEntry();//这里必须写，否则会失败
    	}
        
    }
    
    /**
     * 获取压缩包内每一项的对象,如果返回null则当前文件不计入压缩包
     * @param filePath 压缩包内路径
     * @param file		源文件
     * @param isFile 	如果是当前项是目录则为0，否则为文件大小
     * @return
     */
    public abstract ArchiveEntry getArchiveEntry(String filePath, File file, boolean isFile);
    
    public abstract ArchiveOutputStream getArchiveOutputStream(String filePath) throws FileNotFoundException, IOException;
    public abstract ArchiveInputStream getArchiveInputStream(InputStream is) throws FileNotFoundException;
}
