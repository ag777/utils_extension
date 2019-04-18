package com.ag777.util.lang.convert;

import com.ag777.util.lang.calculate.CalculateHelper;

/**
 * 单位换算
 * 
 * @author ag777
 * @version create on 2017年07月19日,last modify at 2018年07月19日
 */
public class UnitConvertUtils {

	/*
	 * 为什么需要参数system?是由于硬盘系统进制有的是1000有的是1024
	 */
	public enum Size {
		B {
			@Override
			public double toBits(double src, long system) {
				return divide(src, 8, 1).getDouble();
			}
			@Override
			public double toKB(double src, long system) {
				return divide(src, system, 1).getDouble();
			}
			@Override
			public double toMB(double src, long system) {
				return divide(src, system, 2).getDouble();
			}
			@Override
			public double toGB(double src, long system) {
				return divide(src, system, 3).getDouble();
			}
			@Override
			public double toTB(double src, long system) {
				return divide(src, system, 4).getDouble();
			}
		},
		b {
			@Override
			public double toByte(double src, long system) {
				return multiply(src, 8, 1).getDouble();
			}
			@Override
			public double toKB(double src, long system) {
				return divide(src, system, 1).multiply(8).getDouble();
			}
			@Override
			public double toMB(double src, long system) {
				return divide(src, system, 2).multiply(8).getDouble();
			}
			@Override
			public double toGB(double src, long system) {
				return divide(src, system, 3).multiply(8).getDouble();
			}
			@Override
			public double toTB(double src, long system) {
				return divide(src, system, 4).multiply(8).getDouble();
			}
		},
		KB {
			@Override
			public double toByte(double src, long system) {
				return multiply(src, system, 1).getDouble();
			}
			@Override
			public double toBits(double src, long system) {
				return multiply(src, system, 1).multiply(8).getDouble();
			}
			@Override
			public double toMB(double src, long system) {
				return divide(src, system, 1).getDouble();
			}
			@Override
			public double toGB(double src, long system) {
				return divide(src, system, 2).getDouble();
			}
			@Override
			public double toTB(double src, long system) {
				return divide(src, system, 3).getDouble();
			}
		},
		MB {
			@Override
			public double toByte(double src, long system) {
				return multiply(src, system, 2).getDouble();
			}
			@Override
			public double toBits(double src, long system) {
				return multiply(src, system, 2).multiply(8).getDouble();
			}
			@Override
			public double toKB(double src, long system) {
				return multiply(src, system, 1).getDouble();
			}
			@Override
			public double toGB(double src, long system) {
				return divide(src, system, 1).getDouble();
			}
			@Override
			public double toTB(double src, long system) {
				return divide(src, system, 2).getDouble();
			}
		},
		GB {
			@Override
			public double toByte(double src, long system) {
				return multiply(src, system, 3).getDouble();
			}
			@Override
			public double toBits(double src, long system) {
				return multiply(src, system, 3).multiply(8).getDouble();
			}
			@Override
			public double toKB(double src, long system) {
				return multiply(src, system, 2).getDouble();
			}
			@Override
			public double toMB(double src, long system) {
				return multiply(src, system, 1).getDouble();
			}
			@Override
			public double toTB(double src, long system) {
				return divide(src, system, 1).getDouble();
			}
		},
		TB {
			@Override
			public double toByte(double src, long system) {
				return multiply(src, system, 4).getDouble();
			}
			@Override
			public double toBits(double src, long system) {
				return multiply(src, system, 4).multiply(8).getDouble();
			}
			@Override
			public double toKB(double src, long system) {
				return multiply(src, system, 3).getDouble();
			}
			@Override
			public double toMB(double src, long system) {
				return multiply(src, system, 2).getDouble();
			}
			@Override
			public double toGB(double src, long system) {
				return multiply(src, system, 1).getDouble();
			}
		};
		public double toByte(double src, long system) {
			return src;
		}
		public double toBits(double src, long system) {
			return src;
		}
		public double toKB(double src, long system) {
			return src;
		}
		public double toMB(double src, long system) {
			return src;
		}
		public double toGB(double src, long system) {
			return src;
		}
		public double toTB(double src, long system) {
			return src;
		}
		/**
		 * 除
		 * @param src
		 * @param system 进制,比如1000进制,或者1024进制
		 * @param pow 几次方
		 * @return
		 */
		private static CalculateHelper divide(double src, long system, int pow) {
			return new CalculateHelper(src).divide(Math.pow(system, pow));
		}
		
		/**
		 *  乘
		 * @param src
		 * @param system 进制,比如1000进制,或者1024进制
		 * @param pow 几次方
		 * @return
		 */
		private static CalculateHelper multiply(double src, long system, int pow) {
			return new CalculateHelper(src).multiply(Math.pow(system, pow));
		}
	}
	
	
	private UnitConvertUtils() {}
	
	public static Double convert(double src , long system, Size s1, Size s2) {
		switch(s2) {
			case B:
				return s1.toByte(src, system);
			case b:
				return s1.toBits(src, system);
			case KB:
				return s1.toKB(src, system);
			case MB:
				return  s1.toMB(src, system);
			case GB:
				return s1.toGB(src, system);
			case TB:
				return s1.toTB(src, system);
			default:
				throw new RuntimeException("不支持的计算类型");
		}
	}
	
	
	public static void main(String[] args) {
		long a = 1255705395200l;
		System.out.println(
				convert(a, 1024, Size.B, Size.GB).longValue());
		
	}
}
