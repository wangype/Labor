package com.labor.dao;

import com.labor.DaoMaster;
import com.labor.entity.UserEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by wyp on 15-7-2.
 */
@DaoMaster
public interface UserMapper {

    public UserEntity selectUserByID(int id);

    public UserEntity login(String userName, String passWord);



}
