package com.ag777.util.lang.thread;

import com.ag777.util.lang.RandomUtils;
import com.ag777.util.lang.SystemUtils;
import com.ag777.util.lang.exception.model.ValidateException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 任务并发执行工具类,底层采用ForkJoinPool实现
 * @author ag777 <837915770@vip.qq.com>
 * @version 2024/9/23 下午4:51
 */
public class ForkJoinTaskUtils {

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
     * 执行批量处理函数，根据提供的列表和作业对象进行处理
     *
     * @param list 要处理的列表
     * @param job 执行的具体作业对象，指定了处理逻辑
     * @return 处理后的结果列表
     * @throws InterruptedException 如果作业被中断
     * @throws ValidateException 如果作业校验失败
     */
    public static <T, R> List<R> batch(List<T> list, Job<T, R> job) throws InterruptedException, ValidateException {
        // 使用系统默认的CPU核心数来执行批量处理
        return batch(list, job, SystemUtils.cpuCores());
    }

    /**
     * 批处理函数，使用 ForkJoinPool 并行处理列表中的元素
     *
     * @param list            需要被处理的列表
     * @param job             表示需要执行的任务，从T到R的转换
     * @param concurrencyCount 并行处理的线程数量
     * @return                处理后的结果列表
     * @throws InterruptedException   如果任务被中断
     * @throws ValidateException     如果任务执行中出现验证异常
     */
    public static <T, R> List<R> batch(List<T> list, Job<T, R> job, int concurrencyCount) throws InterruptedException, ValidateException {
        // 创建一个 ForkJoinPool 实例，用于并行处理任务
        ForkJoinPool forkJoinPool = new ForkJoinPool(concurrencyCount);
        try {
            // 提交任务到 ForkJoinPool，使用 BatchTask 封装需要并行处理的任务
            return forkJoinPool.invoke(new BatchTask<>(list, job, 0, list.size()));
        } catch (CompletionException e) {
            // 处理 CompletionException，获取原始异常
            Throwable t = unpackCompletionException(e);
            // 根据异常类型进行特殊处理
            if (t instanceof InterruptedException) {
                throw (InterruptedException)t;
            } else if (t instanceof ValidateException) {
                throw (ValidateException)t;
            } else {
                // 抛出新的 ValidateException，携带原始异常信息
                throw new ValidateException(t.getMessage(), t);
            }
        } finally {
            // 关闭并关闭 ForkJoinPool，确保资源释放
            forkJoinPool.shutdownNow();
            ThreadPoolUtils.waitFor(forkJoinPool);
        }
    }


    /**
     * 解包CompletionException以找到根本原因
     *
     * 递归地处理CompletionException，返回最深层的原始异常
     * 当遇到CompletionException时，尝试解包并处理其内部原因
     * 如果最深层的原因不是CompletionException，则直接返回该原因
     *
     * @param e 要解包的CompletionException或包含CompletionException的异常
     * @return 最深层的异常，如果不存在嵌套，则返回原始异常
     */
    private static Throwable unpackCompletionException(Throwable e) {
        // 获取异常的原因
        Throwable cause = e.getCause();

        // 如果原因是CompletionException，则递归调用解包方法
        if (cause instanceof CompletionException) {
            return unpackCompletionException(cause);
        }

        // 如果原因不是CompletionException，则返回当前原因
        return cause;
    }


    // 定义一个 RecursiveTask 来处理列表
    private static class BatchTask<T, R> extends RecursiveTask<List<R>> {
        private final List<T> list;
        private final Job<T, R> job;
        private final int start;
        private final int end;
        private static final int THRESHOLD = 1; // 阈值，用于决定是否继续拆分任务

        public BatchTask(List<T> list, Job<T, R> job, int start, int end) {
            this.list = list;
            this.job = job;
            this.start = start;
            this.end = end;
        }

        @Override
        protected List<R> compute() {
            int size = end - start;
            if (size <= THRESHOLD) {
                // 如果任务足够小，直接计算
                List<R> results = new ArrayList<>(size);
                for (int i = start; i < end; i++) {
                    try {
                        results.add(job.handleItem(list.get(i), i));
                    } catch (Throwable e) {
                        throw new CompletionException(e); // 将异常包装为CompletionException
                    }
                }
                return results;
            } else {
                // 否则，将任务拆分为两个子任务
                int mid = (start + end) / 2;
                BatchTask<T, R> leftTask = new BatchTask<>(list, job, start, mid);
                BatchTask<T, R> rightTask = new BatchTask<>(list, job, mid, end);

                // 异步执行子任务
                leftTask.fork();
                rightTask.fork();

                // 合并子任务的结果
                List<R> leftResult = leftTask.join();
                List<R> rightResult = rightTask.join();

                leftResult.addAll(rightResult);
                return leftResult;
            }
        }
    }

    @FunctionalInterface
    public interface Job<T, R> {
        R handleItem(T item, int index) throws ValidateException, InterruptedException;
    }
}
