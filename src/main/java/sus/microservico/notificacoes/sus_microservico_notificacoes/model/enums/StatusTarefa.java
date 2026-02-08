package sus.microservico.notificacoes.sus_microservico_notificacoes.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum StatusTarefa {
    PENDENTE,
    EM_ANDAMENTO,
    CONCLUIDA;

    @JsonCreator
    public static StatusTarefa fromValue(String value) {
        return StatusTarefa.valueOf(value.toUpperCase());
    }
}
