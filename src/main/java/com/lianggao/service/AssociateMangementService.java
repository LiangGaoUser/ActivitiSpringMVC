package com.lianggao.service;

import com.lianggao.bean.Application;
import com.lianggao.bean.ApplicationInstance;
import com.lianggao.bean.UserInfo;

import java.util.List;

public interface AssociateMangementService {
    /**
     * 添加申请任务
     * Application application:申请实体;String ApproverList：申请人列表,比如a1, a2, a3
     * String:返回启动实例id
     */
    String insertApplicationTask(Application application, List<String> ApproverList);
    /**
     * 添加申请任务
     * Application application:申请实体;List<UserInfo> ApproverList：申请人列表;String BusinessKey:表中DangerTaskId
     * String:返回启动实例id
     */
    String insertApplicationTaskByUserList(UserInfo userInfo, Application application, List<UserInfo> ApproverList, String BusinessKey);
    /**
     * 查看所有的申请
     *
     */
    List<ApplicationInstance>getAllApprovalListInfo();
    /**
     * 根据BusinessKey查询得到申请记录
     * String BusinessKey:
     * Application:返回的记录
     */
    Application getApplicationByBusinessKey(int BusinessKey);
    /**
     * 根据登录的用户，查询所有该用户待审批的任务
     * String userid:待查询的用户id
     * List<ApplicationInstance>:用户待审批的任务
     */
    List<ApplicationInstance> getUserTask(String userid);
    /**
     * 查看个人的所有申请
     * String userid:用户的id
     * List<Application>:查询返回的申请
     */
    List<Application> getUserApplication(String userid);
    /**
     * 审批人处理申请.根据实例号和用户id找到用户当前的任务进行完成
     * String InstanceId:实例id;UserInfo userInfo:用户信息;String comment:用户评论
     */
    void finishMyTask(String InstanceId, UserInfo userInfo, String comment);
    /**
     *查看该实例的所有审批信息，包括审批人员的名称，审批结束时间，审批意见
     */
    ApplicationInstance getSingleApproval(String InstanceId);
    /**
     * 得到所有审批人的审批意见
     * String InstanceId:实例id
     */
    List<String> getSingleApprovalComment(String InstanceId);
    /**
     * 得到所有审批人的审批意见时间
     * String InstanceId:实例id
     */
    List<String> getSingleApprovalCommentTime(String InstanceId);
    /**
     * 转发,将待审批的任务转发给不再审批列表中的其他人
     * String InstanceId:实例, String userid:用户id; UserInfo userInfo:转发的用户
     */
    void forward(String InstanceId, String userid, UserInfo userInfo);
    /**
     *最后一个审批人动态增加审批人,不需要增加申请Application,BusinessKey可以通过被复制的实例获得
     */
    String insertApplicationTaskByCopy(UserInfo userInfo, List<UserInfo> ApproverList, String BusinessKey);
    /**
     * 添加审批人
     */
    public void addInstanceNode(String InstanceId, List<UserInfo> userInfoList);
}
