package ru.bprn.printhouse.data;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy= InheritanceType.JOINED)
@DiscriminatorColumn(name="works_type")
@Table(name="works")
public abstract class AbstractWork {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    private TypeOfWork typeOfWork;
    private Integer time = 0;
    private Double Cost = 0d;

    private Integer leaflets = 1;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer calculateTime() {return time;};

    public Double calculateCost() {return Cost;};


}
