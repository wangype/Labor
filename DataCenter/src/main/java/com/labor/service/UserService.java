package com.labor.service;

import com.labor.entity.UserEntity;
import com.labor.operation.IUserOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wyp on 15-7-2.
 */
@Service
public class UserService {

    @Autowired
    private IUserOperation userOperation;

    public UserEntity getUserById(int id) {
        return userOperation.selectUserByID(id);
    }

    public UserEntity login(String userName, String passWord){
        return userOperation.login(userName, passWord);
    }

}
