package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
    @PositiveOrZero
    private Integer gapTop = 0;

    @NotNull
    @PositiveOrZero
    private Integer gapBottom = 0;

    @NotNull
    @PositiveOrZero
    private Integer gapLeft = 0;

    @NotNull
    @PositiveOrZero
    private Integer gapRight = 0;

    @Override
    public String toString(){
        return name;
    }
}
