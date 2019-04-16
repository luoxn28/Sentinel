package com.alibaba.csp.sentinel.dashboard.controller.rule;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xiangnan.
 */
@RestController
@RequestMapping(value = "/dispatch")
public class FlowRuleController {

    private final Logger logger = LoggerFactory.getLogger(FlowRuleController.class);

    @Autowired
    private AuthService<HttpServletRequest> authService;

    @Resource
    private InMemoryRuleRepositoryAdapter<FlowRuleEntity> repository;

    @GetMapping("/rules")
    public Result<List<RuleEntity>> apiQueryMachineRules(HttpServletRequest request,
            @RequestParam String app, @RequestParam(required = false) String ip) {
        AuthService.AuthUser authUser = authService.getAuthUser(request);
        authUser.authTarget(app, AuthService.PrivilegeType.READ_RULE);

        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }

        try {
            List<RuleEntity> ruleEntityList = new ArrayList<>();
            List<FlowRuleEntity> ruleList = repository.findAllByApp(app);
            if (!CollectionUtils.isEmpty(ruleList)) {
                ruleEntityList.addAll(ruleList);
            }

            return Result.ofSuccess(ruleEntityList);
        } catch (Exception e) {
            logger.error("Error when querying flow rules", e);
            return Result.ofThrowable(-1, e);
        }
    }
}
