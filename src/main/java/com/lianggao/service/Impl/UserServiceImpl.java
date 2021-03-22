package com.lianggao.service.Impl;
import com.lianggao.bean.UserInfo;
import com.lianggao.dao.UserInfoMapper;
import com.lianggao.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserInfoMapper userMapper;

    @Override
    public UserInfo login(UserInfo user) {


        System.out.println(user.toString());
        UserInfo userInfo=userMapper.selectByNameAndPassword(user);
        if(userInfo!=null){
            System.out.println("has used========================================");
            System.out.println(userInfo.toString());
            return userInfo;
        }
        System.out.println("has used2========================================");
        //List<User> userList =new ArrayList<>();
        return null;
    }
}
