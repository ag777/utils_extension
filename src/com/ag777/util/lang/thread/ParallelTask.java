package com.ag777.util.lang.thread;

import com.ag777.util.lang.RandomUtils;
import com.ag777.util.lang.model.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 并发任务工具类
 * @param <T> 单个任务的返回类型
 * @param <D> 单个任务绑定的数据类型
 * @param <R> 最终的结果返回类型
 * @param <V> 处理单个任务结果返回的类型
 * @param <E> 抛出异常类型
 * @author ag777 <837915770@vip.qq.com>
 * @version 2023/8/16 08:57
 */
public abstract class ParallelTask<T, D, R, V, E extends Exception> implements Callable<R> {

    private final ExecutorService pool;
    private final CompletionServiceHelper<T, D> vsh;
    /** 是否添加完爬虫任务 */
    private final AtomicBoolean taskAddFinished;
    /** 一次性处理的数据数量 */
    private final int batchSize;
    /** 批处理超时,当有要处理的数据时，如果超过一定时间没有得到新的数据，则先执行该次批处理, 为0则不超时 */
    private final long batchTimeout;

    /** 返回值 */
    private volatile R result;

    /**
     *
     * @param executeService 线程池
     * @param initResult 返回智初始值
     * @param batchSize 一次性处理的数据数量
     */
    public ParallelTask(ExecutorService executeService, R initResult, int batchSize) {
        this(executeService, initResult, batchSize, 0);
    }

    /**
     *
     * @param executeService 线程池
     * @param initResult 返回智初始值
     * @param batchSize 一次性处理的数据数量
     * @param batchTimeout 批处理超时,当有要处理的数据时，如果超过一定时间没有得到新的数据，则先执行该次批处理, 为0则不超时
     */
    public ParallelTask(ExecutorService executeService, R initResult, int batchSize, long batchTimeout) {
        if (batchSize <= 0) {
            batchSize = 1;
        }
        this.pool = executeService;
        vsh = new CompletionServiceHelper<>(executeService);
        taskAddFinished = new AtomicBoolean(false);
        this.result = initResult;
        this.batchSize = batchSize;
        this.batchTimeout = batchTimeout;
    }

    public CompletionServiceHelper<T, D> getCompletionServiceHelper() {
        return vsh;
    }

    public ExecutorService getExecutorService() {
        return pool;
    }

    public void shutdown() {
        pool.shutdown();
    }

    public void shutdownNow() {
        pool.shutdownNow();
    }

    public void waitFor() throws InterruptedException {
        ThreadPoolUtils.waitFor(pool);
    }

    /**
     *
     * @param doAddTask 添加任务
     * @param onAddTaskErr 添加任务线程异常处理
     * @return 处理结果的异步任务
     */
    public Future<R> runInPool(DoAddTask<T, D, R, V, E> doAddTask, onErr<E> onAddTaskErr) {
        ThreadPoolExecutor taskAddPool = getTaskAddPool();
        return runInPool(doAddTask, onAddTaskErr, taskAddPool);
    }


    public Future<R> runInPool(DoAddTask<T, D, R, V, E> doAddTask, onErr<E> onAddTaskErr, ExecutorService pool) {
        return pool.submit(()->{
            Future<Void> taskAdd = pool.submit(() -> {
                try {
                    doAddTask.accept(this);
                }  finally {
                    taskAddFinished.set(true);
                }
                return null;
            });
            Future<R> taskHandle = pool.submit(this);
            try {
                taskAdd.get();
            }  catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof InterruptedException) {
                    throw (InterruptedException)cause;
                }
                onAddTaskErr.handleErr(e);
            }
            pool.shutdown();
            try {
                return taskHandle.get();
            }  catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof InterruptedException) {
                    throw (InterruptedException)cause;
                }
                throw new RuntimeException(e);
            } finally {
                vsh.cancel(true);
            }
        });
    }

    public R execInPool(DoAddTask<T, D, R, V, E> doAddTask, onErr<E> onAddTaskErr, onErr<E> onUnCaughtErr) throws InterruptedException, E {
        ThreadPoolExecutor taskAddPool = getTaskAddPool();
        return execInPool(doAddTask, onAddTaskErr, onUnCaughtErr, taskAddPool);
    }
    public R execInPool(DoAddTask<T, D, R, V, E> doAddTask, onErr<E> onAddTaskErr, onErr<E> onUnCaughtErr, ExecutorService pool) throws InterruptedException, E {
        Future<R> task = runInPool(doAddTask, onAddTaskErr, pool);
        try {
            return task.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof InterruptedException) {
                throw (InterruptedException)cause;
            }
            onUnCaughtErr.handleErr(cause);
            return null;
        }
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
                if (batchTimeout > 0 &&  !list.isEmpty()) {
                    task = vsh.poll(batchTimeout, TimeUnit.MILLISECONDS);
                    if (task == null) {
                        // 超时还没有任务完成就先处理已经得到的数据
                        handleOnce(list);
                        continue;
                    }
                } else if (taskAddFinished.get()) {
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
                    onErr0(e, task.getData());
                    continue;
                }
                if (list.size() == batchSize) {
                    handleOnce(list);
                }
            }
        } catch (Exception e) {
            throw e;
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
        pairs.clear();
        if (taskAddFinished.get()) {
            // 可以做日志输出
            whenCountDown(vsh.getTaskCount());
        }
    }

    /**
     * 当任务当前任务就已经添加完毕，触发一批任务结果的处理后调用盖方法
     * @param taskCount 剩余任务数量
     */
    protected void whenCountDown(int taskCount) {
    }

    protected void onErr0(ExecutionException e, D data) throws InterruptedException, E {
        Throwable cause = e.getCause();
        if (cause instanceof InterruptedException) {
            throw (InterruptedException)cause;
        }
        this.onErr(cause, data);
    }

    private ThreadPoolExecutor getTaskAddPool() {
        AtomicInteger threadNum = new AtomicInteger(0);
        return new ThreadPoolExecutor(
                3,
                3,
                1,
                TimeUnit.MILLISECONDS,
                //第二个参数代表公平锁
                new ArrayBlockingQueue<>(1, false),
                r -> new Thread(r, "parallel-task-main="+threadNum.addAndGet(1))
        );
    }

    /**
     * 批处理数据
     * @param items 批量的任务结果及其绑定数据
     * @return 批量处理的结果
     * @throws InterruptedException 中断
     * @throws E 异常
     */
    public abstract V handleItems(List<Pair<T,D>> items) throws InterruptedException, E;

    /**
     * @param t 异常
     * @param bindData 绑定数据
     * @throws InterruptedException 中断
     * @throws E 异常的类型
     */
    public abstract void onErr(Throwable t, D bindData) throws InterruptedException, E;

    /**
     * 合并历史结果和本次批处理结果
     * @param result 历史结果
     * @param newVal 本次批处理结果
     * @return 结果，最后会作为任务返回
     */
    public abstract R merge(R result, V newVal);



    @FunctionalInterface
    public interface DoAddTask<T, D, R, V, E extends Exception> {
        void accept(ParallelTask<T, D, R, V, E> task) throws InterruptedException, E;
    }

    @FunctionalInterface
    public interface onErr<E extends Exception> {
        void handleErr(Throwable t) throws InterruptedException, E;
    }

    public static void main(String[] args) throws Throwable {
        /* 示例:
        单线程: 随机(0,1000)，等待该随机数的时间(毫秒),
        有五个单线程,等待执行完毕。如果两次等待的线程小于100则一起输出，否则分开输出
        */
        ParallelTask<String, Void, Void, Void, RuntimeException> task = new ParallelTask<String, Void, Void, Void, RuntimeException>(
                Executors.newFixedThreadPool(3),
                null,
                10,
                100
        ) {
            @Override
            public Void handleItems(List<Pair<String, Void>> items) throws InterruptedException, RuntimeException {
                System.out.println(
                        items.stream().map(item->item.first).collect(Collectors.joining(","))
                );
                return null;
            }

            @Override
            public void onErr(Throwable t, Void bindData) throws InterruptedException, RuntimeException {
                throw new RuntimeException("sth wrong", t);
            }

            @Override
            public Void merge(Void result, Void newVal) {
                return null;
            }
        };

        Future<Void> futureTask = task.runInPool(
                (t) -> {
                    for (int i = 0; i < 5; i++) {
                        t.add(() -> {
                            int l = RandomUtils.rInt(1000);
                            TimeUnit.MILLISECONDS.sleep(l);
                            return l + "";
                        }, null);
                    }
                    // 不再添加任务，线程池里的线程完成就销毁
                    task.shutdown();
                }, e -> {
                    throw new RuntimeException("添加任务异常", e);
                }
        );
        try {
            futureTask.get();
        } catch (InterruptedException e) {
            System.err.println("中断");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof InterruptedException) {
                System.err.println("中断");
            }
            throw e.getCause();
        }
    }
}
