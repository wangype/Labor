package com.labor.action;

import com.labor.model.User;
import com.labor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by wyp on 15-7-1.
 */
@RestController
@RequestMapping("/userinfo")
public class TestAction {

    @Autowired
    private UserService userOperation;

    @RequestMapping(value = "user/{id}", method = RequestMethod.GET)
    @ResponseBody
    public User getUser(@PathVariable("id") int id){
        User user = userOperation.getUserById(id);
        return user;
    }


    @RequestMapping(value = "user/{userName}/{passWord}", method = RequestMethod.GET)
    @ResponseBody
    public User login(@PathVariable("userName") String userName,
                      @PathVariable("passWord") String passWord){
        User user = userOperation.login(userName, passWord);
        return user;
    }


}
