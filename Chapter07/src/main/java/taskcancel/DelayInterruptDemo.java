package taskcancel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 延迟中断恢复
 */
public class DelayInterruptDemo {
    public static void main(String[] args) {
        // endlessLoop();
        // AtomicBoolean替代boolean，因为内部类引用需要final
        final AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(2);

                try {
                    while (true) {
                        try {
                            Integer r = queue.take();
                            return;
                        } catch (InterruptedException e) {
                            // 延迟中断标志
                            interrupted.set(true);
                            System.out.println("catch");
                            e.printStackTrace();
                        }
                    }
                } finally {
                    // 恢复中断
                    if (interrupted.get()){
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        thread.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.interrupt();
    }

    private static void endlessLoop() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(2);
                while (true) {
                    try {
                        Integer r = queue.take();
                        return;
                    } catch (InterruptedException e) {
                        System.out.println("catch");
                        // 恢复中断会造成死循环，应该延迟恢复
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.interrupt();
    }
}
