package taskcancel;

import java.util.concurrent.*;

/**
 * 定时中断任务，由于任务执行时间是不确定的，设定期望执行时间，应该是一种常见的操作，以下提供一种最佳实践
 */
public class TimedInterruptDemo {
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

    /**
     * 利用FutureTask的get方法，设定时间
     * @param runnable
     * @param timeout
     * @param timeUnit
     */
    public void futureTimedRun(Runnable runnable, long timeout, TimeUnit timeUnit) {
        Future<?> future = executor.submit(runnable);
        try {
            future.get(timeout, timeUnit);
        } catch (InterruptedException e) {
            // 处理中断
            e.printStackTrace();
        } catch (ExecutionException e) {
            // 处理执行时异常
            e.printStackTrace();
        } catch (TimeoutException e) {
            // 超时处理
            e.printStackTrace();
        } finally {
            // 取消不再需要的任务
            future.cancel(true);
        }
    }
    /**
     * 利用ScheduledExecutor延迟执行，取消任务<br><br/>
     * 问题：
     * <ol>
     *     <li>违背原则，不知道线程的中断策略，就不能操控它。你不知道它是否响应中断，是否继续执行</li>
     *     <li>如果任务执行异常，无法捕获。你也不知道它究竟是执行异常还是超时异常，还是中断</li>
     * </ol>
     * @param runnable
     * @param timeout
     * @param timeUnit
     */
    public void timedRun(Runnable runnable, long timeout, TimeUnit timeUnit) {
        // 启动线程
        final Thread thread = new Thread(runnable);
        thread.start();
        // 定时取消任务
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                thread.interrupt();
            }
        }, timeout, timeUnit);
    }


    public static void main(String[] args) {
        new TimedInterruptDemo().futureTimedRun(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    // 允许线程退出，不需要恢复
                    e.printStackTrace();
                }
            }
        }, 3000, TimeUnit.MILLISECONDS);

    }
}
