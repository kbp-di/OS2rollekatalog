package dk.digitalidentity.rc.controller.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "SystemRoleAssignment")
public class SystemRoleAssignmentAM {
    @Schema(description = "")
    @NotNull
    private long systemRoleId;
}
