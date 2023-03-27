package gunlender.application.dto;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class LoginDto {
    @NotNull
    private String email;
    @NotNull
    private String password;
}
