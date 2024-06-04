package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "gap")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter

public class Gap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    private String name = "";

    @NotNull
    private Integer gapTop = 0;

    @NotNull
    private Integer gapBottom = 0;

    @NotNull
    private Integer gapLeft = 0;

    @NotNull
    private Integer gapRight = 0;

    @Override
    public String toString(){
        return name;
    }
}
