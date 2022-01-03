package excutor.renderpage;

import java.util.Random;

public class ImageInfo {
    private int index;

    public ImageInfo(int index) {
        this.index = index;
    }

    public ImageData downloadImage() {
        try {
            Random random = new Random();
            int time = 1000 * ((index % 3) + 1);
            System.out.println("download img " + this + " need " + time + "ms ...");
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        System.out.println("img " + index + " downloaded");
        return new ImageData(this);
    }

    @Override
    public String toString() {
        return "ImageInfo{" +
                "index=" + index +
                '}';
    }
}
