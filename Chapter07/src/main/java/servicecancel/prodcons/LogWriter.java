package servicecancel.prodcons;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 中断基于线程的服务-中断生产者消费者服务
 * <p>
 * 案例：
 * 日志记录服务，拥有固定的消费线程，处理日志。多个调用者线程生产日志（生产者）
 * <p>
 * 需要小心编写写中断服务。
 * 有时候不仅是中断线程，正如本例，中断生产者-消费者模型，这是一种方式。
 * 基本理念：中断标记之后，停止生产，消费完成允许中断
 */
public class LogWriter {
    private final BlockingQueue<String> queue;
    private final LoggerThread logger;
    private volatile boolean shutdown = false;

    public LogWriter() {
        this.queue = new ArrayBlockingQueue<>(10);
        this.logger = new LoggerThread();
    }

    public void start() {
        logger.start();
    }

    /**
     * 1. 中断消费线程。
     * 但是生产效率大于消费效率，会阻塞在队列上导致无法中断。
     */
    public void stop() {
        synchronized (this) {
            shutdown = true;
        }
        logger.interrupt();
    }

    /**
     * 2. 增加shutdown标志，关闭后不再生产，避免生产者阻塞。
     * 但是shutdown的判断修改为竞态条件，需要同步
     *
     * @param msg
     * @throws InterruptedException
     */
    public void log(String msg) throws InterruptedException {
        synchronized (this) {
            if (shutdown) {
                System.out.println("has shutdown");
                return;
            }
        }
        queue.put(msg);
    }

    private class LoggerThread extends Thread {
        /**
         * 3. 判断关闭且队列消费完毕，退出。同样注意竞态条件
         */
        public void run() {

            while (true) {
                synchronized (LogWriter.this) {
                    if (shutdown && queue.size() == 0) {
                        break;
                    }
                }
                consume();
            }

        }

        private void consume() {
            try {
                System.out.println(queue.take());
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // 允许忽略中断标志，上一层已经有中断策略
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LogWriter logWriter = new LogWriter();
        logWriter.start();
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int n = 10;
                    try {
                        while (n-- > 0) {
                            logWriter.log(finalI + " : " + "log " + n);
                        }
                    } catch (InterruptedException e) {
                        // 允许线程结束
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        Thread.sleep(1000);
        // logWriter.stop();
    }
}