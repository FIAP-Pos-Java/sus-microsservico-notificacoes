package sus.microservico.notificacoes.sus_microservico_notificacoes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "tb_paciente")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paciente {
    
    @Id
    private UUID id;
    
    private String nome;
    private String email;
    private String telefone;
}
