package sus.microservico.notificacoes.sus_microservico_notificacoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SusMicroservicoNotificacoesApplication {

	public static void main(String[] args) {
		SpringApplication.run(SusMicroservicoNotificacoesApplication.class, args);
	}

}
