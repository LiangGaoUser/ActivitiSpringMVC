package com.lianggao.controller;

//import com.lianggao.service.UserService;
import com.lianggao.bean.UserInfo;
import com.lianggao.service.UserService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;



@Controller
@Scope("prototype")
public class UserController {
    @Autowired
    private UserService userService;
    @RequestMapping("userAction_login")
    public String login(HttpSession session, Model model, String userName, String userPassword) {
        System.out.println("enter login");
        UserInfo userInfo2 = new UserInfo();
        userInfo2.setUserName(userName);
        userInfo2.setUserPassword(userPassword);
        System.out.println(userInfo2.toString());
        UserInfo userInfo = userService.login(userInfo2);

        //登录成功
        if (userInfo!=null) {
            System.out.println(userInfo.toString());
            session.setAttribute("activeUser", userInfo);
            //将用户信息存入到session中
            System.out.println("======================================");
            System.out.println(userInfo.toString());
            System.out.println("login success");
            return "/home/main";
        } else {
            //登录失败
//设置错误提示信息
            System.out.println("======================================");
            System.out.println("login failed");
            model.addAttribute("msg", "用户名或密码错误！");
            return "/home/login_failed";
        }
    }

}
