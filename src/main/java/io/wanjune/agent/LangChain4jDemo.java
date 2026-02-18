package io.wanjune.agent;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * @author zjh
 * @since 2026/2/18 10:26
 */
public class LangChain4jDemo {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("OPENAI_API_KEY");
    private static final String BASE_URL = dotenv.get("OPENAI_BASE_URL"); // 注意在LangChain4j框架中，URL后面要加v1/（要以斜杆结尾）

    // 工具类
    static class WeatherTool {
        @Tool("获取指定城市的天气")
        public String getWeather(String city) {
            return city + "现在 25度, 晴朗";
        }
    }

    // AI 服务接口
    interface Assistant {
        String chat(String userMessage);
    }

    public static void main(String[] args) {
        // 构建模型，填好参数
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .modelName("gemini-3-flash")
                .logRequests(true)
                .logResponses(true)
                .build();

        // 构建Agent
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .tools(new WeatherTool())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        // 测试聊天
        System.out.println(assistant.chat("北京天气怎么样？"));
    }

}