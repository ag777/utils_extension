package com.ag777.util.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.model.Charsets;

/**
 * lnk文件读取修改工具类(windows)
 * 
 * @author ag777
 * @version create on 2019年07月16日,last modify at 2019年07月16日
 */
public class LnkUtils {
	
	private final static int INDEX_FLAGS = 0x14;
	private final static int INDEX_FILE_ATTS_OFFSET = 0x18;
	private final static int INDEX_SHELL_OFFSET = 0x14;
	
	private String sign;	// 总是为0000004CH，相当于字符"L"，用于标识是否是个有效的.lnk文件。 
	private byte[] guid;	// GUID，标识.lnk的唯一标识符，不排除以后MS对该字段有所修改。
	
	private byte flags;
	private boolean isDir;	//原始文件是否是文件夹
	private String linkPath;	//原始文件(夹)路径
	
	/**
	 * 加载解析lnk文件
	 * @param filePath
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static LnkUtils read(String filePath) throws FileNotFoundException, IOException {
		return new LnkUtils().init(filePath);
	}
	
	public String getSign() {
		return sign;
	}
	
	public byte[] getGuid() {
		return guid;
	}
	
	/**
	 * @return 原始文件是否是文件夹
	 */
	public boolean isDir() {
		return isDir;
	}
	
	/**
	 * @return 原始文件(夹)路径
	 */
	public String getLinkPath() {
		return linkPath;
	}
	
	private LnkUtils() {
	}
	
	public LnkUtils init(String filePath) throws FileNotFoundException, IOException {
		byte[] bytes = FileUtils.readBytes(filePath);
		this.sign = new String(bytes, 0, 1);
		
		flags = bytes[INDEX_FLAGS]; 
		
		// get the file attributes byte 
        byte fileatts = bytes[INDEX_FILE_ATTS_OFFSET]; 
        byte is_dir_mask = (byte)0x10; 
        if((fileatts & is_dir_mask) > 0) { 
        	this.isDir = true; 
        } else { 
        	this.isDir = false; 
        }
        
        // if the shell settings are present, skip them 
        int shell_len = 0; 
        if((flags & 0x1) > 0) { 
            // the plus 2 accounts for the length marker itself 
            shell_len = bytes2short(bytes,INDEX_SHELL_OFFSET) + 2; 
        }
        
        // get to the file settings 
        int file_start = 0x4c + shell_len; 
		
        // get to the file settings 
        int fileStart = 0x4c + shell_len; 

        // get the local volume and local system values 
        int local_sys_off = bytes[fileStart+0x10] + file_start; 
        this.linkPath = getNullDelimitedString(bytes,local_sys_off); 
	    
        return this;
	}
	
	
	private static String getNullDelimitedString(byte[] bytes, int off) { 
        int len = 0; 
        // count bytes until the null character (0) 
        while(true) { 
            if(bytes[off+len] == 0) { 
                break; 
            } 
            len++; 
        } 
        return new String(bytes,off,len,Charsets.GBK);
    } 
	
	private static short bytes2short(byte[] bytes, int off) {
		return (short) ((bytes[off] & 0xff) | (bytes[off + 1] & 0xff) << 8);
	}

}
