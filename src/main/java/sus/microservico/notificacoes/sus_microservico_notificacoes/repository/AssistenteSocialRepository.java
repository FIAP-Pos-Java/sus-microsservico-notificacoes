package sus.microservico.notificacoes.sus_microservico_notificacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.AssistenteSocial;

import java.util.UUID;

@Repository
public interface AssistenteSocialRepository extends JpaRepository<AssistenteSocial, UUID> {
}
