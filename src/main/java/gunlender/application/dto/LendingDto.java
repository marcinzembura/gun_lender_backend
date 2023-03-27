package gunlender.application.dto;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class LendingDto {
    @NotNull
    private UUID userId;
    @NotNull
    private UUID gunId;
    @NotNull
    private UUID ammoId;
    private int ammoAmount;
    @NotNull
    private Instant reservationDate;
}
