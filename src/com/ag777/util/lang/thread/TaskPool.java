package com.ag777.util.lang.thread;

import com.ag777.util.lang.thread.model.InterruptibleExecutor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * 任务池，用于管理多个任务的执行和结果获取
 * @author ag777 <837915770@vip.qq.com>
 * @version 2025/11/28 14:38
 */
public class TaskPool<V, D> implements AutoCloseable{

    private final Map<String, CompletableFuture<V>> taskMap;
    private final Map<CompletableFuture<V>, D> taskInfoMap;
    private final InterruptibleExecutor pool;
    private final BlockingQueue<CompletableFuture<V>> completionQueue;

    /**
     * 构造函数
     * @param pool 线程池
     */
    public TaskPool(ExecutorService pool) {
        this.taskMap = new ConcurrentHashMap<>();
        this.pool = new InterruptibleExecutor(pool);
        completionQueue = new LinkedBlockingQueue<>();
        taskInfoMap = new ConcurrentHashMap<>();
    }

    /**
     * 添加异步任务到线程池执行
     * @param key 任务唯一标识
     * @param callable 要执行的任务
     * @param bindData 与该任务关联的数据，便于后续处理或查询。
     * @return CompletableFuture用于外部控制任务
     */
    public CompletableFuture<V> addTask(String key, Callable<V> callable, D bindData) {
        CompletableFuture<V> task = pool.submit(callable);
        // 任务完成时从map中移除
        task.whenComplete((r, e)-> {
            taskMap.remove(key);
            completionQueue.add(task);
        });
        // 将任务添加到map中
        taskMap.put(key, task);
        taskInfoMap.put(task, bindData);
        return task;
    }

    /**
     * 添加任务完成，关闭线程池
     * @return 当前实例
     */
    public TaskPool<V, D> addEnd() {
        pool.shutdown();
        return this;
    }

    /**
     * 阻塞线程，直至获取到任务
     * @return 任务
     * @throws InterruptedException 等待中断
     */
    public Task<V, D> take() throws InterruptedException {
        CompletableFuture<V> task = completionQueue.take();
        return getTask(task);
    }

    /**
     * 阻塞线程一段时间，如果当前没有任务结束，则返回null
     * @param timeout 等待时间
     * @param timeUnit 等待时间单位
     * @return 任务
     * @throws InterruptedException 等待中断
     */
    public Optional<Task<V, D>> poll(long timeout, TimeUnit timeUnit) throws InterruptedException {
        CompletableFuture<V> task = completionQueue.poll(timeout, timeUnit);
        if (task == null) {
            return Optional.empty();
        }
        return Optional.of(getTask(task));
    }

    /**
     * 获取任务
     * @param key 任务唯一标识
     * @return 任务
     */
    public Optional<Task<V, D>> get(String key) {
        CompletableFuture<V> task = taskMap.get(key);
        if (task == null) {
            return Optional.empty();
        }
        return Optional.of(new Task<>(task, taskInfoMap.get(task)));
    }

    /**
     * 取消任务
     * @param key 任务唯一标识
     * @return 任务
     */
    public Optional<Task<V, D>> cancel(String key) {
        CompletableFuture<V> task = taskMap.get(key);
        if (task != null) {
            task.cancel(true);
            return Optional.of(getTask(task));
        }
        return Optional.empty();
    }

    /**
     * 获取任务
     * @param task 任务
     * @return 任务+绑定数据
     */
    private Task<V, D> getTask(CompletableFuture<V> task) {
        return new Task<>(task, taskInfoMap.remove(task));
    }

    /**
     * 关闭线程池
     * @throws Exception 关闭异常
     */
    @Override
    public void close() throws Exception {
        pool.shutdownNow();
        taskInfoMap.clear();
        taskMap.clear();
    }

    /**
     * 任务+绑定数据
     * @param <V> 任务返回类型
     * @param <D> 绑定数据类型
     */
    public static class Task<V, D> {
        private final CompletableFuture<V> task;
        private final D bindData;
        public Task(CompletableFuture<V> task, D bindData) {
            this.task = task;
            this.bindData = bindData;
        }
        public CompletableFuture<V> getTask() {
            return task;
        }
        public D getBindData() {
            return bindData;
        }
    }

}
