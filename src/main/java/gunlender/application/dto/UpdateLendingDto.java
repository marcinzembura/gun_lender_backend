package gunlender.application.dto;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
public class UpdateLendingDto {
    @NotNull
    private UUID userId;
    @NotNull
    private UUID oldGunId;
    @NotNull
    private UUID oldAmmoId;
    @NotNull
    private UUID newGunId;
    @NotNull
    private UUID newAmmoId;
    private int ammoAmount;
    @NotNull
    private Instant reservationDate;

    public UpdateLendingDto(LendingDto lendingDto, @NotNull UUID oldGunId, @NotNull UUID oldAmmoId) {
        userId = lendingDto.getUserId();
        this.oldGunId = oldGunId;
        this.oldAmmoId = oldAmmoId;
        newGunId = lendingDto.getGunId();
        newAmmoId =lendingDto.getAmmoId();
        ammoAmount = lendingDto.getAmmoAmount();
        reservationDate = lendingDto.getReservationDate();
    }
}
