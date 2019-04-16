package com.alibaba.csp.sentinel.dashboard.repository.rule.mysql;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.db.entity.SentinelRule;
import com.alibaba.csp.sentinel.dashboard.db.entity.SentinelRuleExample;
import com.alibaba.csp.sentinel.dashboard.db.mapper.SentinelRuleMapper;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库存储
 */
public abstract class DbRuleRepositoryAdapter<T extends RuleEntity> implements RuleRepository<T, Long> {

    @Resource
    protected SentinelRuleMapper ruleMapper;

    @Override
    public T save(T entity) {
        SentinelRule sentinelRule = convertSentinelRule(entity);
        if (sentinelRule == null) {
            return null;
        }

        // 更新时返回该规则
        SentinelRule oldEntity;
        if (sentinelRule.getId() != null &&
                (oldEntity = ruleMapper.selectByPrimaryKey(sentinelRule.getId())) != null) {
            sentinelRule.setGmtCreate(oldEntity.getGmtCreate());
            sentinelRule.setGmtModified(new Date());
            ruleMapper.updateByPrimaryKey(sentinelRule);
            return convertRuleEntity(ruleMapper.selectByPrimaryKey(sentinelRule.getId()));
        }

        // 保存规则到db，并返回该规则
        sentinelRule.setGmtCreate(new Date());
        sentinelRule.setGmtModified(sentinelRule.getGmtCreate());
        ruleMapper.insert(sentinelRule);

        SentinelRuleExample ruleExample = new SentinelRuleExample();
        ruleExample.createCriteria()
                .andAppEqualTo(sentinelRule.getApp())
                .andTypeEqualTo(sentinelRule.getType())
                .andResourceEqualTo(sentinelRule.getResource());
        List<SentinelRule> ruleList = ruleMapper.selectByExample(ruleExample);
        Assert.isTrue(ruleList.size() == 1, "新增规则异常 " + JSON.toJSONString(entity));

        return convertRuleEntity(ruleList.get(0));
    }

    @Override
    public List<T> saveAll(List<T> rules) {
        return rules.stream().map(this::save).collect(Collectors.toList());
    }

    @Override
    public T delete(Long id) {
        SentinelRule sentinelRule = ruleMapper.selectByPrimaryKey(id.intValue());
        if (sentinelRule == null) {
            return null;
        }

        ruleMapper.deleteByPrimaryKey(id.intValue());
        return convertRuleEntity(sentinelRule);
    }

    @Override
    public T findById(Long id) {
        SentinelRule sentinelRule = ruleMapper.selectByPrimaryKey(id.intValue());
        return sentinelRule != null ? convertRuleEntity(sentinelRule) : null;
    }

    @Override
    public List<T> findAllByMachine(MachineInfo machineInfo) {
        throw new RuntimeException("IllegalAccessException");
    }

    @Override
    public List<T> findAllByApp(String appName) {
        Assert.isTrue(StringUtil.isNotBlank(appName), "appName不能为空");

        SentinelRuleExample ruleExample = new SentinelRuleExample();
        ruleExample.createCriteria().andAppEqualTo(appName);
        List<SentinelRule> ruleList = ruleMapper.selectByExample(ruleExample);
        return CollectionUtils.isEmpty(ruleList) ? new ArrayList<>() :
                ruleList.stream().map(this::convertRuleEntity).collect(Collectors.toList());
    }

    public abstract SentinelRule convertSentinelRule(T entity);

    public abstract T convertRuleEntity(SentinelRule sentinelRule);
}
