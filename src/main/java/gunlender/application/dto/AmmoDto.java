package gunlender.application.dto;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class AmmoDto {
    @NotNull
    private String caliber;
    @NotNull
    private String amount;
    @NotNull
    private String price;
    @NotNull
    private String picture;
}
