package xiaozhi.modules.agent.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 智能体迁移DTO
 * 用于将指定MAC地址设备的智能体迁移到另一个设备
 */
@Data
@Schema(description = "智能体迁移对象")
public class AgentMigrateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "源设备MAC地址", example = "AA:BB:CC:DD:EE:FF")
    @NotBlank(message = "源设备MAC地址不能为空")
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", 
             message = "MAC地址格式不正确")
    private String sourceMacAddress;

    @Schema(description = "目标设备MAC地址", example = "11:22:33:44:55:66")
    @NotBlank(message = "目标设备MAC地址不能为空")
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", 
             message = "MAC地址格式不正确")
    private String targetMacAddress;

    @Schema(description = "是否删除源设备绑定(true:删除源设备智能体绑定, false:保留但清空绑定)", 
            example = "true", defaultValue = "true")
    private Boolean deleteSourceBinding = true;

    @Schema(description = "迁移确认码(防误操作,由前端生成)", example = "MIGRATE_123456")
    @NotBlank(message = "确认码不能为空")
    private String confirmationCode;

    @Schema(description = "迁移原因说明", example = "原设备损坏需要更换")
    private String reason;
}