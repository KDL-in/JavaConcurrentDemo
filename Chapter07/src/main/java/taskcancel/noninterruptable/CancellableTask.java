package taskcancel.noninterruptable;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

/**
 *  自定义任务中的不可中断的阻塞，中断策略。实践中比较少直接使用线程，
 *  而是通过Executor框架+任务的形式操作，这个时候面对不可中断阻塞，中断策略如何编写?
 */
public interface CancellableTask<T> extends Callable<T> {
    void cancel();

    RunnableFuture<T> newTask();
}

// @ThreadSafe
class CancellingExecutor extends ThreadPoolExecutor {

    public CancellingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        if (callable instanceof CancellableTask)
            // callable 和 socket一定会耦合，因为必须关闭socket
            // return ((CancellableTask<T>) callable).newTask();
            // 自定义FutureTask的形式比较清晰，futureTask和socket耦合
            return new MyFutureTask(callable);
        else
            return super.newTaskFor(callable);
    }
}
class MyFutureTask extends FutureTask {
    private Socket socket;
    public MyFutureTask(Callable callable) {
        super(callable);
        // socket和callable的耦合设置
    }

    /**
     * 自定义中断策略
     * @param mayInterruptIfRunning
     * @return
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean r = false;
        try {
            if (socket != null)
                socket.close();
        } catch (IOException ignored) {
        } finally {
            r =  super.cancel(mayInterruptIfRunning);
        }
        return r;
    }
}

/**
 * 这个就比较别扭了，但是和自定futureTask基本是一样的
 * @param <T>
 */
abstract class SocketUsingTask<T>
        implements CancellableTask<T> {
    // @GuardedBy("this")
    private Socket socket;

    protected synchronized void setSocket(Socket s) {
        socket = s;
    }

    public synchronized void cancel() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException ignored) {
        }
    }

    public RunnableFuture<T> newTask() {
        return new FutureTask<T>(this) {
            public boolean cancel(boolean mayInterruptIfRunning) {
                try {
                    SocketUsingTask.this.cancel();
                } finally {
                    return super.cancel(mayInterruptIfRunning);
                }
            }
        };
    }
}