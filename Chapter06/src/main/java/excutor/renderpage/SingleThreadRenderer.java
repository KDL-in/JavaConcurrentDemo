package excutor.renderpage;

import java.util.ArrayList;
import java.util.List;

/**
 * 串行方式渲染页面，任务包括：
 * <ol>
 *     <li>渲染文字信息</li>
 *     <li>下载一系列图片并渲染</li>
 * </ol>
 */
public class SingleThreadRenderer {
    private Render render = new Render();

    void renderPage(CharSequence source) {
        render.renderText(source);
        List<ImageData> imageData = new ArrayList<ImageData>();
        for (ImageInfo imageInfo : render.scanForImageInfo(source))
            imageData.add(imageInfo.downloadImage());
        for (ImageData data : imageData)
            render.renderImage(data);
    }



    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new SingleThreadRenderer().renderPage("www.baidu.com");
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        // 20279
    }
}