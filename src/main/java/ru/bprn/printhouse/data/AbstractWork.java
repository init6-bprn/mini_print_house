package ru.bprn.printhouse.data;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Inheritance(strategy= InheritanceType.JOINED)
@Table(name="abstract_works")
public abstract class AbstractWork implements CalculateCostAndTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "type_of_work_id")
    private TypeOfWork typeOfWork;

    private Integer time = 0;
    private Double Cost = 0d;

    private Integer leaflets = 1;

    @Override
    public Integer calculateTime() {
        return null;
    }

    @Override
    public Double calculateCost() {
        return null;
    }
}
