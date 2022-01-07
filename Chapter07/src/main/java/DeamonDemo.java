import java.time.LocalTime;

public class DeamonDemo {

    public static void main(String[] args) throws InterruptedException {
        TimerThread timerThread = new TimerThread();
        timerThread.setDaemon(true);
        timerThread.start();
        Thread.sleep(3000);

        System.out.println("end");

    }
}

class TimerThread extends Thread {
    @Override
    public void run() {
        while (true) {
            System.out.println(LocalTime.now());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
