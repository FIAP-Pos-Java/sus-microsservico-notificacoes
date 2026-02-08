package sus.microservico.notificacoes.sus_microservico_notificacoes.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sus.microservico.notificacoes.sus_microservico_notificacoes.controller.dto.BuscarTarefaDTO;
import sus.microservico.notificacoes.sus_microservico_notificacoes.controller.dto.TarefaDTO;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.TarefaAssistenteSocial;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.enums.StatusTarefa;
import sus.microservico.notificacoes.sus_microservico_notificacoes.repository.TarefaAssistenteSocialRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TarefaAssistenteSocialService {

    private final Logger logger = LoggerFactory.getLogger(TarefaAssistenteSocialService.class);
    private final TarefaAssistenteSocialRepository tarefaRepository;

    public List<BuscarTarefaDTO> buscarTarefasPorStatus(String status) {
        StatusTarefa statusTarefa = StatusTarefa.fromValue(status);
        List<TarefaAssistenteSocial> tarefas = tarefaRepository.findByStatus(statusTarefa);
        return tarefas.stream().map(tarefa -> new BuscarTarefaDTO(
                tarefa.getId(),
                tarefa.getPacienteId(),
                tarefa.getCirurgiaId(),
                tarefa.getDescricao(),
                tarefa.getStatus(),
                tarefa.getAssistenteSocialId(),
                tarefa.getDataCriacao(),
                tarefa.getDataConclusao()
        )).collect(Collectors.toList());
    }

    public void criarTarefa(TarefaDTO dto) {
        TarefaAssistenteSocial tarefa = new TarefaAssistenteSocial();
        tarefa.setPacienteId(dto.pacienteId());
        tarefa.setCirurgiaId(dto.cirurgiaId());
        tarefa.setDescricao(dto.descricao());
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setDataCriacao(LocalDateTime.now());
        
        tarefaRepository.save(tarefa);
        this.logger.info("Tarefa criada: {}", tarefa.getId());
    }

    public void atribuirTarefa(String id, UUID assistenteSocialId) {
        UUID uuid = UUID.fromString(id);
        TarefaAssistenteSocial tarefa = tarefaRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        
        tarefa.setAssistenteSocialId(assistenteSocialId);
        tarefa.setStatus(StatusTarefa.EM_ANDAMENTO);
        tarefaRepository.save(tarefa);
        this.logger.info("Tarefa {} atribuída ao assistente social {}", id, assistenteSocialId);
    }

    public void concluirTarefa(String id) {
        UUID uuid = UUID.fromString(id);
        TarefaAssistenteSocial tarefa = tarefaRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        
        tarefa.setStatus(StatusTarefa.CONCLUIDA);
        tarefa.setDataConclusao(LocalDateTime.now());
        tarefaRepository.save(tarefa);
        this.logger.info("Tarefa {} concluída", id);
    }
}
