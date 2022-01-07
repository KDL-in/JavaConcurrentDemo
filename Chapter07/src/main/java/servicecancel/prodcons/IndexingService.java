package servicecancel.prodcons;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 中断基于线程的服务-中断生产者消费者服务
 * <p>
 * 案例：
 * 索引建立服务，生产线程不断添加文件路径，消费线程不断对路径建立索引
 * <p>
 * POISON法：
 * 理念是生产者被中断添加POISON标志，并且不会在生产；等待消费者消费完成，消费者看到POISON直接退出。
 * 注意，在n个生产者需要添加ngePOISON标志
 */
public class IndexingService {
    private static final File POISON = new File("");
    private final IndexerThread consumer = new IndexerThread();
    private final CrawlerThread producer = new CrawlerThread();


    private final BlockingQueue<File> queue;

    private final File root;

    public IndexingService() {
        this.queue = new LinkedBlockingQueue<>();
        this.root = new File("root");

    }

    /**
     * 生产线程
     */
    class CrawlerThread extends Thread {
        public void run() {
            try {
                crawl(root);
            } catch (InterruptedException e) { /* fall through */ } finally {
                // 2. 生产者被中断之后，添加POISON
                // 注意，如果有n个生产者，每个生产者应该各自添加POISON
                while (true) {
                    try {
                        System.out.println("中断，添加POISON");
                        queue.put(POISON);
                        break;
                    } catch (InterruptedException e1) { /* retry */ }
                }
            }
        }

        private void crawl(File root) throws InterruptedException {
            /* 添加路径到队列等待建立索引*/
            for (int i = 0; i < 1000; i++) {
                queue.put(new File(i + ""));
                Thread.sleep(100);
            }

        }
    }

    /**
     * 消费者线程
     */
    class IndexerThread extends Thread {
        public void run() {
            try {
                // 3. 消费者如果看到POISON则立即退出
                while (true) {
                    Thread.sleep(150);
                    File file = queue.take();
                    if (file == POISON)
                        break;
                    else
                        indexFile(file);
                }
            } catch (InterruptedException consumed) {
                // 允许中断
            }
        }

        private void indexFile(File file) {
            /* 建立索引 */
            System.out.println("indexing " + file.getAbsolutePath());
        }
    }

    public void start() {
        producer.start();
        consumer.start();
    }

    /**
     * 1. 中断服务，中断生产者
     */
    public void stop() {
        producer.interrupt();
    }

    /**
     * 等待的同步阻塞
     */
    public void awaitTermination() throws InterruptedException {
        consumer.join();
    }

    public static void main(String[] args) {
        IndexingService indexingService = new IndexingService();
        indexingService.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        indexingService.stop();
        try {
            indexingService.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 会等待消费者线程结束
        System.out.println("main");
    }
}