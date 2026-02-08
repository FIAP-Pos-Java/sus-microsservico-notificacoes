package sus.microservico.notificacoes.sus_microservico_notificacoes.controller.dto;

import sus.microservico.notificacoes.sus_microservico_notificacoes.model.enums.StatusTarefa;

import java.time.LocalDateTime;
import java.util.UUID;

public record BuscarTarefaDTO(
        UUID id,
        UUID pacienteId,
        UUID cirurgiaId,
        String descricao,
        StatusTarefa status,
        UUID assistenteSocialId,
        LocalDateTime dataCriacao,
        LocalDateTime dataConclusao
) {
}
