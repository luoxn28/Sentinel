package com.alibaba.csp.sentinel.dashboard.repository.rule.mysql;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.db.entity.SentinelRule;
import com.alibaba.csp.sentinel.dashboard.db.entity.SentinelRuleExample;
import com.alibaba.csp.sentinel.dashboard.db.manager.RuleEnum;
import com.alibaba.csp.sentinel.dashboard.db.mapper.SentinelRuleMapper;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xiangnan.
 */
public abstract class MysqlRuleRepositoryAdapter implements RuleRepository<SentinelRule, Integer> {

    @Resource
    private SentinelRuleMapper ruleMapper;

    @Override
    public SentinelRule save(SentinelRule entity) {
        entity = preProcess(entity);

        ruleMapper.insert(entity);
        SentinelRuleExample ruleExample = new SentinelRuleExample();
        ruleExample.createCriteria()
                .andAppEqualTo(entity.getApp())
                .andTypeEqualTo(entity.getType())
                .andResourceEqualTo(entity.getResource());
        List<SentinelRule> ruleList = ruleMapper.selectByExample(ruleExample);

        Assert.isTrue(ruleList.size() == 1, "新增规则异常 " + JSON.toJSONString(entity));
        return ruleList.get(0);
    }

    public boolean update(SentinelRule entity) {
        SentinelRule oldEntity = ruleMapper.selectByPrimaryKey(entity.getId());
        Assert.notNull(oldEntity);
        entity.setGmtCreate(oldEntity.getGmtCreate());
        return ruleMapper.updateByPrimaryKey(entity) > 0;
    }

    @Override
    public List<SentinelRule> saveAll(List<SentinelRule> rules) {
        throw new RuntimeException("IllegalAccessException");
    }

    @Override
    public SentinelRule delete(Integer id) {
        SentinelRule rule = ruleMapper.selectByPrimaryKey(id);
        ruleMapper.deleteByPrimaryKey(id);
        return rule;
    }

    @Override
    public SentinelRule findById(Integer id) {
        return ruleMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<SentinelRule> findAllByMachine(MachineInfo machineInfo) {
        throw new RuntimeException("IllegalAccessException");
    }

    @Override
    public List<SentinelRule> findAllByApp(String appName) {
        Assert.isTrue(StringUtil.isNotBlank(appName), "appName不能为空");

        return findAllByApp(appName, null);
    }

    public List<SentinelRule> findAllByApp(String appName, String ip) {
        Assert.isTrue(StringUtil.isNotBlank(appName), "appName不能为空");

        SentinelRuleExample ruleExample = new SentinelRuleExample();
        SentinelRuleExample.Criteria criteria = ruleExample.createCriteria()
                .andAppEqualTo(appName);
        if (StringUtil.isNotBlank(ip)) {
            criteria.andIpEqualTo(ip);
        }
        return ruleMapper.selectByExample(ruleExample);
    }

    protected SentinelRule preProcess(SentinelRule entity) {
        return entity;
    }

    // 转换方法

    public SentinelRule toSentinelRule(FlowRuleEntity entity) {
        SentinelRule rule = new SentinelRule();
        rule.setId(entity.getId().intValue());
        rule.setType(RuleEnum.流控规则.ordinal());
        rule.setApp(entity.getApp());
        rule.setResource(entity.getResource());
        rule.setRule(JSON.toJSONString(entity));

        return rule;
    }

    public FlowRuleEntity toRuleEntity(SentinelRule rule) {
        if (StringUtil.isBlank(rule.getRule())) {
            return null;
        }

        FlowRuleEntity entity;
        switch (rule.getType()) {
            // 流控规则
            case 0: {
                entity = JSON.parseObject(rule.getRule(), FlowRuleEntity.class);
                entity.setId(rule.getId().longValue());
                entity.setApp(rule.getApp());
                entity.setIp(rule.getIp());
                entity.setPort(rule.getPort());
                entity.setResource(rule.getResource());
                entity.setGmtCreate(rule.getGmtCreate());
                entity.setGmtModified(rule.getGmtModified());
                break;
            }
            default: {
                throw new RuntimeException("IllegalAccessException");
            }
        }

        return entity;
    }
}
