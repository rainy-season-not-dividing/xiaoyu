package com.xiaoyua.websocket;

import com.xiaoyua.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器
 * 用于JWT认证
 *
 * @author xiaoyu
 */
@Component
@Slf4j
public class WebSocketInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        String uri = request.getURI().toString();
        log.info("WebSocket握手请求: {}", uri);

        String token = null;

        // 优先从请求头中获取token (Bearer Token方式)
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // 移除 "Bearer " 前缀
            log.info("从Authorization头获取到token");
        }

        // 如果请求头中没有token，尝试从WebSocket子协议中获取
        if (token == null) {
            String secWebSocketProtocol = request.getHeaders().getFirst("Sec-WebSocket-Protocol");
            if (secWebSocketProtocol != null && secWebSocketProtocol.startsWith("Bearer.")) {
                token = secWebSocketProtocol.substring(7); // 移除 "Bearer." 前缀
                log.info("从WebSocket子协议获取到token");
                // 设置响应头，告诉客户端使用的子协议
                response.getHeaders().add("Sec-WebSocket-Protocol", "Bearer." + token.substring(0, Math.min(10, token.length())) + "...");
            }
        }

        // 如果请求头中没有token，尝试从查询参数中获取 (兼容旧方式)
        if (token == null) {
            String query = request.getURI().getQuery();
            if (query != null && query.contains("token=")) {
                token = extractToken(query);
                log.info("从查询参数获取到token");
            }
        }

        if (token != null) {
            try {
                // 验证JWT token
                Long userId = jwtUtil.getUserIdFromToken(token);
                if (userId != null) {
                    // 将用户ID存储到WebSocket会话属性中
                    attributes.put("userId", userId);
                    log.info("WebSocket认证成功，用户ID: {}", userId);
                    return true;
                }
            } catch (Exception e) {
                log.error("WebSocket JWT验证失败: {}", e.getMessage());
            }
        }

        log.warn("WebSocket认证失败，拒绝连接");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket握手后异常: {}", exception.getMessage());
        }
    }

    /**
     * 从查询字符串中提取token
     */
    private String extractToken(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6); // "token=".length()
            }
        }
        return null;
    }
}
