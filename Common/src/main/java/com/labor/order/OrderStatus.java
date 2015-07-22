package com.labor.order;

/**订单类型
 * Created by wyp on 15-7-22.
 */
public enum OrderStatus {

    //未确认， 已确认,已付款 , 已发货, 已完成 , 已取消, 已退货
    UNCONFIRMED(1), CONFIRMED(2), PAID(3), DELIVERED(4), FINISHED(5), CANCELED(6), RETURNED(7);


    private int status;

    OrderStatus(int status) {
        this.status = status;
    }


    public int getStatus(){
        return status;
    }


}
