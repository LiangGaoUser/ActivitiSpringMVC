package com.lianggao.service;

import com.lianggao.bean.Application;
import com.lianggao.bean.ApplicationInstance;
import com.lianggao.bean.UserInfo;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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



    //////////////////////////////////////////////////////////////////////////////////////////////
    //获取危险作业类型
    public Map<String,Object> getCategory();
    public Map[] getAlltaskListInfo(String conditions, int pageindex, String username, String UserInstitution) throws SQLException;
    public Map[] getAlltaskList(String conditions, String username, String UserInstitution) throws SQLException;
    public int insertApplicationTask(String ApplicantNum, String TaskInstitution, String ApplyingTime, String StartTime, String EndTime, String DangerTaskName, String Category, String Archived, String uploadfilename, String ApproverList,String DangerTaskLevel,String DangerTaskNum);
    public Map[] getTaskInfo(String taskID);
    public List<Map<String,Object>> getTaskList(String tasklist) throws Exception;
    public List<Map<String,Object>> getApplicant(String tasklist) throws Exception;
    public int Edit_submitApplicantTask(String ApplicantNum, String TaskInstitution, String ApplyingTime, String StartTime, String EndTime, String DangerTaskName, String Category, String Archived, String uploadfilename, String State, String ApproverList, String TaskID,String DangerTaskLevel,String DangerTaskNum);
    public int updateApplicationTask(String ApplicantNum, String TaskInstitution, String ApplyingTime, String StartTime, String EndTime, String DangerTaskName, String Category, String Archived, String uploadfilename, String State, String ApproverList, String TaskID,String DangerTaskLevel,String DangerTaskNum) throws Exception;
    public int insertApplicationTask2(String ApplicantNum, String TaskInstitution, String ApplyingTime, String StartTime, String EndTime, String DangerTaskName, String Category, String Archived, String uploadfilename, String ApproverList,String DangerTaskLevel,String DangerTaskNum);
    public int Add_submitApplicantTask(String ApplicantNum, String ApproverList, String TaskID);
    //////////////////////////////////////////////////////////////////////////////////////////////
    public Map[] getAllApprovalListInfo(String conditions, int pageindex, String username, String UserInstitution) throws SQLException;
    public Map[] getAllApprovalList(String conditions,String username) throws SQLException;
    public List<Map<String, Object>> getApplyInfo(String ApprovalID) throws Exception ;
    public Map[] getSubmitter(String ApprovalID);
    public int Approve(String ApprovalID, String TaskID, String Approver, String ApproverList, String ApprovalResult,String ApproveTime,String ApprovalFinished, String ApproveSuggestion,String ApproveSignature,String flag);
    public Map[] getrelayPeopleList(String InstitutionNum, String usernum)throws Exception;
    public int replayApplication(String[] ApprovalIDList, String username, String relayPeople, String ApproveSuggestion);
    public int returnApplication(String ApprovalID, String SubmitterID, String Applicant, String ApproveSuggestion, String Finished);
}
