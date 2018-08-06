package com.ag777.util.lang.cmd;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.ag777.util.lang.RegexUtils;
import com.ag777.util.lang.StringUtils;

/**
 * ping命令的执行工具类
 * 
 * @author ag777
 * @version create on 2018年08月06日,last modify at 2018年08月06日
 */
public class PingUtils {

	private PingUtils(){}
	
	/**
	 * 执行ping命令返回平均用时平均用时ms
	 * <p>
	 * 命令: ping -n [count] [host]
	 * 正在 Ping 192.168.161.162 具有 32 字节的数据:
		来自 192.168.161.162 的回复: 字节=32 时间<1ms TTL=63
		来自 192.168.161.162 的回复: 字节=32 时间<1ms TTL=63
		来自 192.168.161.162 的回复: 字节=32 时间<1ms TTL=63
		来自 192.168.161.162 的回复: 字节=32 时间<1ms TTL=63
		
		192.168.161.162 的 Ping 统计信息:
		    数据包: 已发送 = 4，已接收 = 4，丢失 = 0 (0% 丢失)，
		往返行程的估计时间(以毫秒为单位):
		    最短 = 0ms，最长 = 0ms，平均 = 0ms
		==================
		...上部分省略
		请求超时。

		192.168.167.99 的 Ping 统计信息:
    	数据包: 已发送 = 5，已接收 = 0，丢失 = 5 (100% 丢失)，
    	==================
		Ping 请求找不到主机 ping。请检查该名称，然后重试。
		</p>
		
	 * @param host 可以是ip
	 * @param count 测试次数
	 * @return
	 * @throws IOException
	 */
	public static Optional<Double> pingWin(String host, int count) throws IOException {
		String cmd = StringUtils.concat("ping -n ", count, ' ', host);
		List<String> lines = CmdUtils.getInstance().readLines(cmd, null);
		String lastLine = lines.get(lines.size()-1);
		if(lastLine.contains("100% 丢失")) {
			return Optional.empty();
		} else {
			Double average =  RegexUtils.findDouble(lastLine, "平均\\s=\\s(\\d+(\\.\\d+)?)ms", "$1");
			return Optional.ofNullable(average);
		}
		
	}
}
