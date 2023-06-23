package ru.bprn.printhouse.data;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy= InheritanceType.JOINED)
@DiscriminatorColumn(name="works_type")
@Table(name="works")
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

    public TypeOfWork getTypeOfWork() {
        return typeOfWork;
    }

    public void setTypeOfWork(TypeOfWork typeOfWork) {
        this.typeOfWork = typeOfWork;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer calculateTime() {
        return null;
    }

    @Override
    public Double calculateCost() {
        return null;
    }
}
