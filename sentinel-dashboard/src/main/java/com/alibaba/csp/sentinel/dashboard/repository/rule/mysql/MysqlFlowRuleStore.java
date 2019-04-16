package com.alibaba.csp.sentinel.dashboard.repository.rule.mysql;

import com.alibaba.csp.sentinel.dashboard.db.entity.SentinelRule;
import com.alibaba.csp.sentinel.dashboard.db.manager.RuleEnum;
import org.springframework.stereotype.Component;

/**
 * @author xiangnan.
 */
@Component
public class MysqlFlowRuleStore extends MysqlRuleRepositoryAdapter {

    @Override
    protected SentinelRule preProcess(SentinelRule entity) {
        entity.setType(RuleEnum.流控规则.ordinal());
        return entity;
    }
}
