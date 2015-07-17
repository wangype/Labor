package com.labor.model;

import java.io.Serializable;

/**用户model
 * Created by wyp on 15-7-15.
 */
public class UserModel implements Serializable{

    private String userName;
    private String eMail;
    private String phone;
    private int userType;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }
}
