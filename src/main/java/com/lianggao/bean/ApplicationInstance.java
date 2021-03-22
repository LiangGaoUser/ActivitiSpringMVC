package com.lianggao.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 在Application基础上增加InstanceId即每条申请记录对应的实例名称，和对应的审核人员组成的列表
 */
public class ApplicationInstance implements Serializable{
    private Application application;
    private String InstanceId;
    private List<UserInfo> approveList;
    private List<String>commentList;
    private List<String> timeList;
    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getInstanceId() {
        return InstanceId;
    }

    public void setInstanceId(String instanceId) {
        InstanceId = instanceId;
    }

    public List<UserInfo> getApproveList() {
        return approveList;
    }

    public void setApproveList(List<UserInfo> approveList) {
        this.approveList = approveList;
    }

    public List<String> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<String> commentList) {
        this.commentList = commentList;
    }

    public List<String> getTimeList() {
        return timeList;
    }

    public void setTimeList(List<String> timeList) {
        this.timeList = timeList;
    }

    @Override
    public String toString() {
        return "ApplicationInstance{" +
                "application=" + application +
                ", InstanceId='" + InstanceId + '\'' +
                ", approveList=" + approveList +
                ", commentList=" + commentList +
                ", timeList=" + timeList +
                '}';
    }
}
