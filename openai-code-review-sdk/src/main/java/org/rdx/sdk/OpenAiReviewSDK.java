package org.rdx.sdk;

import org.rdx.sdk.domain.service.Impl.OpenAiCodeReviewService;
import org.rdx.sdk.infrastructure.git.GitCommand;
import org.rdx.sdk.infrastructure.openai.Impl.ChatGMLImpl;
import org.rdx.sdk.infrastructure.weixin.WeiXin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ author rdx
 * @ describe :
 * @ date  2024/12/4
 **/
public class OpenAiReviewSDK {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReview.class);
    // 配置配置
    private static final String weixin_appid = "wx503c07ee8e9848ee";
    private static final String weixin_secret = "2e174af9438d8937ba9b98ccd4858e5b";
    private static final String weixin_touser = "ozC5u64VgZVYAS-p0E1V0sTD1ICw";
    private static final String weixin_template_id = "hGw7vyz12-gQNHwfj6cxXN6rZk9me4aHts5ASX0yRtY";

    // ChatGLM 配置
    private static final String chatglm_apiHost = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String chatglm_apiKeySecret = "a7ce6f4ab128d3403ae7bb5e89edc699.33DZiyacos8M4e5d";

    // Github 配置
    private  static final String github_review_log_uri ="https://github.com/progressrdx/openai-code-review-log.git";
    private static final String github_token = getEnv("GITHUB_TOKEN");

    // 工程配置 - 自动获取
    private String github_project;
    private String github_branch;
    private String github_author;

    public static void main(String[] args) throws Exception {
        //1.
        GitCommand gitCommand = new GitCommand(github_review_log_uri, github_token, getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_MESSAGE")
        );

        WeiXin weiXin = new WeiXin(weixin_appid, weixin_secret, weixin_touser, weixin_template_id);

        ChatGMLImpl chatGML = new ChatGMLImpl(chatglm_apiKeySecret, chatglm_apiHost);

        OpenAiCodeReviewService codeReviewService = new OpenAiCodeReviewService(gitCommand, chatGML, weiXin);
        codeReviewService.exec();

        logger.info("openai-code-review done!");
    }

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (null == value || value.isEmpty()) {
            throw new RuntimeException(key+" value is null");
        }
        return value;
    }


}
