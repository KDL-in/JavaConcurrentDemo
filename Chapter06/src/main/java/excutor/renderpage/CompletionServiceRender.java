package excutor.renderpage;

import java.util.concurrent.*;

/**
 * ExecutorCompletionService提高并发度，它相当于线程池服务 + 阻塞队列的；<br/>
 * 替代的方式：<br/>
 * 1. 轮询所有future的get，将timeout设 置为0<br/>
 * 2. 每个任务独立执行渲染，可能需要将图像下载和渲染耦合<br/>
 */
public class CompletionServiceRender {
    private final Render render = new Render();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    void renderPage(CharSequence source) {
        ImageInfo[] imageInfos = render.scanForImageInfo(source);
        // 多个CompletionService可以共享同一个excutor
        ExecutorCompletionService<ImageData> executorService = new ExecutorCompletionService(executor);
        for (ImageInfo imageInfo : imageInfos) {
            executorService.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    ImageData imageData = imageInfo.downloadImage();
                    return imageData;
                }
            });
        }
        render.renderText(source);
        try {
            for (int i = 0; i < imageInfos.length; i++) {
                Future<ImageData> future = executorService.take();
                ImageData imageData = future.get();
                render.renderImage(imageData);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new CompletionServiceRender().renderPage("www.baidu.com");
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        // 3097
    }

}
