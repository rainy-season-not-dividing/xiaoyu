package com.xiaoyu.interceptor;

import com.xiaoyu.context.BaseContext;
import com.xiaoyua.properties.JwtProperties;
import com.xiaoyua.utils.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * 登录验证拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor_yuji implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Resource
    private JwtUtil jwtUtil;



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        log.info("进行登录验证...");
        log.info("ServletPath: {}", request.getServletPath());

        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        // 对于 /posts 路径，GET 方法放行，其他方法需要验证
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        if (("/posts".equals(requestURI) || "/tasks".equals(requestURI)) && "GET".equalsIgnoreCase(method)) {
            return true; // 直接放行
        }

        //1、从请求头中获取令牌  -- token
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);


            Long empId = jwtUtil.getUserIdFromToken( token);

            log.info("当前员工id：{}", empId);


            //设置当前登录用户的ID
            BaseContext.setCurrentId(empId);
            com.xiaoyua.context.BaseContext.setCurrentId(empId);


            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.clear();
        com.xiaoyua.context.BaseContext.clearCurrentId();
    }
}
