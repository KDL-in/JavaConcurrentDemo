package tool.cache;

/**
 * 计算缓存工具类接口
 * @param <T> 输入
 * @param <R> 输出
 */
public interface ComputeCache<T, R> {
    R compute(T t);
}
