package com.ag777.util.file;

import com.ag777.util.lang.IOUtils;
import com.ag777.util.lang.interf.Disposable;
import com.sun.nio.file.SensitivityWatchEventModifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 文件(夹)监听辅助类
 * 
 * {@link https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-shllink/16cb4ca1-9339-4d0c-a68d-bf1d6cc0f943}
 * {@link https://pope12389.iteye.com/blog/1333585}
 * @author ag777
 * @version create on 2020年08月04日,last modify at 2020年08月04日
 */
public abstract class FileWatchHelper implements AutoCloseable, Disposable {

    private final static String kindCreate = StandardWatchEventKinds.ENTRY_CREATE.toString();
    private final static String kindModify = StandardWatchEventKinds.ENTRY_MODIFY.toString();
    private final static String kindDelete = StandardWatchEventKinds.ENTRY_DELETE.toString();

    private WatchService service;
    private boolean isStop;

    public FileWatchHelper(WatchService service) {
        this.service = service;
        this.isStop = false;
    }

    public static FileWatchHelper getInstance(Consumer<File> onCreate, Consumer<File> onModify, Consumer<File> onDelete) throws IOException {
        WatchService service = FileSystems.getDefault().newWatchService();
        return new FileWatchHelper(service) {

            @Override
            public void onCreate(File file) {
                if(onCreate != null) {
                    onCreate.accept(file);
                }
            }

            @Override
            public void onModify(File file) {
                if(onModify != null) {
                    onModify.accept(file);
                }
            }

            @Override
            public void onDelete(File file) {
                if(onDelete != null) {
                    onDelete.accept(file);
                }
            }
        };
    }

    public void add(String filePath) throws IOException {
        add(new File(filePath),
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_CREATE);
    }

    private void add(File file, WatchEvent.Kind<?>... events) throws IOException {
        Path p = file.toPath();
        if(!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath()+" not found");
        }
        // 2. 注册要监听的事件类型，文件增、删、改
        p.register(
                service,
                events, SensitivityWatchEventModifier.HIGH);
        //注册所有子目录
        if(file.isDirectory()) {
            File[] dirs = file.listFiles(File::isDirectory);
            if(dirs != null) {
                for (File subDir : dirs) {
                    add(subDir, events);
                }
            }

        }

    }

    public void stop() {
        this.isStop = true;
    }

    /**
     * <p>第一级子文件(夹)的变化会触发上一级文件夹的modify事件
     * @throws InterruptedException
     */
    public void start() throws InterruptedException {
        try {
            while (!isStop) {
                // 3. 获取准备好的事件，pool() 立即返回、take() 阻塞
                WatchKey watchKey = service.take();
                //操作路径
                Path path = (Path) watchKey.watchable();
                String basePath = path.toString()+"/";

                // 4. 处理准备好的事件
                List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                for (WatchEvent<?> event : watchEvents) {
                	TimeUnit.MILLISECONDS.sleep(1);
                	
                    Path absPath = (Path) event.context();
                    File file = new File(basePath+absPath.toString());
                    String filePath = file.getAbsolutePath();

                    TimeUnit.MILLISECONDS.sleep(1);

                    WatchEvent.Kind<?> kind = event.kind();
                    if (kindCreate.equals(kind.name())) {
                        //创建
                        if(file.isDirectory()) {
                            try {
                                add(filePath);
                            } catch (IOException e) {
                            }
                        }
                        onCreate(file);
                    } else if (kindModify.equals(kind.name())) {
                        //变更
                        onModify(file);
                    } else if (kindDelete.equals(kind.name())) {
                        //删除
                        onDelete(file);
                    }
                }
                // 5. 重启该线程，因为处理文件可能是一个耗时的过程，因此调用 pool() 时需要阻塞监控器线程
                //监控文件被删除时这个方法返回false，这是不能中断线程，因为可能是子文件夹被删除
                watchKey.reset();
            }
        } catch(InterruptedException ex) {
            throw ex;
        }finally{
            close();
        }
    }

    @Override
    public void dispose() {
        close();
        service = null;
    }

    @Override
    public void close() {
    	this.stop();
        IOUtils.close(service);
    }


    public abstract void onCreate(File file);
    public abstract void onModify(File file);
    public abstract void onDelete(File file);
    
    
    public static void main(String[] args) throws IOException, InterruptedException {
    	/*
         * 示例
         */
    	FileWatchHelper helper = getInstance(
                file->System.out.println("创建:" + file.getAbsolutePath()),
                file->System.out.println("修改:" + file.getAbsolutePath()),
                file->System.out.println("删除:" + file.getAbsolutePath())

        );
        
        Thread t = null;
        try {
        	helper.add("f:/临时/c/");
        	t = new Thread(()->{
        		try {
					helper.start();
				} catch (InterruptedException e) {
					System.out.println("中断");
				}
        	});
        	t.start();
        	TimeUnit.MILLISECONDS.sleep(1000);
        	t.interrupt();
        } finally {
        	helper.dispose();
        	if(t != null) {
        		t.interrupt();
        	}
        }
        TimeUnit.MILLISECONDS.sleep(100000);
        
    }
}
