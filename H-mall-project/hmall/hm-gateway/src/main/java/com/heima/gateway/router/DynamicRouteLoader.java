package com.heima.gateway.router;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class DynamicRouteLoader {

    @Autowired
    private NacosConfigManager nacosConfigManager;
    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";
    private final Set<String> routeIds = new HashSet<>();

    @PostConstruct
    public void initRouteConfigListener() throws NacosException {
        // 1.项目启动时，先拉取一次配置，并添加配置监听器
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // 2.监听到配置信息变化，更新路由信息
                        updateConfigInfo(configInfo);
                    }
                });
        // 3.第一次读取配置，更新路由信息
        updateConfigInfo(configInfo);
    }

    public void updateConfigInfo(String configInfo) {
        log.info("更新路由信息：{}", configInfo);
        // 1.解析配置信息，转为RouteDefinition对象
        List<RouteDefinition> routeDefinitionList = JSONUtil.toList(configInfo, RouteDefinition.class);
        log.info("解析后的路由数量：{}", routeDefinitionList.size());

        // 2.删除旧路由
        routeIds.forEach(routeId -> {
            routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
        });
        routeIds.clear();

        // 3.更新路由表
        for (RouteDefinition routeDefinition : routeDefinitionList) {
            log.info("添加路由：{}", routeDefinition.getId());
            // 保存
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            // 记录路由信息
            routeIds.add(routeDefinition.getId());
        }
    }
}
