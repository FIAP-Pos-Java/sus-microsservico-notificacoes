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
        try {
            logger.info("==========================================================");
            logger.info("PROCESSANDO NOTIFICAÇÃO DE CRIAÇÃO");
            logger.info("Cirurgia ID: {}", evento.cirurgiaId());
            logger.info("Paciente ID: {}", evento.pacienteId());
            logger.info("==========================================================");
            
            Paciente paciente = pacienteRepository.findById(evento.pacienteId()).orElse(null);
            
            if (paciente == null) {
                logger.error("==========================================================");
                logger.error("PACIENTE NÃO ENCONTRADO");
                logger.error("Paciente ID: {}", evento.pacienteId());
                logger.error("Tabela: tb_usuario_paciente");
                logger.error("Verifique se o paciente foi cadastrado corretamente!");
                logger.error("==========================================================");
                return;
            }
            
            logger.info("Paciente encontrado: {}", paciente.getNome());
            logger.info("E-mail: {}", paciente.getEmail() != null ? paciente.getEmail() : "(não possui)");
            logger.info("Telefone: {}", paciente.getTelefone() != null ? paciente.getTelefone() : "(não possui)");
            
            String mensagem = String.format(
                    "Cirurgia agendada para %s às %s no local: %s",
                    evento.dataCirurgia().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    evento.horaCirurgia().format(DateTimeFormatter.ofPattern("HH:mm")),
                    evento.local()
            );
            
            enviarNotificacoes(paciente, "AGENDAMENTO", mensagem);
            
            logger.info("==========================================================");
            logger.info("✓ NOTIFICAÇÃO PROCESSADA COM SUCESSO");
            logger.info("==========================================================");
        } catch (Exception e) {
            logger.error("==========================================================");
            logger.error("ERRO AO PROCESSAR NOTIFICAÇÃO DE CRIAÇÃO");
            logger.error("Cirurgia ID: {}", evento.cirurgiaId());
            logger.error("Paciente ID: {}", evento.pacienteId());
            logger.error("Erro: {}", e.getMessage());
            logger.error("Stack trace:", e);
            logger.error("==========================================================");
            throw e;
        }
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
        logger.info("----------------------------------------------------------");
        logger.info("INICIANDO ENVIO DE NOTIFICAÇÕES");
        logger.info("Paciente: {}", paciente.getNome());
        logger.info("----------------------------------------------------------");
        
        boolean pacienteNotificado = false;
        
        // Notificar paciente por e-mail
        if (paciente.getEmail() != null && !paciente.getEmail().isBlank()) {
            logger.info("📧 Paciente possui e-mail. Tentando enviar...");
            boolean emailEnviado = enviarEmail(paciente.getEmail(), tipo, mensagem);
            if (emailEnviado) {
                pacienteNotificado = true;
                logger.info("E-mail marcado como enviado");
            } else {
                logger.warn("E-mail NÃO foi enviado com sucesso");
            }
        } else {
            logger.info("Paciente NÃO possui e-mail cadastrado");
        }
        
        // Notificar paciente por SMS
        if (paciente.getTelefone() != null && !paciente.getTelefone().isBlank()) {
            logger.info("📱 Paciente possui telefone. Tentando enviar SMS...");
            boolean smsEnviado = enviarSMS(paciente.getTelefone(), mensagem);
            if (smsEnviado) {
                pacienteNotificado = true;
                logger.info("✓ SMS marcado como enviado");
            } else {
                logger.warn("⚠ SMS NÃO foi enviado com sucesso");
            }
        } else {
            logger.info("ℹ Paciente NÃO possui telefone cadastrado");
        }
        
        // Se paciente não tem contato, criar tarefa para assistente social
        if (!pacienteNotificado) {
            logger.warn("⚠ PACIENTE NÃO FOI NOTIFICADO (sem e-mail e sem telefone)");
            logger.info("Criando tarefa para Assistente Social...");
            criarTarefaAssistenteSocial(paciente.getId(), mensagem);
        } else {
            logger.info("✓ Paciente foi notificado com sucesso!");
        }
        
        logger.info("----------------------------------------------------------");
        logger.info("✓ ENVIO DE NOTIFICAÇÕES CONCLUÍDO");
        logger.info("Paciente notificado: {}", pacienteNotificado ? "SIM" : "NÃO (Tarefa criada para AS)");
        logger.info("----------------------------------------------------------");
    }

    private boolean enviarEmail(String email, String tipo, String mensagem) {
        try {
            logger.info("   → Verificando configuração de e-mail...");
            
            if (emailFrom == null || emailFrom.isBlank()) {
                logger.error("   ❌ E-MAIL DE ORIGEM NÃO CONFIGURADO!");
                logger.error("   Verifique a variável MAIL_USERNAME no .env");
                logger.error("   Valor atual: {}", emailFrom);
                return false;
            }
            
            logger.info("   ✓ E-mail de origem configurado: {}", emailFrom);
            logger.info("   → Criando mensagem de e-mail...");
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(email);
            message.setSubject("SusTech - Notificação de " + tipo);
            message.setText(mensagem);
            
            logger.info("   → Enviando e-mail via JavaMailSender...");
            logger.info("   De: {}", emailFrom);
            logger.info("   Para: {}", email);
            logger.info("   Assunto: SusTech - Notificação de {}", tipo);
            
            mailSender.send(message);
            
            logger.info("==========================================================");
            logger.info("✅ EMAIL ENVIADO COM SUCESSO!");
            logger.info("Destinatário: {}", email);
            logger.info("Tipo: {}", tipo);
            logger.info("Mensagem: {}", mensagem);
            logger.info("==========================================================");
            return true;
        } catch (Exception e) {
            logger.error("==========================================================");
            logger.error("❌ ERRO AO ENVIAR E-MAIL");
            logger.error("Destinatário: {}", email);
            logger.error("E-mail de origem: {}", emailFrom);
            logger.error("Tipo de erro: {}", e.getClass().getSimpleName());
            logger.error("Mensagem de erro: {}", e.getMessage());
            logger.error("Stack trace:", e);
            logger.error("----------------------------------------------------------");
            logger.error("POSSÍVEIS CAUSAS:");
            logger.error("1. Credenciais do Gmail incorretas no .env");
            logger.error("2. Senha de app do Gmail não configurada");
            logger.error("3. Servidor SMTP não acessível (smtp.gmail.com:587)");
            logger.error("4. Autenticação de 2 fatores não habilitada no Gmail");
            logger.error("==========================================================");
            return false;
        }
    }

    private boolean enviarSMS(String telefone, String mensagem) {
        try {
            logger.info("   → Verificando configuração do Twilio...");
            
            if (twilioPhoneNumber == null || twilioPhoneNumber.isBlank()) {
                logger.warn("   ⚠ TWILIO NÃO CONFIGURADO");
                logger.warn("   SMS não será enviado (isso é opcional)");
                logger.warn("   Para habilitar SMS, configure as variáveis TWILIO_* no .env");
                return false;
            }
            
            logger.info("   ✓ Twilio configurado");
            logger.info("   → Formatando número de telefone...");
            
            // formato internacional
            String telefoneFormatado = telefone.startsWith("+") ? telefone : "+55" + telefone.replaceAll("[^0-9]", "");
            logger.info("   Número original: {}", telefone);
            logger.info("   Número formatado: {}", telefoneFormatado);
            
            logger.info("   → Enviando SMS via Twilio...");
            Message message = Message.creator(
                    new PhoneNumber(telefoneFormatado),
                    new PhoneNumber(twilioPhoneNumber),
                    mensagem
            ).create();
            
            logger.info("==========================================================");
            logger.info("✅ SMS ENVIADO COM SUCESSO!");
            logger.info("Destinatário: {}", telefone);
            logger.info("Twilio SID: {}", message.getSid());
            logger.info("Mensagem: {}", mensagem);
            logger.info("==========================================================");
            return true;
        } catch (Exception e) {
            logger.error("==========================================================");
            logger.error("❌ ERRO AO ENVIAR SMS");
            logger.error("Destinatário: {}", telefone);
            logger.error("Número Twilio: {}", twilioPhoneNumber);
            logger.error("Tipo de erro: {}", e.getClass().getSimpleName());
            logger.error("Mensagem de erro: {}", e.getMessage());
            logger.error("Stack trace:", e);
            logger.error("----------------------------------------------------------");
            logger.error("POSSÍVEIS CAUSAS:");
            logger.error("1. Credenciais do Twilio incorretas no .env");
            logger.error("2. Número de telefone do Twilio não verificado");
            logger.error("3. Saldo insuficiente na conta Twilio");
            logger.error("4. Número de destino inválido");
            logger.error("==========================================================");
            return false;
        }
    }

    private void criarTarefaAssistenteSocial(java.util.UUID pacienteId, String mensagem) {
        try {
            logger.info("   → Criando tarefa para Assistente Social...");
            
            TarefaAssistenteSocial tarefa = new TarefaAssistenteSocial();
            tarefa.setPacienteId(pacienteId);
            tarefa.setDescricao("Notificar paciente presencialmente: " + mensagem);
            tarefa.setStatus(StatusTarefa.PENDENTE);
            tarefa.setDataCriacao(LocalDateTime.now());
            
            TarefaAssistenteSocial tarefaSalva = tarefaRepository.save(tarefa);
            
            logger.info("==========================================================");
            logger.info("✅ TAREFA CRIADA PARA ASSISTENTE SOCIAL");
            logger.info("Tarefa ID: {}", tarefaSalva.getId());
            logger.info("Paciente ID: {}", pacienteId);
            logger.info("Status: {}", StatusTarefa.PENDENTE);
            logger.info("Descrição: {}", tarefaSalva.getDescricao());
            logger.info("==========================================================");
        } catch (Exception e) {
            logger.error("==========================================================");
            logger.error("❌ ERRO AO CRIAR TAREFA PARA ASSISTENTE SOCIAL");
            logger.error("Paciente ID: {}", pacienteId);
            logger.error("Erro: {}", e.getMessage());
            logger.error("Stack trace:", e);
            logger.error("==========================================================");
            throw e;
        }
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
