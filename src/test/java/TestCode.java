import org.junit.jupiter.api.Test;
import java.util.Date;
public class TestCode {
    @Test
    public void test() {
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxNjM5OTc2MTc2NTQyNTcyNTQ0IiwiaWF0IjoxNjgwMjg3ODA1LCJleHAiOjE2ODAyODc4NjV9.DzyD3oNP6wMUNscYrUmP1ncQz3TX6KQP1H6DwE_gVgo";
        ;
        System.out.println(token.replaceFirst("Bearer ",""));
        System.out.println(token);
    }
}
