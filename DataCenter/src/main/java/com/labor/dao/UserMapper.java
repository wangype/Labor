package com.labor.dao;

import com.labor.DaoMaster;
import com.labor.entity.UserEntity;

/**用户数据操作
 * Created by wyp on 15-7-2.
 */
@DaoMaster
public interface UserMapper {

    public UserEntity selectUserByID(int id);

    public UserEntity login(String userName, String passWord);



}
