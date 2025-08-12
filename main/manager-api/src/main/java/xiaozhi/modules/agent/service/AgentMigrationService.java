package xiaozhi.modules.agent.service;

import xiaozhi.modules.agent.dto.AgentMigrateDTO;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.device.entity.DeviceEntity;

/**
 * 智能体迁移服务接口
 * 
 * @author Claude
 * @version 1.0
 * @since 1.0.0
 */
public interface AgentMigrationService {

    /**
     * 执行智能体迁移
     * 将指定MAC地址设备的智能体完整迁移到另一个设备
     * 
     * @param migrateDTO 迁移请求参数
     */
    void migrateAgent(AgentMigrateDTO migrateDTO);

    /**
     * 验证迁移请求的有效性
     * 
     * @param migrateDTO 迁移请求参数
     * @return 验证通过返回true，否则抛出异常
     */
    boolean validateMigrationRequest(AgentMigrateDTO migrateDTO);

    /**
     * 检查MAC地址格式是否正确
     * 
     * @param macAddress MAC地址
     * @return 格式正确返回true
     */
    boolean isValidMacAddress(String macAddress);

    /**
     * 验证确认码是否有效
     * 
     * @param confirmationCode 确认码
     * @return 有效返回true
     */
    boolean isValidConfirmationCode(String confirmationCode);

    /**
     * 验证并获取源设备信息
     * 
     * @param macAddress 源设备MAC地址
     * @return 设备实体
     */
    DeviceEntity validateSourceDevice(String macAddress);

    /**
     * 验证并获取目标设备信息
     * 
     * @param macAddress 目标设备MAC地址
     * @return 设备实体
     */
    DeviceEntity validateTargetDevice(String macAddress);

    /**
     * 验证智能体是否可以迁移
     * 
     * @param agentId 智能体ID
     * @return 智能体实体
     */
    AgentEntity validateAgentForMigration(String agentId);

    /**
     * 执行聊天记录迁移
     * 
     * @param sourceMacAddress 源MAC地址
     * @param targetMacAddress 目标MAC地址  
     * @param agentId 智能体ID
     * @return 迁移的记录数量
     */
    int migrateChatHistory(String sourceMacAddress, String targetMacAddress, String agentId);

    /**
     * 清理迁移相关的缓存
     * 
     * @param sourceDevice 源设备
     * @param targetDevice 目标设备
     * @param agent 智能体
     */
    void cleanupAfterMigration(DeviceEntity sourceDevice, DeviceEntity targetDevice, AgentEntity agent);

    /**
     * 记录迁移操作审计日志
     * 
     * @param migrateDTO 迁移参数
     * @param sourceDevice 源设备
     * @param targetDevice 目标设备
     * @param agent 智能体
     * @param success 操作是否成功
     * @param errorMessage 错误信息(如果有)
     */
    void logMigrationAudit(AgentMigrateDTO migrateDTO, DeviceEntity sourceDevice, 
                          DeviceEntity targetDevice, AgentEntity agent, 
                          boolean success, String errorMessage);
}