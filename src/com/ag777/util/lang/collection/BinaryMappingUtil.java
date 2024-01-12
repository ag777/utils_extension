package com.ag777.util.lang.collection;

/**
 * 二进制映射工具类，提供将整数数组映射为单个整数的功能，
 * 以及将单个整数映射回对应的整数数组的功能。
 * 数组中的每个数字代表二进制位的位置，位置从1开始计数。
 * <p>
 * 1=[1]
 * 2=[2]
 * 3=[1,2]
 * 4=[3]
 * 5=[1,3]
 * 6=[2,3]
 * 7=[1,2,3]
 * 8=[4]
 * 9=[1,4]
 * 10=[2,4]
 * 11=[1,2,4]
 * 12=[3,4]
 * 13=[1,3,4]
 * 14=[2,3,4]
 * 15=[1,2,3,4]
 * 16=[5]
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/1/12 14:57
 */
public class BinaryMappingUtil {

    /**
     * 将整数数组映射为一个整数。
     * 数组中的每个元素对应二进制位的位置，该位置上的值为1。数组里的值最大为31
     *
     * @param array 整数数组，数组中的元素代表要设置为1的二进制位的位置。
     * @return 映射后的整数，其二进制表示中对应位置上的位值为1。
     */
    public static int mapToInt(int[] array) {
        int result = 0;
        for (int num : array) {
            result |= 1 << (num - 1);
        }
        return result;
    }

    /**
     * 将整数映射为整数数组。
     * 返回的数组中包含二进制表示中值为1的位的位置。
     *
     * @param number 要映射的整数。最大为31
     * @return 映射后的整数数组，数组中的每个元素代表在整数的二进制表示中值为1的位的位置。
     */
    public static int[] mapToArray(int number) {
        int count = Integer.bitCount(number);
        int[] result = new int[count];
        int index = 0;
        int position = 1;

        while (number > 0) {
            if ((number & 1) == 1) {
                result[index++] = position;
            }
            number >>>= 1;
            position++;
        }

        return result;
    }

    /**
     * 主方法，用于测试映射到数组和映射到整数的功能。
     *
     * @param args 命令行参数，未使用。
     */
    public static void main(String[] args) {
        // 测试映射到数组
        int[] arrayMapped = mapToArray(11);
        System.out.print("Mapped to Array: ");
        for (int num : arrayMapped) {
            System.out.print(num + " ");
        }
        System.out.println();

        // 测试映射到整数
        int intMapped = mapToInt(new int[]{1, 2, 4});
        System.out.println("Mapped to Int: " + intMapped);
    }
}
