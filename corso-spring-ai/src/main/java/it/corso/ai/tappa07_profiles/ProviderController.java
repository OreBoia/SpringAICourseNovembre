package it.corso.ai.tappa07_profiles;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
class ProviderController {

    private final Environment env;   // accesso a profili e proprietà

    ProviderController(Environment env) {
        this.env = env;
    }

    @GetMapping("/provider")
    InfoProvider provider() {
        String profilo = String.join(",", env.getActiveProfiles());
        String modello = env.getProperty("spring.ai.openai.chat.model");
        return new InfoProvider(profilo, modello);
    }

    record InfoProvider(String profilo, String modello) {}
}
