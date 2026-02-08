package sus.microservico.notificacoes.sus_microservico_notificacoes.event;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record NotificacaoCirurgiaCanceladaEvent(
        UUID cirurgiaId,
        UUID pacienteId,
        UUID medicoId,
        LocalDate dataCirurgia,
        LocalTime horaCirurgia,
        String local
) implements Serializable {
}
