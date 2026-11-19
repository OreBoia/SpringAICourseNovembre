package it.corso.ai;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * L'applicazione intera, avviata con un ChatModel finto al posto di quello di Groq/OpenAI:
 * verifica che tutte le tappe convivano e che i profili accendano le parti giuste.
 */
class CorsoAiApplicationTests {

    // spring.ai.model.chat=none: lo starter non crea il modello OpenAI; al suo posto c'è il bean qui sotto
    @TestConfiguration
    static class ModelloFinto {
        @Bean
        ChatModel chatModel() {
            return FakeChatModel.cheRisponde("risposta finta");
        }
    }

    @Nested
    @SpringBootTest(properties = {"spring.ai.model.chat=none", "GROQ_API_KEY=finta"}, classes = {CorsoAiApplication.class, ModelloFinto.class})
    @AutoConfigureMockMvc
    class ProfiloPredefinito {

        @Autowired
        MockMvc mvc;

        @Test
        void leTappeRispondono() throws Exception {
            mvc.perform(get("/ai/chat").param("message", "ciao"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("risposta finta"));
            mvc.perform(post("/ai/assistente/anna").contentType(MediaType.TEXT_PLAIN).content("Ciao"))
                    .andExpect(status().isOk());
            mvc.perform(get("/ai/provider"))
                    .andExpect(jsonPath("$.profilo").value("groq"))
                    .andExpect(jsonPath("$.modello").value("openai/gpt-oss-120b"));
        }

        @Test
        void gliEserciziNonSvoltiRispondono501() throws Exception {
            mvc.perform(get("/ai/traduci").param("testo", "ciao").param("lingua", "inglese"))
                    .andExpect(status().isNotImplemented());
        }

        @Test
        void tappeAProfiloSpente() throws Exception {
            mvc.perform(get("/rag/cerca").param("q", "ferie")).andExpect(status().isNotFound());
            mvc.perform(get("/ai/risposta").param("provider", "groq").param("domanda", "x"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void laPaginaWebEServita() throws Exception {
            mvc.perform(get("/index.html")).andExpect(status().isOk());
            mvc.perform(get("/chat.html")).andExpect(status().isOk());
        }

        @Test
        void laMicroChatRispondeInStreamingERicordaLaSessione() throws Exception {
            MockHttpSession sessione = new MockHttpSession();

            MvcResult avviata = mvc.perform(post("/chat/messaggio").session(sessione)
                            .contentType(MediaType.TEXT_PLAIN).content("Ciao, sono Anna"))
                    .andExpect(request().asyncStarted())
                    .andReturn();
            mvc.perform(asyncDispatch(avviata))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                    // il testo viaggia dentro un oggetto JSON: lo spazio finale di "risposta " resta intatto
                    .andExpect(content().string(containsString("event:testo\ndata:{\"testo\":\"risposta \"}")))
                    .andExpect(content().string(containsString("event:fine")));

            // stessa sessione (stesso cookie): la memoria ha domanda e risposta
            mvc.perform(get("/chat/stato").session(sessione)).andExpect(jsonPath("$.messaggiInMemoria").value(2));
            mvc.perform(get("/chat/storia").session(sessione))
                    .andExpect(jsonPath("$[0].ruolo").value("utente"))
                    .andExpect(jsonPath("$[0].testo").value("Ciao, sono Anna"));
            // un altro browser (nuova sessione) non vede nulla
            mvc.perform(get("/chat/stato")).andExpect(jsonPath("$.messaggiInMemoria").value(0));
            // nuova conversazione
            mvc.perform(post("/chat/nuova").session(sessione)).andExpect(jsonPath("$.messaggiInMemoria").value(0));
        }

        @Test
        void unMessaggioVuotoDa400() throws Exception {
            mvc.perform(post("/chat/messaggio").contentType(MediaType.TEXT_PLAIN).content("   "))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @SpringBootTest(properties = {"spring.ai.model.chat=none", "GROQ_API_KEY=finta", "OPENAI_API_KEY=finta"},
            classes = {CorsoAiApplication.class, ModelloFinto.class})
    @ActiveProfiles({"groq", "multi", "soluzioni"})
    @AutoConfigureMockMvc
    class ProfiliMultiESoluzioni {

        @Autowired
        MockMvc mvc;

        @Test
        void leSoluzioniSostituisconoGliStub() throws Exception {
            mvc.perform(get("/ai/traduci").param("testo", "ciao").param("lingua", "inglese"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("risposta finta"));
        }

        @Test
        void unProviderSconosciutoDa400() throws Exception {
            mvc.perform(get("/ai/risposta").param("provider", "gemini").param("domanda", "ciao"))
                    .andExpect(status().isBadRequest());
        }
    }
}
