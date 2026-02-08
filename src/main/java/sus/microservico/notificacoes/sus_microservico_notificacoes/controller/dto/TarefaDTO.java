package sus.microservico.notificacoes.sus_microservico_notificacoes.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TarefaDTO(
        @NotNull
        UUID pacienteId,
        
        @NotNull
        UUID cirurgiaId,
        
        @NotBlank
        String descricao
) {
}
