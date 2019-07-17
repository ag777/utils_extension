package com.ag777.util.file;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.ag777.util.file.FileUtils;
import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.model.Charsets;

/**
 * lnk文件读取修改工具类(windows)
 * 
 * {@link https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-shllink/16cb4ca1-9339-4d0c-a68d-bf1d6cc0f943}
 * {@link https://pope12389.iteye.com/blog/1333585}
 * @author ag777
 * @version create on 2019年07月16日,last modify at 2019年07月16日
 */
public class LnkUtils {
	
	private final static int INDEX_FLAGS = 0x14;
	private final static int INDEX_FILE_ATTS_OFFSET = 0x18;
	private final static int INDEX_SHELL_OFFSET = 0x4c;
	
	private final static int INDEX_FILE_INFO_PATH = 0x10;
	
	private byte[] bytes;	//文件内容
	
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
		this.bytes = FileUtils.readBytes(filePath);
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
		
        // get to the file settings 这里跳过整个Shell Item Id List结构体
        int fileStart = INDEX_SHELL_OFFSET + shell_len; 

        // get the local volume and local system values 
        int local_sys_off = bytes[fileStart+INDEX_FILE_INFO_PATH] + fileStart; 
        this.linkPath = getNullDelimitedString(bytes,local_sys_off); 
	    
        return this;
	}
	
//	public LnkUtils setFilePath(String filePath) {
//		byte fileatts = bytes[INDEX_FILE_ATTS_OFFSET]; 
//		if(FileNioUtils.isDirectory(Paths.get(filePath))) {
//			isDir = true;
//	        bytes[INDEX_FILE_ATTS_OFFSET] = (byte)(fileatts | 0x10);	//00010000 改变第四位为1
//		} else {
//			isDir = false;
//			bytes[INDEX_FILE_ATTS_OFFSET] = (byte)(fileatts & 0xef);	//fff0ffff 改变第四位为0
//		}
//		filePath = new File(filePath).getAbsolutePath();
//		
//		int shell_len = 0; 
//        if((flags & 0x1) > 0) { 
//            shell_len = bytes2short(bytes,INDEX_SHELL_OFFSET) + 2;
//            
//            //这里跳过整个Shell Item Id List结构体，到达File location info
//            int fileStart = INDEX_SHELL_OFFSET + shell_len; 
//            
//            int local_sys_off = bytes[fileStart+0x10] + fileStart;
//            List<Byte> byteList = ListUtils.ofList4Array(bytes);
//            for(int i=local_sys_off;i<byteList.size();i++) {
//            	while(byteList.get(i) != 0) {
//            		byteList.remove(i--);
//            	}
//            }
//            byteList.addAll(local_sys_off, ListUtils.ofList4Array(filePath.getBytes()));
//            bytes = new byte[byteList.size()];
//            for (int i = 0; i < bytes.length; i++) {
//            	bytes[i]=byteList.get(i);
//			}
//            this.linkPath = filePath;
//        }
//		return this;
//	}
	
	public void save(String filePath) throws IOException {
		BufferedOutputStream out = FileUtils.getBufferedOutputStream(filePath);
		try {
			out.write(bytes);
		} finally {
			IOUtils.close(out);
		}
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
	
//	private static byte[] short2byte(short s) {
//		ByteBuffer bbuf = ByteBuffer.allocate(2);
//		bbuf.putShort(s);
//		byte[] data = bbuf.array();
//		//倒置数组
//		byte temp = data[0];
//		data[0] = data[1];
//		data[1] = temp;
//		return data;
//	}

}
