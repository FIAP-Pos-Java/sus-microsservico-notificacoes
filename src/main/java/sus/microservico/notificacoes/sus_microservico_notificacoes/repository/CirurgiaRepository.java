package sus.microservico.notificacoes.sus_microservico_notificacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sus.microservico.notificacoes.sus_microservico_notificacoes.model.Cirurgia;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CirurgiaRepository extends JpaRepository<Cirurgia, UUID> {
    List<Cirurgia> findByDataCirurgiaAndLembreteEnviadoFalse(LocalDate dataCirurgia);
}
