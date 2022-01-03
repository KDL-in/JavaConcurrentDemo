package tool.cache;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;

/**
 * 模拟耗时Servlet对多个请求进行服务，在真实场景中，应该有多个Servlet对象，但缓存是共享资源
 */
public class Servlet {

    private final ComputeCache<BigInteger, List<BigInteger>> cache = new HashMapComputeCache<>(new Function<BigInteger, List<BigInteger>>() {

        @Override
        public List<BigInteger> apply(BigInteger bigInteger) {
            // 模拟耗时计算，如果走缓存，应该不用经过这里
            long start = System.currentTimeMillis();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            System.out.println("compute " + bigInteger + " cost " + (end - start) + " s");
            return List.of(BigInteger.valueOf(bigInteger.longValue()));
        }
    });

    public  String service(String input) {
        BigInteger i = extractFromReq(input);
        List<BigInteger> result = cache.compute(i);
        return result.get(0).toString();
    }

    private BigInteger extractFromReq(String input) {
        return BigInteger.valueOf(Long.parseLong(input));
    }

    public static void main(String[] args) {
        Servlet servlet = new Servlet();
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    String r = servlet.service(finalI % 5 + "");
                    long end = System.currentTimeMillis();
                    System.out.println("compute " + finalI + "(" + (finalI % 5) + ") result " + r + " cost " + (end - start) + " s");
                }
            }).start();
        }
    }
}
