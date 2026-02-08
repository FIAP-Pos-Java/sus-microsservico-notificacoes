package sus.microservico.notificacoes.sus_microservico_notificacoes.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "tb_cirurgia")
@Data
public class Cirurgia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID pacienteId;
    
    private UUID medicoId;
    
    private LocalDate dataCirurgia;
    
    private LocalTime horaCirurgia;
    
    private String local;
    
    @Column(nullable = false)
    private Boolean lembreteEnviado = false;
}
