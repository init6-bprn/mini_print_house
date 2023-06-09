package ru.bprn.printhouse.data.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class Gap {
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
    private Integer GapRight = 0;
}
