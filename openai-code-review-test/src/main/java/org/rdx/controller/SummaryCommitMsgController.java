package org.rdx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rdx.sdk.infrastructure.openai.DTO.ChatCompletionRequestDTO;
import org.rdx.sdk.infrastructure.openai.DTO.ChatCompletionSyncResponseDTO;
import org.rdx.sdk.infrastructure.openai.IOpenAi;
import org.rdx.sdk.infrastructure.openai.Impl.ChatGMLImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 控制器类，用于处理总结 commits Msg 信息的请求
@RestController
public class SummaryCommitMsgController {

    private static final Logger logger = LoggerFactory.getLogger(SummaryCommitMsgController.class);
    // 假设这是从配置文件或其他地方获取的提示信息
    private static final String summaryCommitMsgPrompt = "这里是总结提交信息的提示内容";
    private final ObjectMapper objectMapper = new ObjectMapper();
    // 假设从配置文件获取 API Key 和主机地址
    private static final String OPENAI_API_KEY = "your_openai_api_key";
    private static final String OPENAI_HOST = "https://api.openai.com/v1/chat/completions";
    private final IOpenAi openAi;

    public SummaryCommitMsgController() {
        this.openAi = new ChatGMLImpl(OPENAI_API_KEY, OPENAI_HOST);
    }

    /**
     * 总结 commits Msg 信息
     * @param requestBody 请求体，包含 commits 列表
     * @return 包含处理结果的响应实体
     */
    @PostMapping("/summaryCommitMsg")
    public ResponseEntity<Map<String, Object>> summaryCommitMsg(@RequestBody Map<String, List<String>> requestBody) {
        List<String> commits = requestBody.get("commits");

        try {
            // 模拟调用大模型的逻辑，这里直接返回模拟数据
            // 实际中可以使用 HttpClient 调用外部服务
            Map<String, Object> requestBodyForModel = new HashMap<>();
            // 假设 ChatCompletionRequestDTO 类中有 Prompt 内部类，用于存储消息信息
            ChatCompletionRequestDTO.Prompt message1 = new ChatCompletionRequestDTO.Prompt();
            message1.setRole("system");
            message1.setContent(summaryCommitMsgPrompt);

            ChatCompletionRequestDTO.Prompt message2 = new ChatCompletionRequestDTO.Prompt();
            try {
                // 处理 commits 列表，将换行符替换为空格
                String content = objectMapper.writeValueAsString(
                        // Java 8 Stream 写法，兼容旧版本
                        commits.stream().map(item -> item.replace("\n", " ")).collect(java.util.stream.Collectors.toList())
                );
                message2.setRole("user");
                message2.setContent(content);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                logger.error("JSON 序列化出错", e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "JSON 序列化出错: " + e.getMessage());
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 直接构建 List<Prompt> 类型的消息列表
            java.util.List<ChatCompletionRequestDTO.Prompt> messages = java.util.Arrays.asList(message1, message2);
            requestBodyForModel.put("messages", messages);

            // 创建 ChatCompletionRequestDTO 对象
            ChatCompletionRequestDTO completionRequestDTO = new ChatCompletionRequestDTO();
            // 传入 List<Prompt> 类型的消息列表
            completionRequestDTO.setMessages(messages);

            // 调用 OpenAI API
            ChatCompletionSyncResponseDTO responseDTO = openAi.CodeReview(completionRequestDTO);

            // 处理 OpenAI 的响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", responseDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception error) {
            logger.error("总结 commits Msg 信息时出错", error);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", error.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 模拟返回总结提交信息的结果
     * @return 模拟结果对象
     */
    private Map<String, Object> mockSummaryCommitMsg() {
        // 这里可以根据实际需求返回模拟数据
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("mockKey", "mockValue");
        return mockData;
    }
}


