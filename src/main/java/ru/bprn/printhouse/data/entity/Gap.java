package ru.bprn.printhouse.data.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
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
