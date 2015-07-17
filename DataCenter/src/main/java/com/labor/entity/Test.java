package com.labor.entity;

import com.labor.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by wyp on 15-7-2.
 */
public class Test {


    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-mybatis.xml");
        //this.context = new FileSystemXmlApplicationContext("WebRoot/WEB-INF/spring-mybatis.xml");
        UserService userService = context.getBean(UserService.class);
        UserEntity user = userService.getUserById(1);
//        System.out.println(user.getName());
    }

}
