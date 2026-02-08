package sus.microservico.notificacoes.sus_microservico_notificacoes.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import sus.microservico.notificacoes.sus_microservico_notificacoes.event.NotificacaoCirurgiaAtualizadaEvent;
import sus.microservico.notificacoes.sus_microservico_notificacoes.event.NotificacaoCirurgiaCanceladaEvent;
import sus.microservico.notificacoes.sus_microservico_notificacoes.event.NotificacaoCirurgiaCriadaEvent;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.AssistenteSocial;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.Paciente;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.TarefaAssistenteSocial;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.enums.StatusTarefa;
import sus.microservico.notificacoes.sus_microservico_notificacoes.repository.PacienteRepository;
import sus.microservico.notificacoes.sus_microservico_notificacoes.repository.TarefaAssistenteSocialRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class NotificacaoService {

    private final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);
    private final PacienteRepository pacienteRepository;
    private final TarefaAssistenteSocialRepository tarefaRepository;
    private final JavaMailSender mailSender;
    
    @Value("${twilio.account.sid}")
    private String twilioAccountSid;
    
    @Value("${twilio.auth.token}")
    private String twilioAuthToken;
    
    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;
    
    @Value("${spring.mail.username}")
    private String emailFrom;
    
    public NotificacaoService(PacienteRepository pacienteRepository, 
                             TarefaAssistenteSocialRepository tarefaRepository,
                             JavaMailSender mailSender) {
        this.pacienteRepository = pacienteRepository;
        this.tarefaRepository = tarefaRepository;
        this.mailSender = mailSender;
    }
    
    @PostConstruct
    public void initTwilio() {
        if (twilioAccountSid != null && !twilioAccountSid.isBlank() && 
            twilioAuthToken != null && !twilioAuthToken.isBlank()) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            logger.info("Twilio inicializado com sucesso");
        } else {
            logger.warn("Credenciais do Twilio não configuradas - SMS não será enviado");
        }
    }

    public void processarNotificacaoCriacao(NotificacaoCirurgiaCriadaEvent evento) {
        logger.info("Processando notificação de criação para cirurgia {}", evento.cirurgiaId());
        
        Paciente paciente = pacienteRepository.findById(evento.pacienteId()).orElse(null);
        
        if (paciente == null) {
            logger.warn("Paciente {} não encontrado", evento.pacienteId());
            return;
        }
        
        String mensagem = String.format(
                "Cirurgia agendada para %s às %s no local: %s",
                evento.dataCirurgia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                evento.horaCirurgia().format(DateTimeFormatter.ofPattern("HH:mm")),
                evento.local()
        );
        
        enviarNotificacoes(paciente, "AGENDAMENTO", mensagem);
    }

    public void processarNotificacaoAtualizacao(NotificacaoCirurgiaAtualizadaEvent evento) {
        logger.info("Processando notificação de atualização para cirurgia {}", evento.cirurgiaId());
        
        Paciente paciente = pacienteRepository.findById(evento.pacienteId()).orElse(null);
        
        if (paciente == null) {
            logger.warn("Paciente {} não encontrado", evento.pacienteId());
            return;
        }
        
        String mensagem = String.format(
                "Cirurgia ATUALIZADA para %s às %s no local: %s",
                evento.dataCirurgia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                evento.horaCirurgia().format(DateTimeFormatter.ofPattern("HH:mm")),
                evento.local()
        );
        
        enviarNotificacoes(paciente, "ATUALIZAÇÃO", mensagem);
    }

    public void processarNotificacaoCancelamento(NotificacaoCirurgiaCanceladaEvent evento) {
        logger.info("Processando notificação de cancelamento para cirurgia {}", evento.cirurgiaId());
        
        Paciente paciente = pacienteRepository.findById(evento.pacienteId()).orElse(null);
        
        if (paciente == null) {
            logger.warn("Paciente {} não encontrado", evento.pacienteId());
            return;
        }
        
        String mensagem = String.format(
                "Cirurgia CANCELADA que estava agendada para %s às %s",
                evento.dataCirurgia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                evento.horaCirurgia().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
        
        enviarNotificacoes(paciente, "CANCELAMENTO", mensagem);
    }

    private void enviarNotificacoes(Paciente paciente, String tipo, String mensagem) {
        boolean pacienteNotificado = false;
        
        // Notificar paciente
        if (paciente.getEmail() != null && !paciente.getEmail().isBlank()) {
            enviarEmail(paciente.getEmail(), tipo, mensagem);
            pacienteNotificado = true;
        }
        
        if (paciente.getTelefone() != null && !paciente.getTelefone().isBlank()) {
            enviarSMS(paciente.getTelefone(), mensagem);
            pacienteNotificado = true;
        }
        
        // Se paciente não tem contato, criar tarefa para assistente social
        if (!pacienteNotificado) {
            criarTarefaAssistenteSocial(paciente.getId(), mensagem);
        }
        
        logger.info("Notificações enviadas - Paciente: {}", pacienteNotificado);
    }

    private void enviarEmail(String email, String tipo, String mensagem) {
        try {
            if (emailFrom == null || emailFrom.isBlank()) {
                logger.warn("E-mail de origem não configurado. Mensagem não enviada para: {}", email);
                return;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(email);
            message.setSubject("SusTech - Notificação de " + tipo);
            message.setText(mensagem);
            
            mailSender.send(message);
            
            logger.info("==========================================================");
            logger.info("EMAIL enviado com sucesso para: {}", email);
            logger.info("Tipo: {}", tipo);
            logger.info("Mensagem: {}", mensagem);
            logger.info("==========================================================");
        } catch (Exception e) {
            logger.error("Erro ao enviar e-mail para {}: {}", email, e.getMessage());
        }
    }

    private void enviarSMS(String telefone, String mensagem) {
        try {
            if (twilioPhoneNumber == null || twilioPhoneNumber.isBlank()) {
                logger.warn("Número do Twilio não configurado. SMS não enviado para: {}", telefone);
                return;
            }
            
            // formato internacional
            String telefoneFormatado = telefone.startsWith("+") ? telefone : "+55" + telefone.replaceAll("[^0-9]", "");
            
            Message message = Message.creator(
                    new PhoneNumber(telefoneFormatado),
                    new PhoneNumber(twilioPhoneNumber),
                    mensagem
            ).create();
            
            logger.info("==========================================================");
            logger.info("SMS enviado com sucesso para: {}", telefone);
            logger.info("SID: {}", message.getSid());
            logger.info("Mensagem: {}", mensagem);
            logger.info("==========================================================");
        } catch (Exception e) {
            logger.error("Erro ao enviar SMS para {}: {}", telefone, e.getMessage());
        }
    }

    private void criarTarefaAssistenteSocial(java.util.UUID pacienteId, String mensagem) {
        TarefaAssistenteSocial tarefa = new TarefaAssistenteSocial();
        tarefa.setPacienteId(pacienteId);
        tarefa.setDescricao("Notificar paciente presencialmente: " + mensagem);
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setDataCriacao(LocalDateTime.now());
        
        tarefaRepository.save(tarefa);
        logger.info("Tarefa criada para assistente social notificar paciente {} presencialmente", pacienteId);
    }
    
    public void enviarLembretePaciente(java.util.UUID pacienteId, String mensagem) {
        Paciente paciente = pacienteRepository.findById(pacienteId).orElse(null);
        
        if (paciente == null) {
            logger.warn("Paciente {} não encontrado para envio de lembrete", pacienteId);
            return;
        }
        
        boolean notificado = false;
        
        if (paciente.getEmail() != null && !paciente.getEmail().isBlank()) {
            enviarEmail(paciente.getEmail(), "LEMBRETE", mensagem);
            logger.info("Lembrete enviado para paciente por email");
            notificado = true;
        }
        
        if (paciente.getTelefone() != null && !paciente.getTelefone().isBlank()) {
            enviarSMS(paciente.getTelefone(), mensagem);
            logger.info("Lembrete enviado para paciente por SMS");
            notificado = true;
        }
        
        if (!notificado) {
            logger.warn("Paciente {} não possui e-mail ou telefone para receber lembrete", pacienteId);
        }
    }
    
    public void enviarLembreteAssistenteSocial(AssistenteSocial assistenteSocial, String mensagem) {
        if (assistenteSocial == null) {
            logger.warn("Assistente social não encontrada");
            return;
        }
        
        boolean notificado = false;
        
        if (assistenteSocial.getEmail() != null && !assistenteSocial.getEmail().isBlank()) {
            enviarEmail(assistenteSocial.getEmail(), "LEMBRETE CIRURGIA", mensagem);
            logger.info("Lembrete enviado para assistente social {} por email", assistenteSocial.getNome());
            notificado = true;
        }
        
        if (assistenteSocial.getTelefoneContato() != null && !assistenteSocial.getTelefoneContato().isBlank()) {
            enviarSMS(assistenteSocial.getTelefoneContato(), mensagem);
            logger.info("Lembrete enviado para assistente social {} por SMS", assistenteSocial.getNome());
            notificado = true;
        }
        
        if (!notificado) {
            logger.warn("Assistente social {} não possui e-mail ou telefone para receber lembrete", 
                       assistenteSocial.getId());
        }
    }
}
