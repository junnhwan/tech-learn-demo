package io.wanjune.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import io.wanjune.agent.config.properties.HttpToolProperties;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.IDN;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author zjh
 * @since 2026/2/18 18:43
 */
@Component
public class HttpGetTool {

    private final OkHttpClient client;
    private final HttpToolProperties props;
    private final ObjectMapper objectMapper;

    public HttpGetTool(OkHttpClient client, HttpToolProperties props, ObjectMapper objectMapper) {
        this.client = client;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    @Tool("""
    Perform an HTTP GET request to an allowed host and return a JSON object:
    { ok, url, statusCode, contentType, truncated, bodySnippet } or { ok:false, errorType, message, url }.
    Allowed hosts are restricted by server configuration.
    """)
    public String get(String url) {
        try {
            Map<String, Object> result = doGet(url);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            String error = e.getClass().getSimpleName() + ": " + e.getMessage();
            try {
                return objectMapper.writeValueAsString(fail("TOOL_EXCEPTION", error, url));
            } catch (Exception ignored) {
                throw new RuntimeException(e);
            }
        }
    }

    private Map<String, Object> doGet(String url) throws Exception {
        URI uri = URI.create(url);
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http"))) {
            return fail("UNSUPPORTED_SCHEME", "Only http/https is supported", url);
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return fail("INVALID_URL", "URL host is missing", url);
        }
        // host 规范化（处理大小写、punycode）
        String normalizedHost = IDN.toASCII(host).toLowerCase(Locale.ROOT);
        // 禁止 IP 直连（简单版本：防止 127.0.0.1 / 10.x 等；更严谨可加 DNS 解析校验）
        if (looksLikeIpLiteral(normalizedHost) || normalizedHost.equals("localhost")) {
            return fail("HOST_NOT_ALLOWED", "IP/localhost is not allowed", url);
        }
        if (!isAllowedHost(normalizedHost)) {
            return fail("HOST_NOT_ALLOWED", "Host not allowed: " + normalizedHost, url);
        }
        int maxBytes = props.maxBodyBytes() == null ? 5120 : props.maxBodyBytes();
        Request request = new Request.Builder()
                .url(uri.toString())
                .get()
                .header("User-Agent", "agent-demo/0.1") // 某些站点没 UA 会拒绝
                .build();
        try (Response response = client.newCall(request).execute()) {
            String contentType = response.body() != null && response.body().contentType() != null
                    ? response.body().contentType().toString()
                    : null;
            byte[] bytes = new byte[0];
            boolean truncated = false;
            if (response.body() != null) {
                try (InputStream in = response.body().byteStream()) {
                    byte[] buf = in.readNBytes(maxBytes + 1);
                    if (buf.length > maxBytes) {
                        truncated = true;
                        byte[] cut = new byte[maxBytes];
                        System.arraycopy(buf, 0, cut, 0, maxBytes);
                        bytes = cut;
                    } else {
                        bytes = buf;
                    }
                }
            }
            String snippet = new String(bytes, StandardCharsets.UTF_8);
            Map<String, Object> ok = new LinkedHashMap<>();
            ok.put("ok", true);
            ok.put("url", uri.toString());
            ok.put("statusCode", response.code());
            ok.put("contentType", contentType);
            ok.put("truncated", truncated);
            ok.put("bodySnippet", snippet);
            return ok;
        }
    }

    private boolean isAllowedHost(String normalizedHost) {
        if (props.allowedHosts() == null) return false;
        return props.allowedHosts().stream()
                .filter(h -> h != null && !h.isBlank())
                .map(h -> IDN.toASCII(h).toLowerCase(Locale.ROOT))
                .anyMatch(allowed -> allowed.equals(normalizedHost));
    }
    private static boolean looksLikeIpLiteral(String host) {
        // 简单判断：IPv4 形态 / IPv6 方括号形态
        return host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")
                || host.startsWith("[") && host.endsWith("]");
    }

    private static Map<String, Object> fail(String type, String message, String url) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", false);
        m.put("errorType", type);
        m.put("message", message);
        m.put("url", url);
        return m;
    }

}