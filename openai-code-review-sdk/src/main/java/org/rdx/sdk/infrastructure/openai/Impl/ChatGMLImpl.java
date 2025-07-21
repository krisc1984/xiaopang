package org.rdx.sdk.infrastructure.openai.Impl;

import com.alibaba.fastjson2.JSON;
import org.rdx.sdk.infrastructure.openai.DTO.ChatCompletionRequestDTO;
import org.rdx.sdk.infrastructure.openai.DTO.ChatCompletionSyncResponseDTO;
import org.rdx.sdk.infrastructure.openai.IOpenAi;
import org.rdx.sdk.types.util.BearerTokenUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @ author rdx
 * @ describe :
 * @ date  2024/12/3
 **/
public class ChatGMLImpl implements IOpenAi {
    private final String apiKeySecret ;

    private final String host;

    public ChatGMLImpl(String apiKeySecret, String host) {
        this.apiKeySecret = apiKeySecret;
        this.host = host;
    }

    @Override
    public ChatCompletionSyncResponseDTO CodeReview(ChatCompletionRequestDTO requestDTO) throws Exception{
        String token = BearerTokenUtils.getToken(apiKeySecret);

        URL url = new URL(host);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = JSON.toJSONString(requestDTO).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        connection.disconnect();

        return JSON.parseObject(content.toString(), ChatCompletionSyncResponseDTO.class);
    }
}
