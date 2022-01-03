package tool.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * ConcurrentHashMap计算缓存工具类<br><br/>
 * 解决HashMap的并发度问题，但是它仍有缺陷——同一段时间，某个key的任务会重复执行，浪费资源。
 * 工程中Redis的实现应该和这个类似
 * @param <T>
 * @param <R>
 */
public class ConcurrentComputeCache<T, R> implements ComputeCache<T, R> {
    private final Function<T, R> func;
    private final Map<T, R> cache = new ConcurrentHashMap<>();

    public ConcurrentComputeCache(Function<T, R> func) {
        this.func = func;
    }

    @Override
    public R compute(T t) {
        R result = cache.get(t);
        if (result == null) {
            result = func.apply(t);
            cache.put(t, result);
        }
        return result;
    }
}
