package servicecancel;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * 中断基于线程的服务-Executor
 * <p>
 * 将中断的工作委托给ExecutorService是非常简单的，它提供了方便的生命周期API
 */
public class LogService {
    private final ExecutorService exec = newSingleThreadExecutor();


    public void start() {
        // 绑定JVM关闭时的回调线程：关闭钩子，用于关闭日志服务
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try { LogService.this.stop(); }
                catch (InterruptedException ignored) {}
            }
        });
    }

    /**
     * 直接停止，并且阻塞等待，固定时间
     */
    public void stop() throws InterruptedException {
        try {
            exec.shutdown();
            exec.awaitTermination(3000, TimeUnit.MILLISECONDS);
        } finally {
            /* 关闭其他资源，例如io */
        }
    }

    public void log(String msg) {
        try {
            exec.execute(() -> {
                /* 记录日志 */
            });
        } catch (RejectedExecutionException ignored) {
        }
    }

    /**
     * 如果ExecutorService只执行一次，那么可以直接在finally关闭
     * <p>
     * 案例：并发检查多个地址的邮件是否更新，有则设置更新标志
     */
    boolean checkMail(Set<String> hosts, long timeout, TimeUnit unit)
            throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();
        final AtomicBoolean hasNewMail = new AtomicBoolean(false);
        try {
            for (final String host : hosts)
                exec.execute(new Runnable() {
                    public void run() {
                        if (checkMail(host))
                            hasNewMail.set(true);
                    }

                    private boolean checkMail(String host) {
                        /* 检查邮件 */
                        return false;
                    }
                });
        } finally {
            exec.shutdown();
            exec.awaitTermination(timeout, unit);
        }
        return hasNewMail.get();
    }

}