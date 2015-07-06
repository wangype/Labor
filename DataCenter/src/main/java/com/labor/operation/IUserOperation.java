package com.labor.operation;

import com.labor.model.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by wyp on 15-7-2.
 */
public interface IUserOperation {

    @Select("select * from user where id=#{id}")
    public User selectUserByID(int id);

    @Select("select * from user where user_name=#{userName} and password=#{passWord}")
    public User login(@Param("userName")String userName, @Param("passWord")String passWord);



}
