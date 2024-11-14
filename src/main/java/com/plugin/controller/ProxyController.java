package com.plugin.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.ChatMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 郑勇
 * @date 2024/11/14
 */
@RestController
public class ProxyController {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Resource
    private OpenAiApi openAiApi;

    public ProxyController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/api/tags")
    public Mono<JSONObject> getModels() {
        return webClient.get()
                .uri(baseUrl + "/v1/models")
                .header("Authorization", apiKey)
                .retrieve()
                .bodyToMono(String.class).map(v -> {
                    JSONObject object = JSONObject.parseObject(v);
                    List<JSONObject> list = object.getJSONArray("data").toList(JSONObject.class).stream().map(item -> {
                        JSONObject rs = new JSONObject();
                        rs.put("name", item.getString("id"));
                        rs.put("version", "v1");
                        rs.put("description", "xxxx");
                        rs.put("size", "1GB");
                        rs.put("last_modified", "2023-10-01T12:34:56Z");
                        return rs;
                    }).toList();

                    JSONObject result = new JSONObject();
                    result.put("models", list);

                    return result;
                });
    }

    @GetMapping("/health")
    public Mono<JSONObject> getHealth() {
        JSONObject object = new JSONObject();
        object.put("status", 200);
        object.put("message", "Service is running normally.");
        object.put("models_loaded", true);
        object.put("memory_usage", "50%");
        object.put("cpu_usage", "20%");
        return Mono.just(object);
    }

    @GetMapping("/")
    public Mono<JSONObject> getHealthEx() {
        JSONObject object = new JSONObject();
        object.put("status", 200);
        object.put("message", "Service is running normally.");
        object.put("models_loaded", true);
        object.put("memory_usage", "50%");
        object.put("cpu_usage", "20%");
        return Mono.just(object);
    }

//    @GetMapping("/api/tags")
    public Mono<JSONObject> tags() {
        JSONObject object = new JSONObject();
        object.put("models", JSONArray.parseArray("[\n" +
                "    {\n" +
                "      \"name\": \"gemini-1.5-flash\",\n" +
                "      \"modified_at\": \"2023-11-04T14:56:49.277302595-07:00\",\n" +
                "      \"size\": 7365960935,\n" +
                "      \"digest\": \"9f438cb9cd581fc025612d27f7c1a6669ff83a8bb0ed86c94fcf4c5440555697\",\n" +
                "      \"details\": {\n" +
                "        \"format\": \"gguf\",\n" +
                "        \"family\": \"llama\",\n" +
                "        \"families\": null,\n" +
                "        \"parameter_size\": \"13B\",\n" +
                "        \"quantization_level\": \"Q4_0\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"glm-4-flash\",\n" +
                "      \"modified_at\": \"2023-12-07T09:32:18.757212583-08:00\",\n" +
                "      \"size\": 3825819519,\n" +
                "      \"digest\": \"fe938a131f40e6f6d40083c9f0f430a515233eb2edaa6d72eb85c50d64f2300e\",\n" +
                "      \"details\": {\n" +
                "        \"format\": \"gguf\",\n" +
                "        \"family\": \"llama\",\n" +
                "        \"families\": null,\n" +
                "        \"parameter_size\": \"7B\",\n" +
                "        \"quantization_level\": \"Q4_0\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]"));
        object.put("status", 200);
        return Mono.just(object);
    }

    @PostMapping(value = "/api/chat1", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<JSONObject> proxyStreamEx(@RequestBody JSONObject message) {
        System.out.println("message ===>> " + message.toJSONString());

        OpenAiChatClient chatClient = new OpenAiChatClient(openAiApi,
                OpenAiChatOptions.builder()
                        .withModel(message.getString("model"))
                        .withMaxTokens(8000)
                        .withTemperature(0.3F)
                        .build()
        );

        List<JSONObject> messages = message.getList("messages", JSONObject.class);
        Message message1 = new ChatMessage(MessageType.SYSTEM,messages.get(0).getString("content"));
        ChatMessage content1 = new ChatMessage(MessageType.USER,messages.get(1).getString("content"));
        List<Message> msgList = List.of(message1,content1);


        return chatClient.stream(new Prompt(msgList)).map(v-> {
//            return v;

//           String result = JSONObject.toJSONString(v.getResult());
            JSONObject jsonObject = JSONObject.from(v.getResult());
            JSONObject result = new JSONObject();
            result.put("model", message.getString("model"));
            result.put("created_at", "2024-10-27T10:00:00Z");
            String content = jsonObject.getJSONObject("output").getString("content");

            System.out.println(jsonObject.getString("output"));

            JSONObject rmessage = new JSONObject();
            rmessage.put("content", StrUtil.isEmpty(content) ? "":content);
            rmessage.put("role", "assistant");
            rmessage.put("images", null);

            result.put("message", rmessage);
            result.put("done", StrUtil.isEmpty(content));

            return result;
        });

    }

    @PostMapping(value = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONObject proxyStream(@RequestBody JSONObject message) {
        System.out.println("message ===>> " + message.toJSONString());

        OpenAiChatClient chatClient = new OpenAiChatClient(openAiApi,
                OpenAiChatOptions.builder()
                        .withModel(message.getString("model"))
                        .withMaxTokens(8000)
                        .withTemperature(0.3F)
                        .build()
        );

        List<JSONObject> messages = message.getList("messages", JSONObject.class);
        Message message1 = new ChatMessage(MessageType.SYSTEM,messages.getFirst().getString("content"));
        List<Message> msgList = new ArrayList<>(List.of(message1));

        for (int i = 1; i < messages.size(); i++) {
            message1 = new ChatMessage(MessageType.USER,messages.get(i).getString("content"));
            msgList.add(message1);
        }

        JSONObject jsonObject = JSONObject.from(chatClient.call(new Prompt(msgList)));

        System.out.println("jsonObject ===>> " + jsonObject.toJSONString());

        JSONObject result = new JSONObject();
        result.put("model", message.getString("model"));
        result.put("created_at", "2024-10-27T10:00:00Z");
        String content = jsonObject.getJSONObject("result").getJSONObject("output").getString("content");

        JSONObject rmessage = new JSONObject();
        rmessage.put("content", StrUtil.isEmpty(content) ? "":content);
        rmessage.put("role", "assistant");
        rmessage.put("images", null);

        result.put("message", rmessage);
        result.put("done", true);

        return result;
    }

    @PostMapping(value = "/api/chatStream1", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody String message) {
        // 模拟每秒发送一次消息
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "Received message: " + message + " | Event: " + sequence);
    }

    private Mono<String> transformResponse(String originalResponse) {
        try {
            System.out.println("originalResponse ===>> " + originalResponse);
            // 处理普通的流响应
            if (originalResponse.contains("chat.completion.chunk")) {
                JsonNode originalNode = objectMapper.readTree(originalResponse);
                String content = originalNode.path("choices").get(0).path("delta").path("content").asText("");

                return Mono.just(createTransformedResponse(content, false));
            }

            // 处理 [DONE] 标记
            if ("[DONE]".equals(originalResponse)) {
                return Mono.just(createTransformedResponse("", true));
            }

            // 其他情况直接返回
            return Mono.just(originalResponse);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private String createTransformedResponse(String content, boolean isDone) {
        try {
            Map<String, Object> transformedResponse = new HashMap<>();
            transformedResponse.put("model", "llama2");
            transformedResponse.put("created_at", Instant.now().toString());
            transformedResponse.put("response", content);
            transformedResponse.put("done", isDone);

//            // 可选的额外字段
//            transformedResponse.put("context", List.of(1, 2, 3));
//            transformedResponse.put("total_duration", 2902435095L);
//            transformedResponse.put("load_duration", 2605831520L);
//            transformedResponse.put("prompt_eval_count", 17);
//            transformedResponse.put("prompt_eval_duration", 29322000L);
//            transformedResponse.put("eval_count", 13);
//            transformedResponse.put("eval_duration", 266499000L);

            return objectMapper.writeValueAsString(transformedResponse);
        } catch (Exception e) {
            // 处理转换异常
            return "";
        }
    }

}
