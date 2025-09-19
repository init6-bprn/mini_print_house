package ru.bprn.printhouse.views.machine.entity;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@Entity
@NoArgsConstructor
public class DigitalPrintingMachine extends AbstractMachine{


    @Override
    public String toString() {
        return name;
    }

    @PrePersist
    @PreUpdate
    private void initSearchStr() {
        this.searchStr = this.name + ", ЦПМ (принтер): ";
    }

}
