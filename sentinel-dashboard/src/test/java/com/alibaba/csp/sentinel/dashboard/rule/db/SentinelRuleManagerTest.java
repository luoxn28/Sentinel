package com.alibaba.csp.sentinel.dashboard.rule.db;

import com.alibaba.csp.sentinel.dashboard.DashboardApplication;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.rule.mysql.DbFlowRuleStore;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author xiangnan.
 */
//@SpringBootTest
public class SentinelRuleManagerTest {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication
                .run(DashboardApplication.class, args);

        test_save_findById_delete(applicationContext);
    }

    static FlowRuleEntity entity = new FlowRuleEntity();
    static {
        entity.setApp("App1");
        entity.setIp("1.1.1.1");
        entity.setPort(8888);
        entity.setResource("HelloWorld");
        entity.setGmtCreate(new Date());
        entity.setGmtModified(new Date());
    }

    static void test_save_findById_delete(ConfigurableApplicationContext applicationContext) {
        DbFlowRuleStore mysqlAdapter = applicationContext
                .getBean(DbFlowRuleStore.class);

        entity.setResource(UUID.randomUUID().toString());
        FlowRuleEntity savedEntity = mysqlAdapter.save(entity);
        Assert.notNull(savedEntity.getIp());
        Assert.isTrue(Objects.equals(savedEntity.getApp(), entity.getApp()));
        Assert.isTrue(Objects.equals(savedEntity.getResource(), entity.getResource()));

        savedEntity.setApp("App2");
        FlowRuleEntity updatedEntity = mysqlAdapter.save(savedEntity);
        Assert.isTrue(Objects.equals(updatedEntity.getApp(), savedEntity.getApp()));
        Assert.isTrue(Objects.equals(savedEntity.getResource(), savedEntity.getResource()));

        FlowRuleEntity findEntity = mysqlAdapter.findById(savedEntity.getId());
        Assert.isTrue(Objects.equals(savedEntity.getId(), findEntity.getId()));
        Assert.isTrue(Objects.equals(savedEntity.getApp(), findEntity.getApp()));
        Assert.isTrue(Objects.equals(savedEntity.getResource(), findEntity.getResource()));

        List<FlowRuleEntity> findAllEntity = mysqlAdapter.findAllByApp(savedEntity.getApp());
        for (FlowRuleEntity rule : findAllEntity) {
            Assert.isTrue(Objects.equals(savedEntity.getApp(), rule.getApp()));
        }

//        SentinelRule removedEntity = mysqlAdapter.delete(savedEntity.getId());
//        Assert.isTrue(Objects.equals(savedEntity.getId(), removedEntity.getId()));
//        Assert.isTrue(Objects.equals(savedEntity.getApp(), removedEntity.getApp()));
//        Assert.isTrue(Objects.equals(savedEntity.getResource(), removedEntity.getResource()));
//        Assert.isTrue(Objects.equals(savedEntity.getType(), removedEntity.getType()));
    }

}
