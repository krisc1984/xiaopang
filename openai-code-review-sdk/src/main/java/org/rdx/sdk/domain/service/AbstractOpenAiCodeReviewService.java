package org.rdx.sdk.domain.service;

import org.rdx.sdk.infrastructure.git.GitCommand;
import org.rdx.sdk.infrastructure.openai.IOpenAi;
import org.rdx.sdk.infrastructure.weixin.WeiXin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ author rdx
 * @ describe :
 * @ date  2024/12/3
 **/
public abstract class AbstractOpenAiCodeReviewService implements IOpenAiCodeReviewService{

    private final Logger logger = LoggerFactory.getLogger(AbstractOpenAiCodeReviewService.class);

    protected final GitCommand gitCommand;
    protected final IOpenAi openAI;
    protected final WeiXin weiXin;

    public AbstractOpenAiCodeReviewService(GitCommand gitCommand, IOpenAi openAI, WeiXin weiXin) {
        this.gitCommand = gitCommand;
        this.openAI = openAI;
        this.weiXin = weiXin;
    }

    @Override
    public void exec() throws Exception {
        //1.获取代码的修改，diff
        List<String> diffCode = getDiff();
        //2.openai对代码评审
        String recommend = reviewCode(diffCode.toString());
        //3.将评审代码写入日志中
        String url = writeLog(recommend);
        //4.发送微信公众号消息
        sendMessage(url);
    }

    protected abstract void sendMessage(String url) throws Exception;

    protected abstract String writeLog(String recommend) throws Exception;

    protected abstract String reviewCode(String diffCode) throws Exception;

    protected abstract List<String> getDiff() throws Exception;
}
