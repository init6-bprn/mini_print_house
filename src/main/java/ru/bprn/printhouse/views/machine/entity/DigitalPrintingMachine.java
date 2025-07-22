package ru.bprn.printhouse.views.machine.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bprn.printhouse.data.entity.Gap;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class DigitalPrintingMachine extends AbstractMachine{

    @NotNull
    @PositiveOrZero
    private Integer maxSizeX;

    @NotNull
    @PositiveOrZero
    private Integer maxSizeY;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gap")
    private Gap gap;

    @PrePersist
    @PreUpdate
    private void initSearchStr() {
        String s = "";
        //if (printMashine!=null) s = printMashine.getName();
        this.searchStr = this.name+", ЦПМ (принтер): "+ s;
    }

}
