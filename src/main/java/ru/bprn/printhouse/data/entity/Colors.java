package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "colors")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
public class Colors {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    private String name = "";

    private Long printerID ;

    private Double coast;
}
