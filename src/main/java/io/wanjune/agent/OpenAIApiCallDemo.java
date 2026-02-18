package io.wanjune.agent;

import io.github.cdimascio.dotenv.Dotenv;

import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @author zjh
 * @since 2026/2/17 23:24
 */
public class OpenAIApiCallDemo {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String BASE_URL = dotenv.get("OPENAI_BASE_URL");
    private static final String API_KEY = dotenv.get("OPENAI_API_KEY");

    public static void main(String[] args) {

        try {
            HttpClient client = HttpClient.newHttpClient();
            String requestBody = """
                    {
                        "model": "gemini-3-flash",
                        "messages": [
                            {
                                "role": "user",
                                "content": "北京天气怎么样？"
                            },
                            {
                                "role": "assistant",
                                "content": null,
                                "tool_calls": [
                                    {
                                        "id": "call_e6907cf6f8314e34acad64d81deff42d",
                                        "type": "function",
                                        "function": {
                                            "name": "get_weather",
                                            "arguments": "{\\"location\\":\\"北京\\"}"
                                        }
                                    }
                                ]
                            },
                            {
                                "role": "tool",
                                "tool_call_id": "call_e6907cf6f8314e34acad64d81deff42d",
                                "name": "get_weather",
                                "content": "{\\"temp\\": \\"25度\\", \\"weather\\": \\"晴朗\\"}"
                            }
                        ]
                    }
                    """;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + "/v1/chat/completions"))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("响应状态码: " + response.statusCode());
            System.out.println("响应内容: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getRealWeather(String location) {
        // 这里模拟查询数据库或调用第三方API
        return "{\"temp\": \"25度\", \"weather\": \"晴朗\"}";
    }

}