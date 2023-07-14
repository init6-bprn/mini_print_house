package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "type_of_work")
public class TypeOfWork {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ToString.Include
    private String name = "";

}
