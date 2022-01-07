package taskcancel.noninterruptable;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * 自定义线程中的不可中断的阻塞，中断策略
 */
public class ReaderThread extends Thread {
    private final Socket socket;
    private final InputStream in;

    public ReaderThread(Socket socket) throws IOException {
        this.socket = socket;
        this.in = socket.getInputStream();
    }

    /**
     * 其实就是 socket的中断方法 + 线程的中断方法
     */
    public void interrupt() {
        try {
            socket.close();
        } catch (IOException ignored) {
        } finally {
            super.interrupt();
        }
    }

    public void run() {
        try {
            byte[] buf = new byte[1024];
            while (true) {
                int count = in.read(buf);
                if (count < 0)
                    break;
                else if (count > 0)
                    processBuffer(buf, count);
            }
        } catch (IOException e) { /* Allow thread to exit */ }
    }

    private void processBuffer(byte[] buf, int count) {

    }
}