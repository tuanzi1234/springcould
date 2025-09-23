package com.heima.gateway.filters;

import cn.hutool.core.text.AntPathMatcher;
import com.heima.gateway.config.AuthProperties;
import com.heima.gateway.utils.JwtTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private AuthProperties authProperties;
    @Autowired
    private JwtTool jwtTool;

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取request
        ServerHttpRequest request = exchange.getRequest();
        // 2.判断是否需要放行
        if (isExclude(request.getPath().toString())){
            // 放行
            return chain.filter(exchange);
        }
        // 3.获取token
        String token = null;
        List<String> tokens = request.getHeaders().get("Authorization");
        if (tokens != null && !tokens.isEmpty()) {
            token = tokens.get(0);
        }
        // 4.校验token
        Long userId;
        try {
            userId = jwtTool.parseToken(token);
        } catch (Exception e) {
            // 设置响应状态码为401
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // 5.传递用户信息
        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("user-info", userId.toString()))
                .build();
        // 6.放行
        return chain.filter(swe);
    }
    // 放行方法
    private boolean isExclude(String path) {
        for (String pathPattern : authProperties.getExcludePaths()) {
            if (matcher.match(pathPattern, path)) {
              return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
