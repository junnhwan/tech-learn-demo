import io.github.cdimascio.dotenv.Dotenv;

/**
 * @author zjh
 * @since 2026/2/18 10:08
 */
public class OutputTest {

    public static void main(String[] args) {
        testOutputURL();
    }

    public static void testOutputURL() {
        Dotenv dotenv = Dotenv.load();
        String openaiBaseUrl = dotenv.get("OPENAI_BASE_URL");
        String fullUrl = openaiBaseUrl + "/v1/chat/completions";
        System.out.println("Full URL: " + fullUrl); // 检查输出是否包含 https://
    }

}