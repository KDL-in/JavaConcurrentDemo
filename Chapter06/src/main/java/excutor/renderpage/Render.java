package excutor.renderpage;

public class Render {
    public void renderImage(ImageData data) {
        System.out.println("render img " + data + " ...");
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public ImageInfo[] scanForImageInfo(CharSequence source) {
        ImageInfo[] imageInfos = new ImageInfo[10];
        for (int i = 0; i < 10; i++) {
            imageInfos[i] = new ImageInfo(i);
        }
        return imageInfos;
    }

    public void renderText(CharSequence source) {
        try {
            System.out.println("rendering text " + source + " ...");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        System.out.println("render text fin");
    }
}
