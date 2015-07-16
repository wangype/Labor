package com.labor.utils.convert;

import com.labor.entity.UserEntity;
import com.labor.model.UserModel;

/**
 * Created by wyp on 15-7-16.
 */
public class UserEntityToModel {


    public UserModel convertEntity(UserEntity userEntity){
        UserModel userModel = new UserModel();
        userModel.setUserName(userEntity.getUserName());
        userModel.seteMail(userEntity.geteMail());
        userModel.setPhone(userEntity.getPhoneNum());
        return userModel;
    }


}
