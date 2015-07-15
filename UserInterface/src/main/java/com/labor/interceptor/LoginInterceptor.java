package com.labor.interceptor;

import com.labor.common.Constant;
import com.labor.model.UserModel;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by wyp on 15-7-13.
 */
public class LoginInterceptor implements HandlerInterceptor {

    private final static String LOGIN_URL = "/pages/login.html";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        if (request.getRequestURI().indexOf("login") != -1) {
            return true;
        }
        HttpSession session=request.getSession();
        UserModel userModel = (UserModel) session.getAttribute(Constant.USERSESSION);
        if (userModel == null) {
            response.sendRedirect(LOGIN_URL);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
