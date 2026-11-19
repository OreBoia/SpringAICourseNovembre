package it.corso.ai.tappa10_microchat;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

import it.corso.ai.tappa10_microchat.TracciaStrumenti.ChiamataStrumento;

/**
 * Decoratore di un tool: esegue quello vero e annota nome, argomenti e risultato nella {@link TracciaStrumenti}.
 *
 * <p>È il pattern <i>Decorator</i>: stessa interfaccia ({@code ToolCallback}), stesso comportamento, più una
 * responsabilità in aggiunta. Il modello non si accorge di nulla: vede la stessa definizione del tool.
 */
class StrumentoTracciato implements ToolCallback {

    private final ToolCallback vero;
    private final TracciaStrumenti traccia;

    StrumentoTracciato(ToolCallback vero, TracciaStrumenti traccia) {
        this.vero = vero;
        this.traccia = traccia;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return vero.getToolDefinition();   // nome, descrizione e schema dei parametri restano quelli del metodo @Tool
    }

    @Override
    public String call(String argomenti) {
        return call(argomenti, null);
    }

    @Override
    public String call(String argomenti, ToolContext contesto) {
        String risultato = vero.call(argomenti, contesto);
        if (contesto != null && contesto.getContext().get(TracciaStrumenti.ID_RICHIESTA) instanceof String id) {
            traccia.registra(id, new ChiamataStrumento(getToolDefinition().name(), argomenti, risultato));
        }
        return risultato;
    }
}
