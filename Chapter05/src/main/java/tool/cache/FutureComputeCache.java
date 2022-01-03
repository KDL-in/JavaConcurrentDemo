package tool.cache;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * FutureTask计算缓存工具<br><br/>
 * 解决了ConcurrentHashMap的版本的任务重复计算问题，获取相同的任务会阻塞，而不会重复
 * @param <T> 输入
 * @param <R> 输出
 */
public class FutureComputeCache <T, R> implements ComputeCache<T, R>{
    private final Function<T, R> func;
    private final Map<T, Future<R>> cache = new ConcurrentHashMap<>();

    public FutureComputeCache(Function<T, R> func) {
        this.func = func;
    }
    @Override
    public R compute(T t) {
        Future<R> future = cache.get(t);
        if (future == null) {
            FutureTask<R> ft = new FutureTask<>(() -> func.apply(t));
            future = cache.putIfAbsent(t, ft);
            if (future == null) {
                future = ft;
            }
            ((FutureTask<R>)future).run();
        }
        R result = null;
        try {
            result = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }
}
