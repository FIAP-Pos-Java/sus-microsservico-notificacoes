package sus.microservico.notificacoes.sus_microservico_notificacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.CirurgiaNotificacao;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CirurgiaNotificacaoRepository extends JpaRepository<CirurgiaNotificacao, UUID> {
    List<CirurgiaNotificacao> findByNotificacaoEnviadaFalseAndDataCirurgiaLessThanEqual(LocalDate dataLimite);
}
