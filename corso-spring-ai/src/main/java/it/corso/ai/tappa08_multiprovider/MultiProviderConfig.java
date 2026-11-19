package it.corso.ai.tappa08_multiprovider;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("multi")   // esiste solo con il profilo "multi": richiede entrambe le chiavi
class MultiProviderConfig {

    @Bean
    ClientiAi clientiAi(@Value("${GROQ_API_KEY}") String groqKey,
                        @Value("${OPENAI_API_KEY}") String openaiKey) {

        OpenAiChatModel groqModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl("https://api.groq.com/openai/v1")
                        .apiKey(groqKey)
                        .model("openai/gpt-oss-120b")
                        .replayReasoningContent(false)
                        .build())
                .build();

        OpenAiChatModel openaiModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .apiKey(openaiKey)
                        .model("gpt-5-mini")
                        .build())
                .build();

        return new ClientiAi(ChatClient.create(groqModel), ChatClient.create(openaiModel));
    }
}
