package org.rdx.sdk;

import com.alibaba.fastjson2.JSON;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.rdx.sdk.domain.model.ChatCompletionRequest;
import org.rdx.sdk.domain.model.ChatCompletionSyncResponse;
import org.rdx.sdk.infrastructure.git.GitCommand;
import org.rdx.sdk.types.weixin.Message;
import org.rdx.sdk.types.enums.Model;
import org.rdx.sdk.types.util.BearerTokenUtils;
import org.rdx.sdk.types.util.WXAccessTokenUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

/**
 * @ author rdx
 * @ describe :
 * @ date  2024/11/22
 **/
public class OpenAiCodeReviewTest {

    // Github 配置
    private  static final String github_review_log_uri ="https://github.com/krisc1984/xiaopang.git";

    // 工程配置 - 自动获取（常量化）
    private static final String COMMIT_PROJECT = "xiaopang";
    private static final String COMMIT_BRANCH = "main";
    private static final String COMMIT_AUTHOR = "krisc@163.com";
    private static final String COMMIT_MESSAGE = "123";

    // 工程配置 - 自动获取
    private String github_project;
    private String github_branch;
    private String github_author;
    public static void main(String[] args)throws Exception {
        System.out.println("测试执行");
        String github_token = System.getenv("GITHUB_TOKEN");
        System.out.println("测试执行"+github_token);
        GitCommand gitCommand = new GitCommand(github_review_log_uri, github_token,
                COMMIT_PROJECT,
                COMMIT_BRANCH,
                COMMIT_AUTHOR,
                COMMIT_MESSAGE
        );

        String diffCode =gitCommand.getCodeDiff();

       // String token = System.getenv("OPENAI_API_TOKEN");;
      /*  if (null == token || token.isEmpty()) {
            throw new RuntimeException("token is null");
        }
        // 1. 代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        ArrayList<Object> list = new ArrayList<>();
        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code:" + exitCode);*/

       // System.out.println("评审代码：" + diffCode.toString());

      /*  //2.Gpt评审
        String log = codeReview(diffCode.toString());
        System.out.println("review :" +log);
        //3.写入日志
        String url = codeLog(log, token);
        System.out.println("log url:"+url);
        //4.消息通知
        pushMessage(url);*/
    }

    private static String codeReview(String diffCode)throws Exception{
        String apiKeySecret = "a7ce6f4ab128d3403ae7bb5e89edc699.33DZiyacos8M4e5d";
        String token = BearerTokenUtils.getToken(apiKeySecret);

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequest.Prompt("user", diffCode));
            }
        });

        OutputStream outputStream = connection.getOutputStream();
        byte[] bytes = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
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
        return response.getChoices().get(0).getMessage().getContent();
    }

    private static String codeLog(String log,String token)throws Exception{
        Git git =Git.cloneRepository()
                .setURI("https://github.com/progressrdx/openai-code-review-log.git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token,""))
                .call();

        String dateFolderName =new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder =new File("repo/" + dateFolderName);
        if (!dateFolder.exists()){
            dateFolder.mkdirs();
        }
        String fileName =generateRandomString(12)+".md";
        File newFile=new File(dateFolder,fileName);
        try(FileWriter writer =new FileWriter(newFile)){
            writer.write(log);
        }
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        System.out.println("add");
        git.commit().setMessage("Add New File via github Action").call();
        System.out.println("commit");
        Iterable<PushResult> pushResults = git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();
        for (PushResult result : pushResults) {
            System.out.println(result);
        }
        System.out.println("All changes pushed to remote repository successfully.");

        return "https://github.com/progressrdx/openai-code-review-log/blob/main/"+dateFolderName+"/"+fileName;
    }

    private static void pushMessage(String logUrl) {
        String accessToken = WXAccessTokenUtils.getAccessToken();
        System.out.println(accessToken);

        Message message = new Message();
        message.put("project", "big-market");
        message.put("review", logUrl);
        message.setUrl(logUrl);
        message.setTemplate_id("PqnBIWgXyY39Wd9Gmi1dTOYl-dSXt54j7tH-ktVdIU0");
        message.setTouser("ozC5u64VgZVYAS-p0E1V0sTD1ICw");
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        sendPostRequest(url, JSON.toJSONString(message));
    }

    private static void sendPostRequest(String urlString, String jsonBody) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }


    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (null == value || value.isEmpty()) {
            throw new RuntimeException(key+" value is null");
        }
        return value;
    }
}


