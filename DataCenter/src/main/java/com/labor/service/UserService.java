package com.labor.service;

import com.labor.MD5Utils;
import com.labor.entity.UserEntity;
import com.labor.dao.UserMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

/**
 * Created by wyp on 15-7-2.
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userOperation;

    private Logger logger = Logger.getLogger(UserService.class);

    public UserEntity getUserById(int id) {
        return userOperation.selectUserByID(id);
    }

    public UserEntity login(String userName, String passWord) {
        String passStr = null;
        try {
            passStr = MD5Utils.string2MD5(passWord);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage());
            return null;
        }
        return userOperation.login(userName, passStr);
    }

}
