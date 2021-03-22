package com.lianggao.dao;

import com.lianggao.bean.Application;

import java.util.List;

public interface ApplicationMapper {

    int insert(Application record);
    Application selectByBusinessKey(Integer businessKey);
    List<Application> getUserApplication(int userid);
    void setApplicationState(Application application);
}