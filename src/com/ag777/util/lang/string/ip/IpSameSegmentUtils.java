package com.ag777.util.lang.string.ip;

/**
 * ip地址验证辅助类
 * <p>
 *  判断ip是否处于同一网段
 *  参考资料:https://blog.csdn.net/youyou_yo/article/details/46770801
 * </p>
 * 
 * @author ag777
 * @version create on 2018年05月09日,last modify at 2018年05月09日
 */
public class IpSameSegmentUtils {
	private IpSameSegmentUtils() {}
	
	/**
	 * 根据两个ip及其掩码判断是否属于同一网段
	 * <p>
	 * 同一网段的子网掩码一定相同 -by百度知道
	 * </p>
	 * @param ip1
	 * @param mask1
	 * @param ip2
	 * @param mask2
	 * @return
	 */
	public static boolean isSameSegment(String ip1, String mask1, String ip2, String mask2) {
		if(!mask1.equals(mask2)) {
			return false;
		}
		return isSameSegment(ip1, ip2, mask1);
	}
	
	/**
	 * 判断两个ip(同一掩码)是否属于同一网段
	 * @param ip1
	 * @param ip2
	 * @param mask
	 * @return
	 */
	private static boolean isSameSegment(String ip1,String ip2, String mask) {  
		return isSameSegment(ip1, ip2, getIpV4Value(mask));
	}
	
	/** 
     * 比较两个ip地址是否在同一个网段中，如果两个都是合法地址，两个都是非法地址时，可以正常比较； 
     * 如果有其一不是合法地址则返回false； 
     * 注意此处的ip地址指的是如“192.168.1.1”地址 
     * @return 
     */  
     private static boolean isSameSegment(String ip1,String ip2, int mask) {  
          int ipValue1 = getIpV4Value(ip1);  
          int ipValue2 = getIpV4Value(ip2);  
          return (mask & ipValue1) == (mask & ipValue2);  
     }  
	
	private static int getIpV4Value(String ipOrMask) {  
         byte[] addr = getIpV4Bytes(ipOrMask);  
         int address1  = addr[3] & 0xFF;  
         address1 |= ((addr[2] << 8) & 0xFF00);  
         address1 |= ((addr[1] << 16) & 0xFF0000);  
         address1 |= ((addr[0] << 24) & 0xFF000000);  
         return address1;  
    }
	
	private static byte[] getIpV4Bytes(String ipOrMask)  {  
         try {  
              String[] addrs = ipOrMask.split("\\.");  
              int length = addrs.length;  
              byte[] addr = new byte[length];  
              for (int index = 0; index < length; index++)  
              {  
                   addr[index] = (byte) (Integer.parseInt(addrs[index]) & 0xff);  
              }  
              return addr;  
         }  
         catch (Exception e)  {  
         }  
         return new byte[4];  
    }
	public static void main(String[] args) {
		boolean same = isSameSegment("192.168.162.100", "255.255.255.0", "192.168.162.12", "255.255.255.0");
		boolean diff = isSameSegment("192.168.162.100", "255.255.255.0", "192.168.162.12", "255.255.254.0");
		System.out.println(same+"||"+diff);
	}
}
