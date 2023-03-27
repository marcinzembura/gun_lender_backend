package gunlender.application.dto;

import gunlender.domain.services.AuthManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class ChangeRoleDto {
    @NotNull
    private AuthManager.Role role;
}
