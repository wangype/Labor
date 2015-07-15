package com.labor.action;

import com.labor.common.Constant;
import com.labor.common.ReturnStatus;
import com.labor.entity.UserEntity;
import com.labor.model.UserModel;
import com.labor.request.UserLoginRequest;
import com.labor.response.RetResponse;
import com.labor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by wyp on 15-7-13.
 */
@RestController
@RequestMapping("/userinfo")
public class LoginAction {

    @Autowired
    private UserService userService;


    @RequestMapping(value = "login/{userName}", method = RequestMethod.GET)
    @ResponseBody
    public RetResponse login(@PathVariable("userName") String userName,
                        @RequestBody UserLoginRequest userLoginRequest,
                        HttpServletRequest request){
        RetResponse retResponse = new RetResponse();
        if (userLoginRequest == null || userLoginRequest.getPassWord() == null) {
            retResponse.setStatus(ReturnStatus.FATAL);
            return retResponse;
        }
        UserEntity userEntity = userService.login(userName, userLoginRequest.getPassWord());
        if (userEntity == null) {
            retResponse.setStatus(ReturnStatus.LOGIN_FAIL);
            return retResponse;
        }
        request.getSession().setAttribute(Constant.USERSESSION, new UserModel());
        retResponse.setStatus(ReturnStatus.LOGIN_SUCCESS);
        return retResponse;
    }

}
