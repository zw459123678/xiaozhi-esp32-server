package xiaozhi.modules.agent.service.impl;

import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.user.UserDetail;
import xiaozhi.modules.agent.dto.AgentMigrateDTO;
import xiaozhi.modules.agent.entity.AgentChatHistoryEntity;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.agent.service.AgentChatHistoryService;
import xiaozhi.modules.agent.service.AgentMigrationService;
import xiaozhi.modules.agent.service.AgentService;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.device.service.DeviceService;
import xiaozhi.modules.security.user.SecurityUser;

/**
 * 智能体迁移服务实现类
 * 
 * @author Claude
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class AgentMigrationServiceImpl implements AgentMigrationService {

    private final DeviceService deviceService;
    private final AgentService agentService;
    private final AgentChatHistoryService agentChatHistoryService;
    private final RedisUtils redisUtils;

    // MAC地址正则表达式
    private static final Pattern MAC_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    
    // 确认码前缀
    private static final String CONFIRMATION_PREFIX = "MIGRATE_";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void migrateAgent(AgentMigrateDTO migrateDTO) {
        log.info("开始执行智能体迁移: 从 {} 到 {}", migrateDTO.getSourceMacAddress(), migrateDTO.getTargetMacAddress());
        
        try {
            // 1. 验证迁移请求
            validateMigrationRequest(migrateDTO);
            
            // 2. 获取并验证设备信息
            DeviceEntity sourceDevice = validateSourceDevice(migrateDTO.getSourceMacAddress());
            DeviceEntity targetDevice = validateTargetDevice(migrateDTO.getTargetMacAddress());
            
            // 3. 验证智能体信息
            AgentEntity agent = validateAgentForMigration(sourceDevice.getAgentId());
            
            // 4. 执行迁移核心逻辑
            executeMigration(migrateDTO, sourceDevice, targetDevice, agent);
            
            // 5. 清理缓存
            cleanupAfterMigration(sourceDevice, targetDevice, agent);
            
            // 6. 记录成功日志
            logMigrationAudit(migrateDTO, sourceDevice, targetDevice, agent, true, null);
            
            log.info("智能体迁移完成: 智能体 {} 从设备 {} 迁移到 {}", 
                     agent.getAgentName(), migrateDTO.getSourceMacAddress(), migrateDTO.getTargetMacAddress());
            
        } catch (Exception e) {
            log.error("智能体迁移失败: " + e.getMessage(), e);
            
            // 记录失败日志
            try {
                DeviceEntity sourceDevice = deviceService.getDeviceByMacAddress(migrateDTO.getSourceMacAddress());
                DeviceEntity targetDevice = deviceService.getDeviceByMacAddress(migrateDTO.getTargetMacAddress());
                AgentEntity agent = sourceDevice != null && sourceDevice.getAgentId() != null ? 
                                   agentService.selectById(sourceDevice.getAgentId()) : null;
                logMigrationAudit(migrateDTO, sourceDevice, targetDevice, agent, false, e.getMessage());
            } catch (Exception logError) {
                log.error("记录迁移失败日志时发生错误", logError);
            }
            
            throw new RenException("迁移操作失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validateMigrationRequest(AgentMigrateDTO migrateDTO) {
        // 1. 基础参数验证
        if (migrateDTO == null) {
            throw new RenException("迁移请求参数不能为空");
        }
        
        // 2. MAC地址格式验证
        if (!isValidMacAddress(migrateDTO.getSourceMacAddress())) {
            throw new RenException("源设备MAC地址格式不正确");
        }
        
        if (!isValidMacAddress(migrateDTO.getTargetMacAddress())) {
            throw new RenException("目标设备MAC地址格式不正确");
        }
        
        // 3. 不能是同一个设备
        if (migrateDTO.getSourceMacAddress().equalsIgnoreCase(migrateDTO.getTargetMacAddress())) {
            throw new RenException("源设备和目标设备不能相同");
        }
        
        // 4. 确认码验证
        if (!isValidConfirmationCode(migrateDTO.getConfirmationCode())) {
            throw new RenException("确认码格式不正确");
        }
        
        return true;
    }

    @Override
    public boolean isValidMacAddress(String macAddress) {
        return StringUtils.isNotBlank(macAddress) && MAC_PATTERN.matcher(macAddress).matches();
    }

    @Override
    public boolean isValidConfirmationCode(String confirmationCode) {
        return StringUtils.isNotBlank(confirmationCode) && 
               confirmationCode.startsWith(CONFIRMATION_PREFIX) &&
               confirmationCode.length() >= 12; // MIGRATE_ + 至少6位
    }

    @Override
    public DeviceEntity validateSourceDevice(String macAddress) {
        DeviceEntity sourceDevice = deviceService.getDeviceByMacAddress(macAddress);
        if (sourceDevice == null) {
            throw new RenException("源设备不存在: " + macAddress);
        }
        
        if (StringUtils.isBlank(sourceDevice.getAgentId())) {
            throw new RenException("源设备未绑定智能体");
        }
        
        // 验证用户权限
        UserDetail user = SecurityUser.getUser();
        if (user == null || user.getId() == null) {
            throw new RenException("用户未登录");
        }
        
        if (!sourceDevice.getUserId().equals(user.getId())) {
            throw new RenException("无权限操作源设备");
        }
        
        return sourceDevice;
    }

    @Override
    public DeviceEntity validateTargetDevice(String macAddress) {
        DeviceEntity targetDevice = deviceService.getDeviceByMacAddress(macAddress);
        if (targetDevice == null) {
            throw new RenException("目标设备不存在: " + macAddress);
        }
        
        if (StringUtils.isNotBlank(targetDevice.getAgentId())) {
            throw new RenException("目标设备已绑定智能体，请先解绑");
        }
        
        // 验证用户权限
        UserDetail user = SecurityUser.getUser();
        if (!targetDevice.getUserId().equals(user.getId())) {
            throw new RenException("无权限操作目标设备");
        }
        
        return targetDevice;
    }

    @Override
    public AgentEntity validateAgentForMigration(String agentId) {
        AgentEntity agent = agentService.selectById(agentId);
        if (agent == null) {
            throw new RenException("智能体不存在: " + agentId);
        }
        
        // 验证用户权限
        UserDetail user = SecurityUser.getUser();
        if (!agent.getUserId().equals(user.getId())) {
            throw new RenException("无权限操作该智能体");
        }
        
        return agent;
    }

    @Override
    public int migrateChatHistory(String sourceMacAddress, String targetMacAddress, String agentId) {
        log.info("开始迁移聊天记录: 从 {} 到 {}, 智能体ID: {}", sourceMacAddress, targetMacAddress, agentId);
        
        UpdateWrapper<AgentChatHistoryEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("mac_address", sourceMacAddress)
                     .eq("agent_id", agentId);
        
        AgentChatHistoryEntity updateEntity = new AgentChatHistoryEntity();
        updateEntity.setMacAddress(targetMacAddress);
        updateEntity.setUpdatedAt(new Date());
        
        boolean result = agentChatHistoryService.update(updateEntity, updateWrapper);
        
        // 获取更新的记录数量
        long count = agentChatHistoryService.count(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AgentChatHistoryEntity>()
                .eq("mac_address", targetMacAddress)
                .eq("agent_id", agentId)
        );
        
        log.info("聊天记录迁移完成: 更新了 {} 条记录", count);
        return (int) count;
    }

    @Override
    public void cleanupAfterMigration(DeviceEntity sourceDevice, DeviceEntity targetDevice, AgentEntity agent) {
        log.info("清理迁移相关缓存");
        
        try {
            // 1. 清理设备配置缓存
            redisUtils.delete(RedisKeys.getDeviceConfigKey(sourceDevice.getMacAddress()));
            redisUtils.delete(RedisKeys.getDeviceConfigKey(targetDevice.getMacAddress()));
            
            // 2. 清理智能体配置缓存
            String agentConfigKey = "agent:config:" + agent.getId();
            redisUtils.delete(agentConfigKey);
            
            // 3. 清理服务器配置缓存
            redisUtils.delete(RedisKeys.getServerConfigKey());
            
            // 4. 清理智能体设备最后连接时间缓存
            redisUtils.delete(RedisKeys.getAgentDeviceLastConnectedAtById(agent.getId()));
            
            log.info("缓存清理完成");
        } catch (Exception e) {
            log.warn("清理缓存时发生错误，但不影响迁移结果", e);
        }
    }

    @Override
    public void logMigrationAudit(AgentMigrateDTO migrateDTO, DeviceEntity sourceDevice, 
                                  DeviceEntity targetDevice, AgentEntity agent, 
                                  boolean success, String errorMessage) {
        try {
            UserDetail user = SecurityUser.getUser();
            
            String logMessage = String.format(
                "智能体迁移操作 - 用户ID: %s, 源设备: %s, 目标设备: %s, 智能体ID: %s, 智能体名称: %s, " +
                "删除源绑定: %s, 原因: %s, 结果: %s%s",
                user != null ? user.getId() : "未知",
                migrateDTO.getSourceMacAddress(),
                migrateDTO.getTargetMacAddress(),
                agent != null ? agent.getId() : "未知",
                agent != null ? agent.getAgentName() : "未知",
                migrateDTO.getDeleteSourceBinding(),
                StringUtils.isNotBlank(migrateDTO.getReason()) ? migrateDTO.getReason() : "未填写",
                success ? "成功" : "失败",
                success ? "" : ", 错误: " + errorMessage
            );
            
            if (success) {
                log.info("[迁移审计] {}", logMessage);
            } else {
                log.error("[迁移审计] {}", logMessage);
            }
            
            // TODO: 可以在这里将审计日志保存到数据库的审计表中
            
        } catch (Exception e) {
            log.error("记录迁移审计日志时发生错误", e);
        }
    }

    /**
     * 执行核心迁移逻辑
     */
    private void executeMigration(AgentMigrateDTO migrateDTO, DeviceEntity sourceDevice, 
                                 DeviceEntity targetDevice, AgentEntity agent) {
        log.info("执行核心迁移逻辑");
        
        String sourceAgentId = sourceDevice.getAgentId();
        String sourceMacAddress = sourceDevice.getMacAddress();
        String targetMacAddress = targetDevice.getMacAddress();
        
        // 1. 更新目标设备的智能体绑定
        targetDevice.setAgentId(sourceAgentId);
        targetDevice.setUpdateDate(new Date());
        UserDetail user = SecurityUser.getUser();
        if (user != null) {
            targetDevice.setUpdater(user.getId());
        }
        deviceService.updateById(targetDevice);
        log.info("目标设备智能体绑定更新完成");
        
        // 2. 迁移聊天记录
        int migratedChatCount = migrateChatHistory(sourceMacAddress, targetMacAddress, sourceAgentId);
        log.info("聊天记录迁移完成: {} 条", migratedChatCount);
        
        // 3. 处理源设备
        if (migrateDTO.getDeleteSourceBinding()) {
            // 清空源设备的智能体绑定
            sourceDevice.setAgentId(null);
            sourceDevice.setUpdateDate(new Date());
            if (user != null) {
                sourceDevice.setUpdater(user.getId());
            }
            deviceService.updateById(sourceDevice);
            log.info("源设备智能体绑定已清除");
        } else {
            // 保留源设备但清空智能体绑定（预留选项，当前逻辑与删除绑定相同）
            sourceDevice.setAgentId(null);
            sourceDevice.setUpdateDate(new Date());
            if (user != null) {
                sourceDevice.setUpdater(user.getId());
            }
            deviceService.updateById(sourceDevice);
            log.info("源设备智能体绑定已保留但清空");
        }
        
        // 4. 声纹信息和其他关联数据无需迁移，因为它们通过agentId关联
        log.info("核心迁移逻辑执行完成");
    }
}