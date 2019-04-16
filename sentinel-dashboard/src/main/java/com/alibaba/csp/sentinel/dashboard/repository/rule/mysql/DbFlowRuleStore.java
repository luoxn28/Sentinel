package com.alibaba.csp.sentinel.dashboard.repository.rule.mysql;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.db.entity.SentinelRule;
import com.alibaba.csp.sentinel.dashboard.db.entity.SentinelRuleExample;
import com.alibaba.csp.sentinel.dashboard.db.manager.RuleEnum;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author xiangnan.
 */
@Component
public class DbFlowRuleStore extends DbRuleRepositoryAdapter<FlowRuleEntity> implements
        InitializingBean {

    @Resource
    private InMemoryRuleRepositoryAdapter<FlowRuleEntity> repository;

    @Override
    public FlowRuleEntity save(FlowRuleEntity entity) {
        FlowRuleEntity dbEntity = super.save(entity);
        repository.save(JSON.parseObject(JSON.toJSONString(dbEntity), FlowRuleEntity.class));
        return dbEntity;
    }

    @Override
    public List<FlowRuleEntity> findAllByMachine(MachineInfo machineInfo) {
        return repository.findAllByMachine(machineInfo);
    }

    @Override
    public FlowRuleEntity delete(Long id) {
        repository.delete(id);
        SentinelRule sentinelRule = ruleMapper.selectByPrimaryKey(id.intValue());
        if (sentinelRule == null) {
            return null;
        }

        ruleMapper.deleteByPrimaryKey(id.intValue());
        return convertRuleEntity(sentinelRule);
    }

    @Override
    public SentinelRule convertSentinelRule(FlowRuleEntity entity) {

        SentinelRule rule = new SentinelRule();
        if (entity.getId() != null) {
            rule.setId(entity.getId().intValue());
        }
        rule.setType(RuleEnum.流控规则.ordinal());
        rule.setApp(entity.getApp());
        rule.setResource(entity.getResource());
        rule.setRule(JSON.toJSONString(entity));

        return rule;
    }

    @Override
    public FlowRuleEntity convertRuleEntity(SentinelRule rule) {
        if (rule == null || StringUtil.isBlank(rule.getRule())) {
            return null;
        }

        Assert.isTrue(Objects.equals(RuleEnum.流控规则.ordinal(), rule.getType()));
        FlowRuleEntity entity = JSON.parseObject(rule.getRule(), FlowRuleEntity.class);
        entity.setId(rule.getId().longValue());
        entity.setApp(rule.getApp());
        entity.setResource(rule.getResource());
        entity.setGmtCreate(rule.getGmtCreate());
        entity.setGmtModified(rule.getGmtModified());
        return entity;
    }

    /**
     * 加载数据库限流规则到内存
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        SentinelRuleExample ruleExample = new SentinelRuleExample();
        ruleExample.createCriteria().andTypeEqualTo(RuleEnum.流控规则.ordinal());

        List<SentinelRule> ruleList = ruleMapper.selectByExample(ruleExample);
        if (!CollectionUtils.isEmpty(ruleList)) {
            for (SentinelRule rule : ruleList) {
                repository.save(convertRuleEntity(rule));
            }
        }
    }
}
