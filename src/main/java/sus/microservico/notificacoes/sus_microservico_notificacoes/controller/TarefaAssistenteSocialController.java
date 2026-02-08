package sus.microservico.notificacoes.sus_microservico_notificacoes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sus.microservico.notificacoes.sus_microservico_notificacoes.controller.dto.BuscarTarefaDTO;
import sus.microservico.notificacoes.sus_microservico_notificacoes.controller.dto.TarefaDTO;
import sus.microservico.notificacoes.sus_microservico_notificacoes.service.TarefaAssistenteSocialService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/tarefas")
@RequiredArgsConstructor
public class TarefaAssistenteSocialController {

    private final Logger logger = LoggerFactory.getLogger(TarefaAssistenteSocialController.class);
    private final TarefaAssistenteSocialService tarefaService;

    @GetMapping
    public ResponseEntity<List<BuscarTarefaDTO>> buscarTarefasPorStatus(
            @RequestParam(value = "status", defaultValue = "PENDENTE") String status
    ) {
        this.logger.info("GET -> /api/v1/tarefas?status={}", status);
        List<BuscarTarefaDTO> tarefas = tarefaService.buscarTarefasPorStatus(status);
        return ResponseEntity.ok(tarefas);
    }

    @PostMapping
    public ResponseEntity<Void> criarTarefa(@Valid @RequestBody TarefaDTO dto) {
        this.logger.info("POST -> /api/v1/tarefas");
        tarefaService.criarTarefa(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("{id}/atribuir")
    public ResponseEntity<Void> atribuirTarefa(
            @PathVariable String id,
            @RequestParam UUID assistenteSocialId
    ) {
        this.logger.info("PUT -> /api/v1/tarefas/{}/atribuir", id);
        tarefaService.atribuirTarefa(id, assistenteSocialId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}/concluir")
    public ResponseEntity<Void> concluirTarefa(@PathVariable String id) {
        this.logger.info("PUT -> /api/v1/tarefas/{}/concluir", id);
        tarefaService.concluirTarefa(id);
        return ResponseEntity.noContent().build();
    }
}
