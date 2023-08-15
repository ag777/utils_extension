package com.ag777.util.lang.thread;

import com.ag777.util.lang.model.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 并发任务工具类
 * @param <T> 单个任务的返回类型
 * @param <D> 单个任务绑定的数据类型
 * @param <R> 最终的结果返回类型
 * @param <V> 处理单个任务结果返回的类型
 * @param <E> 抛出异常类型
 * @author ag777 <837915770@vip.qq.com>
 * @version 2023/8/15 10:48
 */
public abstract class ParallelTask<T, D, R, V, E extends Exception> implements Callable<R> {

    private final ExecutorService pool;
    private final CompletionServiceHelper<T, D> vsh;
    // 是否添加完爬虫任务
    private final AtomicBoolean taskAddFinished;
    // 一次性处理的数据数量
    private final int batchSize;

    private volatile R result;

    /**
     *
     * @param executeService 线程池
     * @param initResult 返回智初始值
     * @param batchSize 一次性处理的数据数量
     */
    public ParallelTask(ExecutorService executeService, R initResult, int batchSize) {
        this.pool = executeService;
        vsh = new CompletionServiceHelper<>(executeService);
        taskAddFinished = new AtomicBoolean(false);
        this.result = initResult;
        this.batchSize = batchSize;
    }

    public CompletionServiceHelper<T, D> getCompletionServiceHelper() {
        return vsh;
    }

    public ExecutorService getExecutorService() {
        return pool;
    }

    /**
     * 将该类放到和其它任务同一个线程池里执行
     * @return 异步任务
     */
    public Future<R> runInPool() {
        return pool.submit(this);
    }

    /**
     *
     * @param callable 任务
     * @param bindData 要绑定的数据
     * @return self
     */
    public ParallelTask<T, D, R, V, E> add(Callable<T> callable, D bindData) {
        vsh.submit(callable, bindData);
        return this;
    }

    /**
     * 添加完所有任务后务必调用该方法，避免call()先入无限等待状态
     * @return self
     */
    public ParallelTask<T, D, R, V, E> finishAdd() {
        taskAddFinished.set(true);
        return this;
    }

    @Override
    public R call() throws InterruptedException, E {
        try {
            waitAndHandleTask();
            return result;
        } catch (Exception e) {
            vsh.cancel(true);
            throw e;
        }
    }

    /**
     * 等待并处理任务结果
     * @throws InterruptedException 中断异常
     * @throws E 自定义异常
     */
    private void waitAndHandleTask() throws InterruptedException, E {
        List<Pair<T,D>> list = new ArrayList<>(batchSize);
        try {
            while (!taskAddFinished.get() || vsh.getTaskCount() > 0) {
                // 爬虫没完成或者任务还有剩余的情况下，结束本次解析
                CompletionServiceHelper.Task<T, D> task;
                if (taskAddFinished.get()) {
                    // 爬虫任务结束，直接阻塞获取任务
                    task = vsh.take();
                } else {
                    // 爬虫任务没结束, 每隔一段时间去获取任务
                    task = vsh.poll(100, TimeUnit.MILLISECONDS);
                    if (task == null) {
                        continue;
                    }
                }
                // 遇到异常就抛出，会丢失其它已完成的任务，需要优化
                try {
                    T item = task.get();
                    list.add(new Pair<>(item, task.getData()));
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof InterruptedException) {
                        throw (InterruptedException)cause;
                    }
                    this.onErr(task.getData(), cause);
                    continue;
                }
                ThreadUtils.checkInterrupted();
                if (list.size() == batchSize) {
                    handleOnce(list);
                    list.clear();
                    if (taskAddFinished.get()) {
                        // 可以做日志输出
                        whenCountDown(vsh.getTaskCount());
                    }
                }
            }
        } finally {
            // 不论怎样，处理剩余已爬取到的结果数据
            if (!list.isEmpty()) {
                handleOnce(list);
            }
        }
    }

    private void handleOnce(List<Pair<T, D>> pairs) throws InterruptedException, E {
        // 到达批量处理数量阈值，执行处理
        V newVal = handleItems(pairs);
        synchronized (ParallelTask.class) {
            this.result = merge(this.result, newVal);
        }
    }

    /**
     * 当任务当前任务就已经添加完毕，触发一批任务结果的处理后调用盖方法
     * @param taskCount 剩余任务数量
     */
    protected void whenCountDown(int taskCount) {
    }

    public abstract V handleItems(List<Pair<T,D>> items) throws InterruptedException, E;

    public abstract void onErr(D bindData, Throwable t) throws InterruptedException, E;

    public abstract R merge(R result, V newVal);


}
