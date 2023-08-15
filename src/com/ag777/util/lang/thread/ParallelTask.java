package com.ag777.util.lang.thread;

import com.ag777.util.lang.model.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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

    public ParallelTask<T, D, R, V, E> add(Callable<T> callable, D bindData) {
        vsh.submit(callable, bindData);
        return this;
    }

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

    private void handleOnce(List<Pair<T, D>> pairs) throws E {
        // 到达批量处理数量阈值，执行处理
        V newVal = handleItems(pairs);
        synchronized (ParallelTask.class) {
            this.result = merge(this.result, newVal);
        }
    }

    protected void whenCountDown(int taskCount) {
    }

    public abstract V handleItems(List<Pair<T,D>> items) throws E;

    public abstract void onErr(D bindData, Throwable t) throws E;

    public abstract R merge(R result, V newVal);


}
