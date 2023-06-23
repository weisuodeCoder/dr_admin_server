import com.darhan.utils.IdWorker;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class IdWorkerTest {
    private int len = 196606;
    @Test
    public void testIdWorker() {
        long[] arr = new long[len];
        for(int i = 0; i<len; i++) {
            long id = IdWorker.nextId();
            arr[i] = id;
        }
        Set<Long> set = new HashSet<Long>();
        boolean state = true;
        for(int i=0; i<len; i++) {
            if(!set.add(arr[i])) {
                state = false;
                System.out.println("重复元素为："+arr[i]);
            }
        }
        if(state) {
            System.out.println("添加"+len+"条数据没有重复元素！");
        }
    }
}
