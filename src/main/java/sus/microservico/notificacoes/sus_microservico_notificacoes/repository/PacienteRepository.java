package sus.microservico.notificacoes.sus_microservico_notificacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.Paciente;

import java.util.UUID;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, UUID> {
}
