package org.rdx.sdk.domain.service.Impl;

import org.rdx.sdk.domain.service.AbstractOpenAiCodeReviewService;
import org.rdx.sdk.infrastructure.git.GitCommand;
import org.rdx.sdk.infrastructure.openai.DTO.ChatCompletionRequestDTO;
import org.rdx.sdk.infrastructure.openai.DTO.ChatCompletionSyncResponseDTO;
import org.rdx.sdk.infrastructure.openai.IOpenAi;
import org.rdx.sdk.infrastructure.weixin.DTO.TemplateMessageDTO;
import org.rdx.sdk.infrastructure.weixin.WeiXin;
import org.rdx.sdk.types.enums.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ author rdx
 * @ describe :
 * @ date  2024/12/3
 **/
public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenAi openAI, WeiXin weiXin) {
        super(gitCommand, openAI, weiXin);
    }

    @Override
    protected void sendMessage(String logUrl) throws Exception {
        Map<String, Map<String, String>> data = new HashMap<>();
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
        weiXin.sendTemplateMessage(logUrl, data);

    }

    @Override
    protected String writeLog(String recommend) throws Exception {
         return gitCommand.commitAndPush(recommend);
    }
    @Override
    protected String reviewCode(String diffCode) throws Exception {
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        // 系统角色定义
        String systemPrompt =
                "你是一个资深编程架构师，擅长结合业务需求、技术规范和代码上下文进行评审。请从以下维度分析：\n"
                        + "- 架构一致性：是否违反分层规范或设计模式\n"
                        + "- 可维护性：代码可读性、重复代码、魔法值\n"
                        + "- 安全性：数据加密、SQL注入风险\n"
                        + "- 性能影响：算法时间复杂度、并发竞争条件\n"
                        + "- 测试覆盖：关键路径是否有测试用例缺失";
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
            {
                add(new ChatCompletionRequestDTO.Prompt("system", systemPrompt)); // 设定评审框架
                add(new ChatCompletionRequestDTO.Prompt("user", "### Git Diff记录\n" + diffCode));
                add(new ChatCompletionRequestDTO.Prompt("user", getContextInfo())); // 注入补充上下文
                add(new ChatCompletionRequestDTO.Prompt("user", getTestReport()));  // 注入测试报告
            }
        });

        ChatCompletionSyncResponseDTO completions = openAI.CodeReview(chatCompletionRequest);
        ChatCompletionSyncResponseDTO.Message message = completions.getChoices().get(0).getMessage();
        return message.getContent();

    }
    // 补充上下文生成方法（示例）
    private String getContextInfo() {
        return "### 项目上下文\n"
                + "- **架构**：DDD分层（用户接口层→应用层→领域层→基础设施层）\n"
                + "- **技术栈**：Spring Boot 3.2 + MySQL 8.0 + Redis 7.0\n"
                + "- **关联需求**：用户积分系统（需求文档：https://xxx/fea-2024-08）\n"
                + "- **已知问题**：订单表未分库分表，新增代码需避免全表扫描";
    }

    // 测试报告生成方法（示例）
    private String getTestReport() {
        return "### 测试覆盖率\n"
                + "- 单元测试：85%（核心服务`PointService`覆盖率达92%）\n"
                + "- 集成测试：新增3个用例验证积分兑换并发场景\n"
                + "- 静态分析：SonarQube报告0严重漏洞";
    }

    @Override
    protected List<String> getDiff() throws Exception {
        return gitCommand.getCodeDiff();
    }
}
