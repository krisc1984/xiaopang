package org.rdx.sdk.infrastructure.openai;

import org.rdx.sdk.infrastructure.openai.DTO.ChatCompletionRequestDTO;
import org.rdx.sdk.infrastructure.openai.DTO.ChatCompletionSyncResponseDTO;

/**
 * @ author rdx
 * @ describe :
 * @ date  2024/12/3
 **/
public interface IOpenAi {
       ChatCompletionSyncResponseDTO CodeReview(ChatCompletionRequestDTO completionRequestDTO) throws Exception;

}
