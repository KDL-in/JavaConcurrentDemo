package excutor.renderpage;

public class ImageData {
    private ImageInfo info;
    public ImageData(ImageInfo imageInfo) {
        info = imageInfo;
    }

    @Override
    public String toString() {
        return "ImageData{" +
                "info=" + info +
                '}';
    }
}
