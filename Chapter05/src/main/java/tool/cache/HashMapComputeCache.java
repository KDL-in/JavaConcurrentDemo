package tool.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * HashMap计算缓存工具类，利用synchronized对cache的读写进行加锁
 * <br/>
 * 本缓存的问题在于多个请求对cache的访问是同步的，它的并发度很差，只是计算了一次会有缓存——然而不同key可以并发进行计算
 *
 * @param <T> 输入
 * @param <R> 输出
 */
public class HashMapComputeCache<T, R> implements ComputeCache<T, R> {
    private final Function<T, R> func;
    private final Map<T, R> cache = new HashMap<>();

    public HashMapComputeCache(Function<T, R> func) {
        this.func = func;
    }

    @Override
    public synchronized R compute(T t) {
        R result = cache.get(t);
        if (result == null) {
            result = func.apply(t);
            cache.put(t, result);
        }
        return result;
    }
}
