package com.labor.dao;

import com.labor.entity.UserEntity;
import org.apache.ibatis.annotations.Param;

/**
 * Created by wyp on 15-7-2.
 */
public interface IUserOperation {

    public UserEntity selectUserByID(int id);

    public UserEntity login(@Param("userName")String userName, @Param("passWord")String passWord);



}
