package com.labor.action;

import com.alibaba.fastjson.JSON;
import com.labor.common.Constant;
import com.labor.common.ReturnStatus;
import com.labor.entity.UserEntity;
import com.labor.model.UserModel;
import com.labor.request.UserLoginRequest;
import com.labor.response.RetResponse;
import com.labor.service.UserService;
import com.labor.utils.ConvertToolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**登录controller
 * Created by wyp on 15-7-13.
 */
@RestController
@RequestMapping("/userinfo")
public class LoginController {

    @Autowired
    private UserService userService;


    @RequestMapping(value = "login/{userName}", method = RequestMethod.GET)
    @ResponseBody
    public RetResponse login(@PathVariable("userName") String userName,
                        @RequestBody String passWordRequest,
                        HttpServletRequest request){
        RetResponse retResponse = new RetResponse();
        UserLoginRequest userLoginRequest = JSON.parseObject(passWordRequest, UserLoginRequest.class);
        if (userLoginRequest == null || userLoginRequest.getPassWord() == null) {
            retResponse.setStatus(ReturnStatus.UNKOWN_ERROR);
            return retResponse;
        }
        UserEntity userEntity = userService.login(userName, userLoginRequest.getPassWord());
        if (userEntity == null) {
            retResponse.setStatus(ReturnStatus.LOGIN_FAIL);
            return retResponse;
        }
        UserModel userModel = ConvertToolUtils.userEntityToModel.convertEntity(userEntity);
        request.getSession().setAttribute(Constant.USERSESSION, userModel);
        retResponse.setStatus(ReturnStatus.LOGIN_SUCCESS);
        retResponse.setObject(userModel);
        return retResponse;
    }


}
