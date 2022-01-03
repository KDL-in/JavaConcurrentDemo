package excutor.renderpage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * FutureTask将文本渲染和图片下载并行
 */
public class FutureRender {
    private final Render render = new Render();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    void renderPage(CharSequence source) {
        ImageInfo[] imageInfos = render.scanForImageInfo(source);
        Callable<List<ImageData>> task = new Callable<List<ImageData>>() {
            @Override
            public List<ImageData> call() throws Exception {
                List<ImageData> result = new ArrayList<>();
                for (ImageInfo imageInfo : imageInfos) {
                    result.add(imageInfo.downloadImage());
                }
                return result;
            }
        };
        Future<List<ImageData>> future = executorService.submit(task);
        render.renderText(source);
        try {
            List<ImageData> imageDataList = future.get();
            for (ImageData imageData : imageDataList) {
                render.renderImage(imageData);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // 记得取消任务
            future.cancel(true);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new FutureRender().renderPage("www.baidu.com");
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        // 19261
    }
}
