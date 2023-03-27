package gunlender.application.dto;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class ChangePasswordDto {
    @NotNull
    private String password;
}
