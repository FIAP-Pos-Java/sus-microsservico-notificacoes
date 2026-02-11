package sus.microservico.notificacoes.sus_microservico_notificacoes.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);
    
    public static final String EXCHANGE = "sus.exchange";
    
    public static final String CIRURGIA_CRIADA_QUEUE = "notificacao.cirurgia.criada.queue";
    public static final String CIRURGIA_ATUALIZADA_QUEUE = "notificacao.cirurgia.atualizada.queue";
    public static final String CIRURGIA_CANCELADA_QUEUE = "notificacao.cirurgia.cancelada.queue";
    
    public static final String NOTIFICACAO_CIRURGIA_CRIADA_ROUTING_KEY = "notificacao.cirurgia.criada";
    public static final String NOTIFICACAO_CIRURGIA_ATUALIZADA_ROUTING_KEY = "notificacao.cirurgia.atualizada";
    public static final String NOTIFICACAO_CIRURGIA_CANCELADA_ROUTING_KEY = "notificacao.cirurgia.cancelada";
    
    @PostConstruct
    public void init() {
        logger.info("==========================================================");
        logger.info(" RABBITMQ CONFIG - MICROSERVIÇO NOTIFICAÇÕES");
        logger.info("==========================================================");
        logger.info("Filas que este serviço ESCUTA:");
        logger.info("  • {} -> {}", CIRURGIA_CRIADA_QUEUE, NOTIFICACAO_CIRURGIA_CRIADA_ROUTING_KEY);
        logger.info("  • {} -> {}", CIRURGIA_ATUALIZADA_QUEUE, NOTIFICACAO_CIRURGIA_ATUALIZADA_ROUTING_KEY);
        logger.info("  • {} -> {}", CIRURGIA_CANCELADA_QUEUE, NOTIFICACAO_CIRURGIA_CANCELADA_ROUTING_KEY);
        logger.info("==========================================================");
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue notificacaoCirurgiaCriadaQueue() {
        return new Queue(CIRURGIA_CRIADA_QUEUE, true);
    }

    @Bean
    public Binding notificacaoCirurgiaCriadaBinding() {
        return BindingBuilder.bind(notificacaoCirurgiaCriadaQueue())
                .to(exchange())
                .with(NOTIFICACAO_CIRURGIA_CRIADA_ROUTING_KEY);
    }

    @Bean
    public Queue notificacaoCirurgiaAtualizadaQueue() {
        return new Queue(CIRURGIA_ATUALIZADA_QUEUE, true);
    }

    @Bean
    public Binding notificacaoCirurgiaAtualizadaBinding() {
        return BindingBuilder.bind(notificacaoCirurgiaAtualizadaQueue())
                .to(exchange())
                .with(NOTIFICACAO_CIRURGIA_ATUALIZADA_ROUTING_KEY);
    }

    @Bean
    public Queue notificacaoCirurgiaCanceladaQueue() {
        return new Queue(CIRURGIA_CANCELADA_QUEUE, true);
    }

    @Bean
    public Binding notificacaoCirurgiaCanceladaBinding() {
        return BindingBuilder.bind(notificacaoCirurgiaCanceladaQueue())
                .to(exchange())
                .with(NOTIFICACAO_CIRURGIA_CANCELADA_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
