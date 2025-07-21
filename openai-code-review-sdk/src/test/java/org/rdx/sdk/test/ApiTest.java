package org.rdx.sdk.test;

import com.alibaba.fastjson2.JSON;
import org.junit.Test;
import org.rdx.sdk.domain.model.ChatCompletionSyncResponse;
import org.rdx.sdk.types.util.BearerTokenUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * @ author rdx
 * @ describe :
 * @ date  2024/11/22
 **/
public class ApiTest {
    public static void main(String[] args) {

    }
    @Test
    public void test() throws Exception {
        String apiKeySecret = "a7ce6f4ab128d3403ae7bb5e89edc699.33DZiyacos8M4e5d";
        String token = BearerTokenUtils.getToken(apiKeySecret);

        URL url =new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection =(HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        String code="1+1";

        String jsonInpuString = "{"
                + "\"model\":\"glm-4-flash\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: " + code + "\""
                + "    }"
                + "]"
                + "}";

        OutputStream outputStream = connection.getOutputStream();
        byte[] bytes = jsonInpuString.getBytes(StandardCharsets.UTF_8);
        outputStream.write(bytes);

        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder builder = new StringBuilder();
        while((line = bufferedReader.readLine())!= null){
            builder.append(line);
        }
        bufferedReader.close();
        outputStream.close();
        connection.disconnect();

        ChatCompletionSyncResponse response = JSON.parseObject(builder.toString(), ChatCompletionSyncResponse.class);
        System.out.println(response.getChoices().get(0).getMessage().getContent());
    }
}
