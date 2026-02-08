package sus.microservico.notificacoes.sus_microservico_notificacoes.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sus.microservico.notificacoes.sus_microservico_notificacoes.client.CoreServiceClient;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.CirurgiaNotificacao;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.TarefaAssistenteSocial;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.enums.StatusTarefa;
import sus.microservico.notificacoes.sus_microservico_notificacoes.repository.CirurgiaNotificacaoRepository;
import sus.microservico.notificacoes.sus_microservico_notificacoes.repository.TarefaAssistenteSocialRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);
    private final CoreServiceClient coreClient;
    private final TarefaAssistenteSocialRepository tarefaRepository;
    private final CirurgiaNotificacaoRepository cirurgiaNotificacaoRepository;

    public void processarNotificacao(CirurgiaNotificacao cirurgia) {
        this.logger.info("Processando notificação para cirurgia {} do paciente {}", 
            cirurgia.getCirurgiaId(), cirurgia.getPacienteId());
        
        // 1. Consultar se paciente tem contato (REST ao core)
        boolean temContato = coreClient.pacienteTemContato(cirurgia.getPacienteId());
        
        if (temContato) {
            // 2a. Enviar notificação digital (simulação: apenas log)
            enviarNotificacaoDigital(cirurgia);
        } else {
            // 2b. Criar tarefa para assistente social
            TarefaAssistenteSocial tarefa = new TarefaAssistenteSocial();
            tarefa.setPacienteId(cirurgia.getPacienteId());
            tarefa.setCirurgiaId(cirurgia.getCirurgiaId());
            tarefa.setDescricao("Notificar paciente presencialmente sobre cirurgia " +
                "dia " + cirurgia.getDataCirurgia() + " às " + cirurgia.getHoraCirurgia() +
                " no local: " + cirurgia.getLocal());
            tarefa.setStatus(StatusTarefa.PENDENTE);
            tarefa.setDataCriacao(LocalDateTime.now());
            tarefaRepository.save(tarefa);
            this.logger.info("Tarefa criada para assistente social notificar paciente {} presencialmente", 
                cirurgia.getPacienteId());
        }
        
        // 3. Marcar como notificada
        cirurgia.setNotificacaoEnviada(true);
        cirurgia.setDataNotificacao(LocalDateTime.now());
        cirurgiaNotificacaoRepository.save(cirurgia);
        this.logger.info("Cirurgia {} marcada como notificada", cirurgia.getCirurgiaId());
    }

    private void enviarNotificacaoDigital(CirurgiaNotificacao cirurgia) {
        // Simulação (MVP): apenas log
        this.logger.info("==========================================================");
        this.logger.info("NOTIFICAÇÃO DIGITAL enviada ao paciente {}", cirurgia.getPacienteId());
        this.logger.info("Cirurgia agendada para {} às {}", cirurgia.getDataCirurgia(), cirurgia.getHoraCirurgia());
        this.logger.info("Local: {}", cirurgia.getLocal());
        this.logger.info("==========================================================");
        // Futuramente: integrar com serviço de e-mail/SMS
    }
}
