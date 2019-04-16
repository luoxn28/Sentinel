package com.alibaba.csp.sentinel.dashboard.db.manager;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.db.entity.SentinelRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeFilter;

import java.util.Date;

/**
 * @author xiangnan.
 */
public class RuleConvert {

    public static SentinelRule toSentinelRule(FlowRuleEntity ruleEntity) {
        if (ruleEntity == null) {
            return null;
        }

        SentinelRule rule = new SentinelRule();
        rule.setType(RuleEnum.流控规则.ordinal());
        rule.setApp(ruleEntity.getApp());
        rule.setIp(ruleEntity.getIp());
        rule.setPort(ruleEntity.getPort());
        rule.setResource(ruleEntity.getResource());
        rule.setGmtCreate(ruleEntity.getGmtCreate() != null ? ruleEntity.getGmtCreate() : new Date());
        rule.setGmtModified(ruleEntity.getGmtModified() != null ? ruleEntity.getGmtModified() : new Date());

        JSON.toJSONString(ruleEntity, new SerializeFilter() {
            @Override
            public boolean equals(Object obj) {
                return super.equals(obj);
            }
        });

        return rule;
    }

}
