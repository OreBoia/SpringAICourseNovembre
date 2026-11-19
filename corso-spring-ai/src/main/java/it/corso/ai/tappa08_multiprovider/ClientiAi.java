package it.corso.ai.tappa08_multiprovider;

import org.springframework.ai.chat.client.ChatClient;

// Un record, non una Map<String, ChatClient>: Spring riempirebbe la Map con i bean di tipo ChatClient, non con questa
public record ClientiAi(ChatClient groq, ChatClient openai) {
}
