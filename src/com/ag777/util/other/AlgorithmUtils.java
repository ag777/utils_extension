package com.ag777.util.other;

import java.util.Iterator;
import java.util.Map;

/**
 * 算法辅助类
 * 
 * @author ag777
 * @version create on 2017年06月21日,last modify at 2017年06月21日
 */
public class AlgorithmUtils {

	
	public static String statisticsString(Map<Integer, Integer> map, int maxNum) {
		int system = getSystem(maxNum);	//进制
		int pow = 1;
		switch(system) {
			case 8:
				pow = 3;
				break;
			case 16:
				pow = 4;
				break;
			default:
				pow = 1;
				break;
		}
		
		long num = 0;
		Iterator<Integer> itor = map.keySet().iterator();
		while(itor.hasNext()) {
			int index = itor.next();
			int value = map.get(index);
			num = num | (value<<index*pow);
		}
		
		switch(system) {
			case 2:
				return Long.toBinaryString(num);
			case 8:
				return Long.toOctalString(num);
			case 16:
				return Long.toHexString(num);
		}
		
		return null;
	}
	
	private static Integer getSystem(int num) {
		if(num<= 2) {
			return 2;
		} else if(num <=8) {
			return 8;
		} else if(num <=16) {
			return 16;
		}
		return null;
	}
	
}
