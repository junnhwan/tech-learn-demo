package io.wanjune.agent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @author zjh
 * @since 2026/2/17 23:02
 */
public class HttpClientDemo {

    public static void main(String[] args) {
        // Http三要素：客户端、请求、响应
        try {
            // 创建 HttpClient 实例
            HttpClient client = HttpClient.newHttpClient();
            // 创建 HttpRequest 请求体
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://jsonplaceholder.typicode.com/posts/1"))
                    .GET()
                    .build();
            // 发送请求并返回响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("响应状态码: " + response.statusCode());
            System.out.println("响应内容: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}