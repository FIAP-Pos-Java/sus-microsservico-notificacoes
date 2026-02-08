package sus.microservico.notificacoes.sus_microservico_notificacoes.client;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoreServiceClient {
    
    private final Logger logger = LoggerFactory.getLogger(CoreServiceClient.class);
    private final RestTemplate restTemplate;
    private static final String CORE_URL = "http://localhost:8080/api/v1";

    public boolean pacienteTemContato(UUID id) {
        try {
            String url = CORE_URL + "/pacientes/" + id + "/tem-contato";
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            Boolean temContato = response.getBody();
            this.logger.info("Paciente {} tem contato: {}", id, temContato);
            return temContato != null && temContato;
        } catch (Exception e) {
            this.logger.error("Erro ao verificar contato do paciente {}: {}", id, e.getMessage());
            return false;
        }
    }
}
