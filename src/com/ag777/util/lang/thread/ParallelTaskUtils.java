package com.ag777.util.lang.thread;

import com.ag777.util.lang.RandomUtils;
import com.ag777.util.lang.SystemUtils;
import com.ag777.util.lang.exception.model.ValidateException;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 任务并发执行工具类,底层采用CompletionService实现
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/9/23 下午3:48
 */
public class ParallelTaskUtils {
    public static void main(String[] args) throws ValidateException, InterruptedException {
        long startTime = System.currentTimeMillis();
        List<Integer> result = batch(
                IntStream.range(0, 100).boxed().collect(Collectors.toList()),
                (item, index) -> {
                    TimeUnit.MILLISECONDS.sleep(RandomUtils.rInt(100));
                    return item * 2;
                }, 2);
        // 输出结果
        System.out.println("Result: " + result);
        System.out.println(System.currentTimeMillis()-startTime);
    }

    /**
     * 执行批量处理任务
     *
     * 此方法接收一个可迭代对象和一个任务对象，将可迭代对象中的数据分批处理，并返回处理结果
     * 它利用系统的CPU核心数来并行处理任务，以提高处理效率
     *
     * @param iterable 一个可迭代对象，包含需要批量处理的数据
     * @param job 一个任务对象，定义了如何处理每个数据项
     * @return 返回一个列表，包含所有处理结果
     * @throws InterruptedException 如果任务被中断
     * @throws ValidateException 如果数据验证失败
     */
    public static <T, R> List<R> batch(Iterable<T> iterable, Job<T, R> job) throws InterruptedException, ValidateException {
        // 调用重载的batch方法，使用系统CPU核心数作为批处理的线程数
        return batch(iterable, job, SystemUtils.cpuCores());
    }

    /**
     * 对可迭代数据集进行批处理
     *
     * @param iterable 可迭代的数据集
     * @param job 执行的任务接口
     * @param concurrencyCount 并发数
     * @param <T> 输入类型
     * @param <R> 输出类型
     * @return 处理后的结果列表
     * @throws InterruptedException 如果任务被中断
     * @throws ValidateException 如果验证失败
     */
    public static <T, R> List<R> batch(Iterable<T> iterable, Job<T, R> job, int concurrencyCount) throws InterruptedException, ValidateException {
        // 检查数据集是否为空
        if (iterable instanceof Collection) {
            if (((Collection<T>) iterable).isEmpty()) {
                return Collections.emptyList();
            }
        }
        // 根据任务初始化线程池辅助类
        CompletionServiceHelper<R, Integer> pool = batchForTask(iterable, job, concurrencyCount);
        try {
            // 获取任务总数
            int taskCount = pool.getTaskCount();
            // 用于存储所有任务的结果
            Object[] results = new Object[taskCount];
            // 循环执行每个任务
            for (int i = 0; i < taskCount; i++) {
                // 从线程池中获取已完成的任务
                CompletionServiceHelper.Task<R, Integer> task = pool.take();
                try {
                    // 获取任务索引和结果
                    results[task.getData()] = task.get();
                } catch (ExecutionException e) {
                    // 获取异常原因
                    Throwable t = e.getCause();
                    // 如果原因是中断异常，则重新抛出
                    if (t instanceof InterruptedException) {
                        throw (InterruptedException) t;
                    } else if (t instanceof ValidateException) {
                        // 如果原因是验证异常，则重新抛出
                        throw (ValidateException) t;
                    } else {
                        // 否则抛出新的验证异常
                        throw new ValidateException(t.getMessage(), t);
                    }
                }
            }
            // 将Object[]转换为R[]
            return Arrays.stream(results).map(i -> (R) i).collect(Collectors.toList());
        } finally {
            // 关闭线程池并等待所有任务完成
            pool.shutdownNow();
            pool.waitForDispose();
            pool.dispose();
        }
    }

    /**
     * 创建一个CompletionServiceHelper辅助类，用于批量处理任务
     * 此方法用于将任务分割成一批一批地处理，每批处理完成后才继续下一批
     *
     * @param iterable 要处理的任务集合
     * @param job 任务的处理逻辑
     * @param concurrencyCount 并发数
     * @param <T> 任务的类型
     * @param <R> 任务处理结果的类型
     * @return 返回一个CompletionServiceHelper实例，用于管理任务的批量处理
     * @throws InterruptedException 当操作被中断时抛出此异常
     * @throws ValidateException 当任务的处理逻辑中存在校验错误时抛出此异常
     */
    public static <T, R> CompletionServiceHelper<R, Integer> batchForTask(Iterable<T> iterable, Job<T, R> job, int concurrencyCount) throws InterruptedException, ValidateException {
        // 创建一个迭代器，用于遍历任务集合
        Iterator<T> iterator = iterable.iterator();
        // 创建一个CompletionServiceHelper实例，用于管理任务的批量处理
        CompletionServiceHelper<R, Integer> pool = new CompletionServiceHelper<>(concurrencyCount);
        try {
            // 初始化任务索引
            int index = 0;
            // 遍历任务集合
            while (iterator.hasNext()) {
                // 获取下一个任务
                T item = iterator.next();
                // 为当前任务的索引创建一个局部变量，确保在lambda表达式中可以正常使用
                final int currentIndex = index;
                // 提交任务到CompletionServiceHelper，并使用lambda表达式定义任务的处理逻辑
                pool.submit(()-> job.handleItem(item, currentIndex), currentIndex);
                // 更新任务索引
                index++;
            }
            // 关闭CompletionServiceHelper，不再接受新任务
            pool.shutdown();
            // 返回CompletionServiceHelper实例
            return pool;
        } catch (Throwable t) {
            // 如果在处理过程中发生异常，关闭CompletionServiceHelper并释放资源
            pool.shutdownNow();
            pool.waitForDispose();
            pool.dispose();
            // 重新抛出异常
            throw t;
        }
    }



    @FunctionalInterface
    public interface Job<T, R> {
        R handleItem(T item, int index) throws ValidateException, InterruptedException;
    }
}
