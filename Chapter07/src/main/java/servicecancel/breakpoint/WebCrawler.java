package servicecancel.breakpoint;


import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 案例：爬虫程序断点恢复
 */
public class WebCrawler {
    private volatile TrackingExecutor exec;
    // @GuardedBy("this")
    private final Set<URL> urlsToCrawl = new HashSet<URL>();

    public WebCrawler(String url) {
        urlsToCrawl.add(new URL(url));
    }

    public synchronized void start() {
        exec = new TrackingExecutor(Executors.newCachedThreadPool());
        for (URL url : urlsToCrawl) submitCrawlTask(url);
        urlsToCrawl.clear();
    }

    /**
     * 演示了如何使用支持断点恢复的Executor
     * <p>
     * 1. 调用{@code exec.shutdownNow()}，保存已经提交未开始的任务
     * <p>
     * 2. 调用{@code exec.getCancelledTasks()}，保存正在执行被取消的任务
     */
    public synchronized void stop() throws InterruptedException {
        try {
            saveUncrawled(exec.shutdownNow());
            // 等待5秒，如果exec正常结束则返回true，否则返回false
            if (exec.awaitTermination(5000, TimeUnit.MILLISECONDS))
                saveUncrawled(exec.getCancelledTasks());
        } finally {
            exec = null;
        }
    }

    /*
     * 模拟爬虫程序解析页面，返回更多的页面列表
     *  */
    protected List<URL> processPage(URL url) {
        System.out.println(Thread.currentThread().getName() + " 正在解析 " + url);
        try {
            Thread.sleep(3000);
            System.out.println(Thread.currentThread().getName() + " 解析完成 " + url);
            return List.of(new URL(url + "_1"), new URL(url + "_2"));
        } catch (InterruptedException e) {
            // 恢复中断
            Thread.currentThread().interrupt();
        }
        return Collections.emptyList();
    }

    private void saveUncrawled(List<Runnable> uncrawled) {
        for (Runnable task : uncrawled)
            urlsToCrawl.add(((CrawlTask) task).getPage());
    }

    private void submitCrawlTask(URL u) {
        exec.execute(new CrawlTask(u));
    }

    /*
     * 爬虫任务
     * */
    private class CrawlTask implements Runnable {
        private final URL url;

        public CrawlTask(URL url) {
            this.url = url;
        }

        public void run() {
            for (URL link : processPage(url)) {
                if (Thread.currentThread().isInterrupted())
                    return;
                submitCrawlTask(link);
            }
        }

        public URL getPage() {
            return url;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        WebCrawler webCrawler = new WebCrawler("root");
        webCrawler.start();
        Thread.sleep(8000);
        webCrawler.stop();
        System.out.println("中断");
        Thread.sleep(2000);
        System.out.println("恢复");
        webCrawler.start();
    }
}

class URL {
    private String str;

    public URL(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}