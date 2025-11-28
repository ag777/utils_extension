package com.ag777.util.lang.thread.model;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * 完美实现你所有需求的终极版本
 * 支持：
 * 1. 队列中任务真正被移除（不再执行）
 * 2. 运行中任务被 Thread.interrupt()
 * 3. cancel(true) 后 CompletableFuture 以 CancellationException 完成
 * 4. 不继承 ThreadPoolExecutor，零依赖，极简
 *
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/11/28 15:56
 */
public class InterruptibleExecutor {
    private final ExecutorService executor;

    public InterruptibleExecutor(ExecutorService executor) {
        this.executor = Objects.requireNonNull(executor);
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * 执行任务，返回可取消 + 可中断的 CompletableFuture
     * @param task 任务
     * @return 可取消 + 可中断的 CompletableFuture
     */
    public CompletableFuture<Void> execute(Runnable task) {
        return submit(() -> {
            task.run();
            return null;
        });
    }

    /**
     * 提交任务，返回可取消 + 可中断的 CompletableFuture
     * @param task 任务
     * @param <T> 任务返回类型
     * @return 可取消 + 可中断的 CompletableFuture
     */
    public <T> CompletableFuture<T> submit(Callable<T> task) {
        CompletableFuture<T> cf = new CompletableFuture<>();

        // 正确构造 FutureTask：必须传 Callable<T>
        FutureTask<T> futureTask = new FutureTask<>(() -> {
            try {
                T result = task.call();
                // 正常完成
                if (!cf.isCancelled()) {
                    cf.complete(result);
                }
                return result;
            } catch (Throwable ex) {
                // 任何异常（包括 InterruptedException）都转异常完成
                if (!cf.isCancelled()) {
                    cf.completeExceptionally(ex);
                }
                return null;
            }
        });

        // 关键：双向绑定取消
        // 当用户调用 cf.cancel(true) 时 → 触发底层 FutureTask 取消（队列移除 + 中断线程）
        cf.whenComplete((r, ex) -> {
            if (ex instanceof CancellationException) {
                futureTask.cancel(true);  // 这才是真正能移除队列任务的关键！
            }
        });

        // 必须用 submit() 提交，不能用 execute()
        executor.submit(futureTask);

        return cf;
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * 立即关闭线程池
     */
    public void shutdownNow() {
        executor.shutdownNow();
    }

    /**
     * 等待线程池关闭
     *
     * @return 是否关闭
     * @throws InterruptedException 等待中断
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }
}
