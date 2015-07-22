package com.labor.dao;

import com.labor.DaoMaster;

/** 订单数据库操作
 * Created by wyp on 15-7-22.
 */
@DaoMaster
public interface OrderMapper {

    public int countOrderNumByType(int orderStatus);


}
