package org.rdx.sdk.domain.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GitHubPRCommenter {
    private static final String GITHUB_API_BASE = "https://api.github.com";

    /**
     * 向 GitHub PR 提交评论
     * @param repo GitHub 仓库名称，例如 hustcer/deepseek-review
     * @param prNumber GitHub PR 编号
     * @param comments 要提交的评论内容
     * @param ghToken GitHub 访问令牌
     * @throws Exception 网络请求或其他异常
     */
    public static void postCommentsToPR(String repo, String prNumber, String comments, String ghToken) throws Exception {
        // 构建评论的 URL
        String commentUrl = GITHUB_API_BASE + "/repos/" + repo + "/issues/" + prNumber + "/comments";
        URL url = new URL(commentUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        // 设置请求头
        connection.setRequestProperty("Authorization", "Bearer " + ghToken);
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // 构建 JSON 请求体
        String jsonInputString = "{ \"body\": \"" + comments.replace("\"", "\\\"") + "\" }";

        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        }

        // 获取响应码
        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            // 读取响应内容
            try (Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8")) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println("评论提交成功，响应内容: " + response);
            }
        } else {
            // 读取错误响应内容
            try (Scanner scanner = new Scanner(connection.getErrorStream(), "UTF-8")) {
                String errorResponse = scanner.useDelimiter("\\A").next();
                System.err.println("评论提交失败，响应码: " + responseCode + "，错误信息: " + errorResponse);
            }
        }
    }

    public static void main(String[] args) {
        String repo = "hustcer/deepseek-review";
        String prNumber = "123";
        String comments = "这是一条测试评论。";
        String ghToken = "your_github_token";

        try {
            postCommentsToPR(repo, prNumber, comments, ghToken);
        } catch (Exception e) {
            System.err.println("提交评论时发生异常: " + e.getMessage());
        }
    }
}
