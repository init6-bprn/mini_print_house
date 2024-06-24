package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode (onlyExplicitlyIncluded = true)
@ToString (onlyExplicitlyIncluded = true)
@Getter
@Setter

@Entity
public class DigitalPrintTemplate{
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank
    @ToString.Include
    private String name = "Name";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gap")
    private Gap gap;

    @NotNull
    @Positive
    private Integer pagePerProduct = 1;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn(name = "material")
    private Material material;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn(name = "standart_size")
    private StandartSize size;

    @NotBlank
    private String pipeWorkflow = "";

}