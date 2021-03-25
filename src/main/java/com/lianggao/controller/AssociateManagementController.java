package com.lianggao.controller;

import cn.jpush.api.push.PushResult;
import com.lianggao.bean.Application;
import com.lianggao.bean.ApplicationInstance;
import com.lianggao.bean.UserInfo;
import com.lianggao.dao.ApplicationMapper;
import com.lianggao.dao.UserInfoMapper;
import com.lianggao.service.AssociateMangementService;
import com.lianggao.utils.*;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;




import net.sf.json.JSONArray;
/**
 * 管理危险作业相关
 * 1.提交危险作业申请 2.
 */

/*@Scope("prototype")*/
@Controller
@RequestMapping("/AssociateManagement")
public class AssociateManagementController {
    private DBo dBo = new DBo();
    private String DangerTaskApplyManPath;
    private String TaskApproveManPath;
    @Autowired
    private AssociateMangementService associateMangementService;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private UserInfoMapper userInfoMapper;
    /*@RequestMapping("/DangerTaskApplyMan/SubmitAdd")
    public String SubmitAdd(HttpServletRequest request, HttpSession httpSession, HttpServletResponse response){
        UserInfo userInfo = (UserInfo)httpSession.getAttribute("activeUser");
        String dangertaskname = request.getParameter("dangertaskname").toString();
        int applicant = userInfo.getUserId();
        String starttime = request.getParameter("starttime").toString();
        String endtime = request.getParameter("endtime").toString();
        String filename = request.getParameter("filename").toString();
        String approveString = request.getParameter("approvelist").toString();
        String [] arr = approveString.split("\\s+");


        Application application = new Application();
        application.setDangerTaskName(dangertaskname);
        application.setApplicant(applicant);
        application.setStartTime(starttime);
        application.setEndTime(endtime);
        application.setFileName(filename);
        application.setState(0);
        System.out.println(application.toString());

        //将申请信息插入数据库
        String BusinessKey = String.valueOf(applicationMapper.insert(application));
        System.out.println("=====================插入结果"+BusinessKey);

        List<UserInfo> userInfoList = new ArrayList<>();
        for(String ss : arr){
            UserInfo userInfo1 = new UserInfo();
            userInfo1 = userInfoMapper.selectByPrimaryKey(Integer.parseInt(ss));
            userInfoList.add(userInfo1);
        }

        for(UserInfo userInfo1:userInfoList){
            System.out.println("=================="+userInfo1.getUserName()+userInfo1.getUserPassword());
        }
        String result = associateMangementService.insertApplicationTaskByUserList(userInfo,application, userInfoList, BusinessKey);
        if(result.equals("insertApplicationTask_failed")){
            System.out.println("===================启动实例失败");
            return "/home/login_failed";
        }else {
            System.out.println("===================启动实例成功");
            return "/home/main";
        }
    }*/
    @RequestMapping("/DangerTaskApplyMan/GetAllInstances")
    public String GetAllInstances(HttpServletRequest request, HttpSession httpSession, HttpServletResponse response){
        System.out.println("开始查询所有实例");
        List<ApplicationInstance>applicationInstanceList = associateMangementService.getAllApprovalListInfo();
        for(ApplicationInstance applicationInstance:applicationInstanceList){
            System.out.println("每个实例对应的情况"+applicationInstance.toString());
        }
        request.setAttribute("applicationInstanceList", applicationInstanceList);

        List<Application> applicationList = new ArrayList<>();
        Application application = new Application();
        application.setFileName("file1");
        applicationList.add(application);
        Application application2 = new Application();
        application2.setFileName("file2");
        applicationList.add(application2);
        request.setAttribute("list",applicationList);
        return "/flow/allApplication";
    }
    @RequestMapping("/DangerTaskApplyMan/GetUserTasks")
    public String GetUserTasks(HttpServletRequest request, HttpSession httpSession, HttpServletResponse response){

        UserInfo userInfo = (UserInfo)httpSession.getAttribute("activeUser");
        System.out.println("==============该用户id "+userInfo.getUserId());
        List<ApplicationInstance>applicationInstanceList = associateMangementService.getUserTask(userInfo.getUserId().toString());
        for(ApplicationInstance applicationInstance:applicationInstanceList){
            System.out.println("该用户需要审批的任务"+applicationInstance.toString());
        }
        request.setAttribute("userTaskList", applicationInstanceList);
        return "/flow/userTask";
    }
    @RequestMapping("/DangerTaskApplyMan/GetUserApplications")
    public String GetUserApplications(HttpServletRequest request, HttpSession httpSession, HttpServletResponse response){
        UserInfo userInfo = (UserInfo)httpSession.getAttribute("activeUser");
        List<Application>applicationList  = associateMangementService.getUserApplication(userInfo.getUserId().toString());
        request.setAttribute("userApplicationList", applicationList);
        return "/flow/allUserApplication";

    }
    @RequestMapping("/DangerTaskApplyMan/FinishOneTask")
    public String FinishOneTask(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, HttpSession httpSession){

        String Comment = httpServletRequest.getParameter("Comment");
        String Approve = httpServletRequest.getParameter("Approve");
        String InstanceId = httpServletRequest.getParameter("InstanceId");
        if(Comment==""){
            Comment = "暂无审批意见";
        }
        if(Approve.equals("no")){
            UserInfo userInfo = (UserInfo)httpSession.getAttribute("activeUser");
            associateMangementService.finishMyTask(InstanceId, userInfo, Comment);
            ProcessInstance processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(InstanceId).singleResult();
            processEngine.getRuntimeService().suspendProcessInstanceById(processInstance.getId());
            Application application = new Application();
            application.setState(3);
            applicationMapper.setApplicationState(application);
            System.out.println("该申请不同意，该实例已经挂起");
        }else{
            System.out.println("======================="+Comment+Approve+InstanceId);
            UserInfo userInfo = (UserInfo)httpSession.getAttribute("activeUser");
            associateMangementService.finishMyTask(InstanceId, userInfo, Comment);
        }



        return "redirect:/DangerTaskApplyMan/GetUserTasks.do";
    }
    @RequestMapping("/DangerTaskApplyMan/GoApprove")
    public String GoApprove(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, HttpSession httpSession){
        String InstanceId = httpServletRequest.getParameter("InstanceId").toString();
        httpServletRequest.setAttribute("InstanceId", InstanceId);
        return "/flow/approve";
    }
    @RequestMapping("/DangerTaskApplyMan/GetSingelApproveInformation")
    public String GetSingelApproveInformation(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, HttpSession httpSession){
        String InstanceId = httpServletRequest.getParameter("InstanceId").toString();
        ApplicationInstance applicationInstance = associateMangementService.getSingleApproval(InstanceId);
        httpServletRequest.setAttribute("applicationInstance", applicationInstance);
        return "/flow/approveInformation";
    }
    @RequestMapping("/DangerTaskApplyMan/GoForwardTask")
    public String GoForwardTask(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, HttpSession httpSession){
        String InstanceId = httpServletRequest.getParameter("InstanceId").toString();
        httpServletRequest.setAttribute("InstanceId", InstanceId);
        return "/flow/forward";
    }
    @RequestMapping("/DangerTaskApplyMan/ForwardTask")
    public String ForwardTask(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, HttpSession httpSession){
        int userid = Integer.parseInt(httpServletRequest.getParameter("userid").toString());
        String instanceid = httpServletRequest.getParameter("InstanceId").toString();
        UserInfo userInfoActive = (UserInfo)httpSession.getAttribute("activeUser");
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userid);
        associateMangementService.forward(instanceid, userInfoActive.getUserId().toString(), userInfo);
        return "redirect:/DangerTaskApplyMan/GetUserTasks.do";
    }
    /*    @RequestMapping("/DangerTaskApplyMan/AddApproval")
    public String AddApproval(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, HttpSession httpSession){
        String instanceid = httpServletRequest.getParameter("InstanceId").toString();
        associateMangementService.addInstanceNode(instanceid);
        System.out.println("==============================删除该实例");
        processEngine.getRuntimeService().deleteProcessInstance(instanceid, "deleted");
        System.out.println("==============================删除该实例成功");
        return "redirect:/DangerTaskApplyMan/GetUserTasks.do";
    }*/
    @RequestMapping("/DangerTaskApplyMan/AddApproval")
    public String AddApproval(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, HttpSession httpSession){
        String instanceid = httpServletRequest.getParameter("InstanceId").toString();
        String approveString = httpServletRequest.getParameter("approvelist").toString();
        String [] arr = approveString.split("\\s+");
        List<UserInfo>userInfoList = new ArrayList<>();
        for(String ss : arr){
            UserInfo userInfo1 = new UserInfo();
            userInfo1 = userInfoMapper.selectByPrimaryKey(Integer.parseInt(ss));
            userInfoList.add(userInfo1);
        }

        associateMangementService.addInstanceNode(instanceid, userInfoList);
        System.out.println("==============================删除该实例");
        processEngine.getRuntimeService().deleteProcessInstance(instanceid, "deleted");
        System.out.println("==============================删除该实例成功");
        return "redirect:/DangerTaskApplyMan/GetUserTasks.do";
    }
    @RequestMapping("/DangerTaskApplyMan/ToAddApproval")
    public String ToAddApproval(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, HttpSession httpSession){
        String instanceid = httpServletRequest.getParameter("InstanceId").toString();
        httpServletRequest.setAttribute("InstanceId", instanceid);
        return "/flow/addApproveUI";
    }









    public String insertApplicationTask(Application application, List<String> ApproverList) {
        try{
            //String Applicant = "a"+application.getApplicant().toString();
            String Applicant = "a1";
            // 创建开始
            StartEvent startEvent = new StartEvent();
            startEvent.setId("startEvent");
            startEvent.setName("startEvent");
            System.out.println("------>startEvent");
            // 创建危险作业申请
            UserTask applyTask = new UserTask();
            applyTask.setId(Applicant);
            applyTask.setName(Applicant);
            applyTask.setAssignee(Applicant);
            System.out.println("------>a1");
            // 创建次级审批成员
            List<UserTask> userTaskList = createUserTaskList(ApproverList);
            //System.out.println("------>size: "+userTaskList.size());
            // 创建结束点
            EndEvent endEvent = new EndEvent();
            endEvent.setId("endEvent");
            endEvent.setName("endEvent");
            System.out.println("------>endEvent");
            System.out.println("=================================");
            // 创建连线: startEvent->用户申请
            SequenceFlow s1 = new SequenceFlow();
            s1.setId("s1");
            s1.setName("s1");
            s1.setSourceRef("startEvent");
            s1.setTargetRef(Applicant);
            System.out.println("tartEvent->a1");
            List<SequenceFlow> sequenceFlowList = createSequenceFlowList(Applicant, ApproverList);
            System.out.println("创建连线成功 sequenceFlowList size"+sequenceFlowList.size());
            // 连接Task
            List<SequenceFlow> sequenceFlowList1 = new ArrayList<>();
            sequenceFlowList1.add(s1);
            startEvent.setOutgoingFlows(sequenceFlowList1);
            applyTask.setIncomingFlows(sequenceFlowList1);
            System.out.println("=================================");
            System.out.println("startEvent-->a1");
            int userTaskNumber = 0;
            int sequenceFlowNumber = 1;
            System.out.println("sequenceFlowList size"+sequenceFlowList.size());
            for(SequenceFlow sequenceFlow:sequenceFlowList){
                if(sequenceFlowNumber==1){//包含申请节点
                    List<SequenceFlow> currentSequenceFlow = new ArrayList<>();
                    currentSequenceFlow.add(sequenceFlow);
                    applyTask.setOutgoingFlows(currentSequenceFlow);
                    userTaskList.get(userTaskNumber).setIncomingFlows(currentSequenceFlow);
                    System.out.println("a1-->"+userTaskList.get(userTaskNumber).getAssignee());
                }else if(sequenceFlowNumber==sequenceFlowList.size()){//最后一个节点
                    List<SequenceFlow> currentSequenceFlow = new ArrayList<>();
                    currentSequenceFlow.add(sequenceFlow);
                    userTaskList.get(userTaskNumber-1).setOutgoingFlows(currentSequenceFlow);
                    endEvent.setIncomingFlows(currentSequenceFlow);
                    System.out.println(userTaskList.get(userTaskNumber-1).getAssignee()+"-->EndEvent");
                }else{
                    List<SequenceFlow> currentSequenceFlow = new ArrayList<>();
                    currentSequenceFlow.add(sequenceFlow);
                    userTaskList.get(userTaskNumber-1).setOutgoingFlows(currentSequenceFlow);
                    userTaskList.get(userTaskNumber).setIncomingFlows(currentSequenceFlow);
                    System.out.println(userTaskList.get(userTaskNumber-1).getAssignee()+"-->"+userTaskList.get(userTaskNumber).getAssignee());
                }
                userTaskNumber++;
                sequenceFlowNumber++;
            }

            System.out.println("完成进行进出设置");
            // 创建流程
            org.activiti.bpmn.model.Process process = new Process();
            process.setName("Apply");
            process.setId("Apply");
            System.out.println("=================================");
            process.addFlowElement(startEvent);
            System.out.println("------>startEvent");
            process.addFlowElement(applyTask);
            System.out.println("------>a1");
            for(UserTask userTask: userTaskList){
                process.addFlowElement(userTask);
                System.out.println("------>"+userTask.getAssignee());
            }
            process.addFlowElement(endEvent);
            System.out.println("------>endEvent");
            process.addFlowElement(s1);
            System.out.println("------>s1");
            for(SequenceFlow sequenceFlow: sequenceFlowList){
                process.addFlowElement(sequenceFlow);
                System.out.println("------>"+sequenceFlow.getId());
            }
            // 创建Bpmnmodel
            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addProcess(process);
            System.out.println("开始部署");
            org.activiti.engine.repository.Deployment deployment = processEngine.getRepositoryService().createDeployment()
                    .name("bpmn")
                    .addBpmnModel("Apply.bpmn", bpmnModel) // 这个addBpmnModel第一个参数一定要带后缀.bpmn
                    .deploy();
            System.out.println("部署完成");
            System.out.println(deployment.getId()+" "+deployment.getName()+" "+deployment.getTenantId());
            System.out.println("==============");
            return startProcessByID(deployment.getId());
        }catch (Exception e){
            return "insertApplicationTask_failed";
        }

    }
    /**
     * 根据危险作业审批人列表创建审批节点列表
     * List<String>ApproverList:危险作业审批人列表;List<UserTask>:工作流审批节点列表
     * List<UserTask>:返回任务列表
     */
    public List<UserTask> createUserTaskList(List<String>ApproverList){
        List<UserTask> userTaskList = new ArrayList<>();
        for(String approver: ApproverList){
            UserTask userTask = new UserTask();
            userTask.setId(approver);
            userTask.setName(approver);
            userTask.setAssignee(approver);
            userTaskList.add(userTask);
            System.out.println("------>"+userTask.getAssignee());
        }
        return userTaskList;
    }
    /**
     * 创建连线,用户申请->审批人列表->结束节点
     * String applicant:申请人;List<Integer>ApproverList:危险作业审批人列表
     * List<SequenceFlow>:SequenceFlow列表
     */
    public List<SequenceFlow> createSequenceFlowList(String applicant, List<String>ApproverList){
        List<SequenceFlow> sequenceFlowList = new ArrayList<>();
        int sequenceNumber = 1;
        String lastSequenceFlow = " ";
        for(String approver: ApproverList){
            SequenceFlow sequenceFlow = new SequenceFlow();
            if(sequenceNumber == 1){
                sequenceFlow.setId("s"+approver);
                sequenceFlow.setName(approver);
                sequenceFlow.setSourceRef(applicant);
                sequenceFlow.setTargetRef(approver);
                System.out.println(applicant+"-->"+approver);
                lastSequenceFlow =  approver;

            }else{

                sequenceFlow.setId("s"+approver);
                sequenceFlow.setName(approver);
                sequenceFlow.setSourceRef(lastSequenceFlow);
                sequenceFlow.setTargetRef(approver);
                System.out.println(lastSequenceFlow+"-->"+approver);
                lastSequenceFlow =  approver;

            }
            sequenceNumber++;
            sequenceFlowList.add(sequenceFlow);
        }

        SequenceFlow sequenceFlow = new SequenceFlow();
        sequenceFlow.setId("s"+lastSequenceFlow);
        sequenceFlow.setName(lastSequenceFlow);
        sequenceFlow.setSourceRef(lastSequenceFlow);
        sequenceFlow.setTargetRef("endEvent");
        System.out.println(lastSequenceFlow+"-->"+"endEvent");
        return sequenceFlowList;
    }
    /**
     * 根据deployment_Id找到procdef_Id,根据act_re_procedef中的ID进行启动
     * String deploymentID:部署id
     * String:返回InstanceID
     */
    public String startProcessByID(String deploymentID){
        String processKey="Fifth";
        //150001
        Application application = new Application();


        //这里需要存入Application表里面,返回得到BusinessKey
        Map<String, Object> variables = new HashMap<>();
        variables.put("application", application);

        ProcessDefinition processDefinition = processEngine.getRepositoryService().createProcessDefinitionQuery().deploymentId(deploymentID).singleResult();
        System.out.println(processDefinition.getId());




        String BusinessKey = "123";//存储申请表中的id
        //部署完成后会有一个部署ID
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceById(processDefinition.getId(), BusinessKey, variables);
        if(processInstance!=null){
            System.out.println(processInstance.getId()+"启动成功");
            return  processInstance.getId();
        }
        return " ";

    }






    //原来项目跳转所需要的方法
    //主页面
    @RequestMapping(value = "/DangerTaskApplyMan/DangerTaskList_vue")
    public ModelAndView InstitutionList(HttpServletRequest request) {

        DangerTaskApplyManPath = request.getServletContext().getRealPath("/") + "upload/AssociateManagement/DangerTaskApplyMan/";
        System.out.println("进入了DangerTaskList_vue"+DangerTaskApplyManPath);
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/AssociateManagement/DangerTaskApplyMan/dangerTaskApply");
        return mv;
    }
    //待批作业
    @RequestMapping(value = "/TaskApproveMan/Approval_Vue")
    public ModelAndView getApprovalList(HttpServletRequest request) {
        TaskApproveManPath = request.getServletContext().getRealPath("/") + "upload/AssociateManagement/DangerTaskApplyMan/";
        System.out.println("Approval_Vue"+TaskApproveManPath);
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/AssociateManagement/TaskApproveMan/dangerTaskApproval");
        return mv;
    }
    //绑定下拉列表数据
    @RequestMapping("/DangerTaskApplyMan/getInfoList")
    @ResponseBody
    public Map<String, Object> getInfoList(HttpSession session, HttpServletRequest request) throws Exception {

        String DangerTaskApplyManPath = request.getServletContext().getRealPath("/") + "upload/AssociateManagement/DangerTaskApplyMan/";
        System.out.println("进入了getInfoList, 返回查询相关选项");
        String UserInstitution = session.getAttribute("UserInstitution").toString();
        //String UserInstitution = "AQB";
        System.out.println("UserInstitution:"+UserInstitution);
        DBOperator db = new DBOperator("");
        Map[] InstitutionList = db.getTableByOneProc("InstitutionProc", UserInstitution);            /*绑定机构编号*/
        Map<String, Object> CategoryList = associateMangementService.getCategory();
        Map<String, Object> result = new HashMap<>();
        result.put("InstitutionList", InstitutionList);
        result.put("CategoryList", CategoryList);
        System.out.println("result"+result.toString());
        return result;
    }

    //主界面——绑定数据
    @RequestMapping("/DangerTaskApplyMan/getList")
    @ResponseBody
    public Map<String, Object> List(@RequestParam(value = "conditions", required = false, defaultValue = "") String conditions, @RequestParam(value = "pageindex", required = false, defaultValue = "1") Integer pageindex, HttpSession session) throws SQLException {
        String username = session.getAttribute("UserNum").toString();
        String UserInstitution = session.getAttribute("UserInstitution").toString();
        System.out.println("进入了getList，返回所有申请和页数");
        //String username = "20";
        //String UserInstitution = "AQB";
        Map[] taskList = associateMangementService.getAlltaskListInfo(conditions, pageindex, username, UserInstitution);
        Map[] alltask = associateMangementService.getAlltaskList(conditions, username, UserInstitution);
        int pagenum;
        if (alltask == null) {
            pagenum = 0;
        } else {
            pagenum = alltask.length % 10 == 0 ? alltask.length / 10 : alltask.length / 10 + 1;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("taskList", taskList);
        result.put("pagenum", pagenum);
        System.out.println("result"+result);
        return result;
    }

    //添加——绑定下拉列表数据  当页面加载的时候，为下拉列表添加项目，从数据字典表中查找这些项
    @RequestMapping("/DangerTaskApplyMan/getInfoForAdd")
    @ResponseBody
    public Map<String, Object> getInfoForAdd(HttpSession session) {
        String UserName = session.getAttribute("UserName").toString();
        String UserNum = session.getAttribute("UserNum").toString();
        String UserInstitution = session.getAttribute("UserInstitution").toString();
        String UserInstitutionName = session.getAttribute("UserInstitutionName").toString();
        /*String UserName = "朱云翔";
        String UserNum = "20";
        String UserInstitution = "AQB";
        String UserInstitutionName="安全部";*/


        DBOperator db = new DBOperator("");
        Map[] FirstInstitutionList = db.getTableByOneProc("NextAppvoreProc", UserNum);                //第一审批部门
        Map<String, Object> result = new HashMap<>();
        //result.put("TaskInstitutionList", TaskInstitutionList);
        result.put("FirstInstitutionList", FirstInstitutionList);
        //result.put("CategoryList",CategoryList);
        result.put("UserName", UserName);
        result.put("UserInstitution", UserInstitution);
        result.put("UserInstitutionName", UserInstitutionName);
        return result;
    }


    // 添加数据
    @RequestMapping("/DangerTaskApplyMan/SubmitAdd")
    public void SubmitAdd(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws IOException {
        try {
            System.out.println("进入了SubmitAdd");
            String ApplicantNum = "";
            String UserInstitution = "";
            String StartTime;
            String EndTime;
            String ApplyingTime;
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (request.getParameter("tag").equals("Mobile")) {
                ApplicantNum = request.getParameter("UserNum").toString();
                UserInstitution = request.getParameter("UserInstitution").toString();
                StartTime = request.getParameter("StartTime");
                EndTime = request.getParameter("EndTime");
                ApplyingTime = request.getParameter("ApplyingTime");
            } else {
                ApplicantNum = session.getAttribute("UserNum").toString();
                UserInstitution = session.getAttribute("UserInstitution").toString();
                /*ApplicantNum = "20";
                UserInstitution = "AQB";*/
                StartTime = dateformat.format(new Date(request.getParameter("StartTime")));
                EndTime = dateformat.format(new Date(request.getParameter("EndTime")));
                ApplyingTime = dateformat.format(new Date(request.getParameter("ApplyingTime")));
            }
            //java.sql.Date ApplyingTime = new java.sql.Date(formatter.parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sdf.parse(request.getParameter("ApplyingTime").replace("GMT", "").replaceAll("\\(.*\\)", ""))).equals("") ? "1970-01-01" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sdf.parse(request.getParameter("ApplyingTime").replace("GMT", "").replaceAll("\\(.*\\)", "")))).getTime());
            String Archived = request.getParameter("Archived");
            String DangerTaskName = request.getParameter("DangerTaskName");
            String Category = request.getParameter("Category");
            String DangerTaskLevel = request.getParameter("DangerTaskLevel");
            String DangerTaskNum = request.getParameter("DangerTaskNum");


            if (DangerTaskName.contains("'")) {
                DangerTaskName = DangerTaskName.replace("'", "''");
            }

            String uploadfilename = request.getParameter("uploadfilename").toString();
            //String firstInstitution="0";
            String ApproverList = request.getParameter("ApproverList").toString();
            int taskID = 0;

            DBOperator db = new DBOperator("");
            System.out.println("=============="+ApplicantNum+" "+UserInstitution+" "+ApplyingTime+" "+StartTime+" "+EndTime+" "+DangerTaskName+" "+Category+" "+Archived+" "+uploadfilename+" "+ApproverList+" "+DangerTaskLevel+" "+DangerTaskNum);
            String returntext = associateMangementService.insertApplicationTask(ApplicantNum, UserInstitution, ApplyingTime, StartTime, EndTime, DangerTaskName, Category, Archived, uploadfilename, ApproverList,DangerTaskLevel,DangerTaskNum) > 0 ? "success" : "error";
            db = new DBOperator("select top(1) TaskID from DangerTaskApplicationTable_T order by TaskID desc");
            String TaskID = db.executeQuery()[0].get("TaskID").toString();
            //session.setAttribute("TaskID_yuan",TaskID);
            //DangerTaskApplyManPath = request.getServletContext().getRealPath("/") + "upload/AssociateManagement/DangerTaskApplyMan/";
            String dest = DangerTaskApplyManPath + TaskID + "/";
            String source = DangerTaskApplyManPath + "temp/";
            long startTime = System.currentTimeMillis();
            new Thread1(TaskID).run();
            //    CopyFile(TaskID, dest, source);
            long endTime = System.currentTimeMillis();
            System.out.println("运行时间:" + (endTime - startTime) + "ms");
            if (request.getParameter("tag").equals("Mobile")) {
                System.out.println("mobile");
                HashMap<String, String> re = new HashMap<>();
                re.put("tag", "SubmitAdd");
                re.put("data", TaskID);
                JSONArray json = JSONArray.fromObject(re);
                response.setContentType("charset=utf-8");
                response.getWriter().write(json.toString());
            } else {
                System.out.println("no mobile");
                HashMap<String, String> re = new HashMap<>();
                re.put("data", TaskID);
                JSONArray json = JSONArray.fromObject(re);
                response.setContentType("charset=utf-8");
                response.getWriter().write(json.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("charset=utf-8");
            response.getWriter().write("error");

        }

    }

    //上传文件
    @RequestMapping("/DangerTaskApplyMan/upload")
    @ResponseBody
    public String uploadPic(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        try {
            request.setCharacterEncoding("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String ID = request.getParameter("id").toString();
        String storePath = "";
        storePath = DangerTaskApplyManPath + "temp/";
        System.out.println("上传文件: DangerTaskApplyManPath"+DangerTaskApplyManPath);
        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            File filepath = new File(storePath, fileName);
            if (!filepath.getParentFile().exists()) {
                filepath.getParentFile().mkdirs();//如果目录不存在，创建目录
            }
            try {
                //把文件写入目标文件地址
                file.transferTo(new File(storePath + File.separator + fileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    class Thread1 implements Runnable {
        private String TaskID;

        public Thread1(String TaskID) {
            this.TaskID = TaskID;

        }

        private void CopyFile(String TaskID) throws Exception {

            String filenames = dBo.getString("select UploadFileName from DangerTaskApplicationTable_T where TaskID=" + TaskID, "UploadFileName");
            if (!filenames.equals("")) {
                for (String filename : filenames.split("\\?")
                ) {
                    String source = DangerTaskApplyManPath + "temp/" + filename;
                    String dest = DangerTaskApplyManPath + TaskID + "/" + filename;
                    System.out.println("正在上传文件...");
                    FileIO.doCopyFile(source, dest);
                    System.out.println("上传文件完成.");

                }
            }else{
                System.out.println("文件名为空.");
            }
        }

        /*private void CopyFile(String TaskID, String dest, String source) throws Exception {




            DBo dBo = new DBo();
            String filenames = dBo.getString("select UploadFileName from DangerTaskApplicationTable_T where TaskID=" + TaskID, "UploadFileName");
            if (!filenames.equals("")) {
                // filenames = filenames.toLowerCase();
                for (String filename : filenames.split("\\?")
                ) {

                    String temp = filename;

                    //filename = crypt.DESEncrypt(filename, "shirleyL");
                    File file = new File(dest + filename);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();//如果目录不存在，创建目录
                    }
                    File oldFile = new File(source + temp);
                    if (oldFile.exists()) {
                        copyFileUsingFileChannels(oldFile, file);
                    } else {

                        while (!oldFile.exists()) {
                            Thread.sleep(5000);
                            oldFile = new File(source + temp);
                        }
                        copyFileUsingFileChannels(oldFile, file);
                    }

                }
            }
        }*/

        //copy文件
        private void copyFileUsingFileChannels(File source, File dest) throws IOException {

            if (source.exists() && !dest.exists()) {
                FileChannel inputChannel = null;
                FileChannel outputChannel = null;
                try {
                    inputChannel = new FileInputStream(source).getChannel();
                    outputChannel = new FileOutputStream(dest).getChannel();
                    outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                    source.delete();
                } catch (Exception e) {
                    throw e;
                } finally {
                    inputChannel.close();
                    outputChannel.close();
                }
            }
        }

        @Override
        public void run() {
            try {
                CopyFile(TaskID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //详细
    @RequestMapping("/DangerTaskApplyMan/getInfoForDetail")
    @ResponseBody
    public Map<String, Object> getInfoForDetail(HttpServletRequest request) {
        System.out.println("进入了getInfoForDetail,返回单个申请的详细信息");
        String TaskID = request.getParameter("TaskID");
        Map[] TaskInfo = associateMangementService.getTaskInfo(TaskID);
        Map<String, Object> result = new HashMap<>();
        result.put("TaskInfo", TaskInfo);
        return result;
    }
    // 查看文件
    @RequestMapping("/DangerTaskApplyMan/viewFile")
    public void viewFile(@RequestParam(value = "filename", required = false, defaultValue = "-1") String filename, @RequestParam(value = "id", required = false, defaultValue = "") String id,
                         HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        try {
            System.out.println("进入了DangerTaskApplyMan/viewFile。filename:"+filename+" id:"+id+" TaskApproveManPath:"+DangerTaskApplyManPath);

            String returntext = "";
            //预览文件的类型
            String type = "";
            //原始文件的类型
            String contentType = filename.split("\\.")[filename.split("\\.").length - 1];
            //文件名（不含后缀）
            String name = filename.substring(0, filename.lastIndexOf("."));
            //文件地址
            String filepath = DangerTaskApplyManPath + id + "/" + filename;
            File prefile = new File(DangerTaskApplyManPath + id + "/preview");
            if (!prefile.exists()) {
                prefile.mkdir();
            }
            if (contentType.equals("xlsx") || contentType.equals("xls")
                    || contentType.equals("doc") || contentType.equals("docx") || contentType.equals("ppt") ||
                    contentType.equals("pptx") || contentType.equals("txt")) {
                //Office转pdf
                type = "pdf";
                name = "/preview/" + name + "." + type;
                String previewPath = DangerTaskApplyManPath + id + name;
                File file = new File(previewPath);
                /*if (!file.exists()) {
                    FileManager.convert2PDF(filepath, previewPath);
                }*/
            } else if (contentType.equals("wma") || contentType.equals("ape") || contentType.equals("flac") || contentType.equals("aac") ||
                    contentType.equals("ac3") || contentType.equals("mmf") || contentType.equals("amr") || contentType.equals("m4a") ||
                    contentType.equals("m4r") || contentType.equals("ogg") || contentType.equals("wav") || contentType.equals("wavpack") ||
                    contentType.equals("mp2") || contentType.equals("mp3")) {
                type = contentType;
                name = "/" + name + "." + type;
            } else if (contentType.equals("asx") || contentType.equals("asf") || contentType.equals("mpg") || contentType.equals("wmv") ||
                    contentType.equals("3gp") || contentType.equals("mp4") || contentType.equals("webm") || contentType.equals("mkv") || contentType.equals("avi") ||
                    contentType.equals("flv")) {
                type = contentType;
                name = "/" + name + "." + type;
            } else {
                type = contentType;
                name = "/" + name + "." + type;
            }
            name = "upload/AssociateManagement/DangerTaskApplyMan/" + id + name;

            returntext = "{\"type\":\"" + type + "\"," +
                    "    \"filepath\": \"" + name.replace("\\", "\\\\") + "\"}";
            System.out.println("返回结果:"+returntext);
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(returntext);
            System.out.println("成功返回");
        } catch (Exception e) {
            System.out.println("抛出了异常");
            e.printStackTrace();
        }

    }
    //文件下载
    @RequestMapping("/DangerTaskApplyMan/download")
    public void downloadFile(HttpServletResponse response, HttpServletRequest request, @RequestParam("filename") String filename, @RequestParam("id") String id) throws Exception {
        String filePath = DangerTaskApplyManPath + id + "/";
        FileManager.downloadFile(response, filePath, filename);
    }

    //删除前判断可否删除
    @RequestMapping("/DangerTaskApplyMan/check")
    @ResponseBody
    public Map<String, Object> check(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws Exception {
        String tasklist = request.getParameter("TaskID");
        //String usernum = session.getAttribute("UserNum").toString();
        String usernum = "20";
        List<Map<String, Object>> list = associateMangementService.getTaskList(tasklist);
        List<Map<String, Object>> Applicant = associateMangementService.getApplicant(tasklist);
        boolean flag = true;
        if (Applicant.size() > 1 || Applicant.size() == 0) {
            flag = false;
        } else {
            if (!usernum.equals(Applicant.get(0).get("Applicant").toString())) {
                flag = false;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("infoList", list);
        result.put("applicant", flag);
        return result;
    }

    //修改——加载所需数据
    @RequestMapping("/DangerTaskApplyMan/getInfoForEdit")
    @ResponseBody
    public Map<String, Object> getInfoForEdit(HttpServletRequest request, HttpSession session) {
        System.out.println("进入getInfoForEdit");
        DBOperator db = new DBOperator("");
        String UserNum = session.getAttribute("UserNum").toString();
        String UserInstitution = session.getAttribute("UserInstitution").toString();
        String UserInstitutionName = session.getAttribute("UserInstitutionName").toString();
        /*String UserName = "朱云翔";
        String UserNum = "20";
        String UserInstitution = "AQB";
        String UserInstitutionName="安全部";*/
        Map[] FirstInstitutionList = db.getTableByOneProc("NextAppvoreProc", UserNum);                //第一审批部门


        String TaskID = request.getParameter("TaskID");
        Map[] taskInfo = associateMangementService.getTaskInfo(TaskID);
        Map<String, Object> result = new HashMap<>();
        result.put("FirstInstitutionList", FirstInstitutionList);
        //result.put("TaskInstitutionList", TaskInstitutionList);
        //result.put("CategoryList",CategoryList);
        result.put("taskInfo", taskInfo);
        result.put("UserInstitution", UserInstitution);
        result.put("UserInstitutionName", UserInstitutionName);
        return result;
    }

    //修改——提交
    @RequestMapping("/DangerTaskApplyMan/Edit_Submit")
    public void Edit_Submit(HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        try {
            System.out.println("进入了Edit_Submit");
            String ApplicantNum = "";
            String UserInstitution = "";
            String StartTime;
            String EndTime;
            String ApplyingTime;
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (request.getParameter("tag").equals("Mobile")) {
                ApplicantNum = request.getParameter("UserNum").toString();
                UserInstitution = request.getParameter("UserInstitution").toString();
                StartTime = request.getParameter("StartTime");
                EndTime = request.getParameter("EndTime");
                ApplyingTime = request.getParameter("ApplyingTime");
            } else {
                ApplicantNum = session.getAttribute("UserNum").toString();
                UserInstitution = session.getAttribute("UserInstitution").toString();
                /*ApplicantNum = "20";
                UserInstitution="AQB";*/
                StartTime = request.getParameter("StartTime").contains("GMT") ? dateformat.format(new Date(request.getParameter("StartTime"))) : request.getParameter("StartTime");
                EndTime = request.getParameter("EndTime").contains("GMT") ? dateformat.format(new Date(request.getParameter("EndTime"))) : request.getParameter("EndTime");
                ApplyingTime = request.getParameter("ApplyingTime").contains("GMT") ? dateformat.format(new Date(request.getParameter("ApplyingTime"))) : request.getParameter("ApplyingTime");
            }

            //java.sql.Date ApplyingTime = new java.sql.Date(formatter.parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sdf.parse(request.getParameter("ApplyingTime").replace("GMT", "").replaceAll("\\(.*\\)", ""))).equals("") ? "1970-01-01" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sdf.parse(request.getParameter("ApplyingTime").replace("GMT", "").replaceAll("\\(.*\\)", "")))).getTime());
            String Archived = request.getParameter("Archived");
            String DangerTaskName = request.getParameter("DangerTaskName");
            String Category = request.getParameter("Category");
            String DangerTaskLevel = request.getParameter("DangerTaskLevel");
            String DangerTaskNum = request.getParameter("DangerTaskNum");

//            String StartTime=request.getParameter("StartTime").contains("GMT")? dateformat.format(new Date(request.getParameter("StartTime"))):request.getParameter("StartTime");
//            String EndTime=request.getParameter("EndTime").contains("GMT")? dateformat.format(new Date(request.getParameter("EndTime"))):request.getParameter("EndTime");
//            String ApplyingTime=request.getParameter("ApplyingTime").contains("GMT")? dateformat.format(new Date(request.getParameter("ApplyingTime"))):request.getParameter("ApplyingTime");
            if (DangerTaskName.contains("'")) {
                DangerTaskName = DangerTaskName.replace("'", "''");
            }
            //java.sql.Date ApplyingTime = new java.sql.Date(new java.util.Date().getTime());
            String uploadfilename = request.getParameter("uploadfilename").toString();
            //String firstInstitution="0";
            String ApproverList = request.getParameter("ApproverList").toString();
            String State = request.getParameter("State");
            String TaskID = request.getParameter("TaskID");


            //String SubmitterID=session.getAttribute("UserNum").toString();
            //String taskID=session.getAttribute("TaskID_yuan").toString();
            //String firstInstitutionList=request.getParameter("rightlist").toString();
            String returntext =associateMangementService.Edit_submitApplicantTask(ApplicantNum, UserInstitution, ApplyingTime, StartTime, EndTime, DangerTaskName, Category, Archived, uploadfilename, State, ApproverList, TaskID,DangerTaskLevel,DangerTaskNum) > 0 ? "success" : "error";
            new Thread1(TaskID).run();

            Collection<String> alias = new ArrayList<>();
            Collection<String> tags = new ArrayList<>();
            alias.add(ApproverList.split(",")[0].toString());
            tags.add("AHB");
            PushResult TST = JiguangPush.push(alias, null, "您有一条新的审批待处理");
            if (request.getParameter("tag").equals("Mobile")) {
                HashMap<String, String> re = new HashMap<>();
                re.put("tag", "Edit_Submit");
                re.put("data", TaskID);
                JSONArray json = JSONArray.fromObject(re);
                response.setContentType("charset=utf-8");
                response.getWriter().write(json.toString());
            } else {
                response.setContentType("charset=utf-8");
                response.getWriter().write(returntext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 修改——保存数据
    @RequestMapping("/DangerTaskApplyMan/SubmitSave")
    public void SubmitSave(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws IOException {
        try {
            String ApplicantNum = "";
            String UserInstitution = "";
            String StartTime;
            String EndTime;
            String ApplyingTime;
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (request.getParameter("tag").equals("Mobile")) {

                ApplicantNum = request.getParameter("UserNum").toString();
                UserInstitution = request.getParameter("UserInstitution").toString();
                StartTime = request.getParameter("StartTime");
                EndTime = request.getParameter("EndTime");
                ApplyingTime = request.getParameter("ApplyingTime");
            } else {
                ApplicantNum = session.getAttribute("UserNum").toString();
                UserInstitution = session.getAttribute("UserInstitution").toString();
                /*ApplicantNum = "20";
                UserInstitution="AQB";*/
                StartTime = request.getParameter("StartTime").contains("GMT") ? dateformat.format(new Date(request.getParameter("StartTime"))) : request.getParameter("StartTime");
                EndTime = request.getParameter("EndTime").contains("GMT") ? dateformat.format(new Date(request.getParameter("EndTime"))) : request.getParameter("EndTime");
                ApplyingTime = request.getParameter("ApplyingTime").contains("GMT") ? dateformat.format(new Date(request.getParameter("ApplyingTime"))) : request.getParameter("ApplyingTime");
            }
            String Archived = request.getParameter("Archived");
            String DangerTaskName = request.getParameter("DangerTaskName");
            String Category = request.getParameter("Category");
            String DangerTaskLevel = request.getParameter("DangerTaskLevel");
            String DangerTaskNum = request.getParameter("DangerTaskNum");

            if (DangerTaskName.contains("'")) {
                DangerTaskName = DangerTaskName.replace("'", "''");
            }
            //java.sql.Date ApplyingTime = new java.sql.Date(new java.util.Date().getTime());
            String uploadfilename = request.getParameter("uploadfilename").toString();
            //String firstInstitution="0";
            String ApproverList = request.getParameter("ApproverList").toString();
            String State = request.getParameter("State");
            String TaskID = request.getParameter("TaskID");

            DBOperator db = new DBOperator("");
            String returntext = associateMangementService.updateApplicationTask(ApplicantNum, UserInstitution, ApplyingTime, StartTime, EndTime, DangerTaskName, Category, Archived, uploadfilename, State, ApproverList, TaskID,DangerTaskLevel,DangerTaskNum) > 0 ? "success" : "error";

            // String dest = request.getServletContext().getRealPath("/") + "upload/AssociateManagement/" + TaskID + "/";
            // String source = request.getServletContext().getRealPath("/") + "upload/AssociateManagement/temp/";
            new Thread1(TaskID).run();

            //   CopyFile(TaskID, dest, source);
            if (request.getParameter("tag").equals("Mobile")) {
                HashMap<String, String> re = new HashMap<>();
                re.put("tag", "SubmitSave");
                re.put("data", TaskID);
                JSONArray json = JSONArray.fromObject(re);
                response.setContentType("charset=utf-8");
                response.getWriter().write(json.toString());
            } else {
                response.setContentType("charset=utf-8");
                response.getWriter().write(returntext);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("charset=utf-8");
            response.getWriter().write("error");

        }

    }

    //添加——提交
    @RequestMapping("/DangerTaskApplyMan/Add_Submit")
    public void Add_Submit(HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        try {
            String ApplicantNum = "";
            String UserInstitution = "";
            String StartTime;
            String EndTime;
            String ApplyingTime;
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (request.getParameter("tag").equals("Mobile")) {
                ApplicantNum = request.getParameter("UserNum").toString();
                UserInstitution = request.getParameter("UserInstitution").toString();
                StartTime = request.getParameter("StartTime");
                EndTime = request.getParameter("EndTime");
                ApplyingTime = request.getParameter("ApplyingTime");
            } else {
                ApplicantNum = session.getAttribute("UserNum").toString();
                UserInstitution = session.getAttribute("UserInstitution").toString();
                StartTime = request.getParameter("StartTime").contains("GMT") ? dateformat.format(new Date(request.getParameter("StartTime"))) : request.getParameter("StartTime");
                EndTime = request.getParameter("EndTime").contains("GMT") ? dateformat.format(new Date(request.getParameter("EndTime"))) : request.getParameter("EndTime");
                ApplyingTime = request.getParameter("ApplyingTime").contains("GMT") ? dateformat.format(new Date(request.getParameter("ApplyingTime"))) : request.getParameter("ApplyingTime");
            }

            //java.sql.Date ApplyingTime = new java.sql.Date(formatter.parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sdf.parse(request.getParameter("ApplyingTime").replace("GMT", "").replaceAll("\\(.*\\)", ""))).equals("") ? "1970-01-01" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sdf.parse(request.getParameter("ApplyingTime").replace("GMT", "").replaceAll("\\(.*\\)", "")))).getTime());
            String Archived = request.getParameter("Archived");
            String DangerTaskName = request.getParameter("DangerTaskName");
            String Category = request.getParameter("Category");
            String DangerTaskLevel = request.getParameter("DangerTaskLevel");
            String DangerTaskNum = request.getParameter("DangerTaskNum");

            if (DangerTaskName.contains("'")) {
                DangerTaskName = DangerTaskName.replace("'", "''");
            }
            String uploadfilename = request.getParameter("uploadfilename").toString();
            String ApproverList = request.getParameter("ApproverList").toString();
            String State = request.getParameter("State");
            // String TaskID = request.getParameter("TaskID");

            //DBOperator db = new DBOperator("");
            DBo dbo = new DBo();
            if (!request.getParameter("TaskID").equals(""))    //已经点击过保存按钮
            {
                String returntext1 = associateMangementService.updateApplicationTask(ApplicantNum, UserInstitution, ApplyingTime, StartTime, EndTime, DangerTaskName, Category, Archived, uploadfilename, "1", ApproverList, request.getParameter("TaskID"),DangerTaskLevel,DangerTaskNum) > 0 ? "success" : "error";

            } else {
                String returntext1 = associateMangementService.insertApplicationTask2(ApplicantNum, UserInstitution, ApplyingTime, StartTime, EndTime, DangerTaskName, Category, Archived, uploadfilename, ApproverList,DangerTaskLevel,DangerTaskNum) > 0 ? "success" : "error";
            }
            // db = new DBOperator("select top 1 TaskID from DangerTaskApplicationTable_T order by TaskID desc");
            String TaskID = dbo.executeQuery("select top 1 TaskID from DangerTaskApplicationTable_T order by TaskID desc").get(0).get("TaskID").toString();
            //session.setAttribute("TaskID_yuan",TaskID);
            // String dest = request.getServletContext().getRealPath("/") + "upload/AssociateManagement/" + TaskID + "/";
            // String source = request.getServletContext().getRealPath("/") + "upload/AssociateManagement/temp/";

            new Thread1(TaskID).run();

            String returntext = associateMangementService.Add_submitApplicantTask(ApplicantNum, ApproverList, TaskID) > 0 ? "success" : "error";

            Collection<String> alias = new ArrayList<>();
            Collection<String> tags = new ArrayList<>();
            alias.add(ApproverList.split(",")[0].toString());
            tags.add("AHB");
            PushResult TST = JiguangPush.push(alias, null, "您有一条新的审批待处理");
            if (request.getParameter("tag").equals("Mobile")) {
                HashMap<String, String> re = new HashMap<>();
                re.put("tag", "Add_Submit");
                re.put("data", TaskID);
                JSONArray json = JSONArray.fromObject(re);
                response.setContentType("charset=utf-8");
                response.getWriter().write(json.toString());
            } else {
                response.setContentType("charset=utf-8");
                response.getWriter().write(returntext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除单个文件
     *
     * @param name 文件名称
     * @param id   ID
     */
    @RequestMapping("/DangerTaskApplyMan/deleteFile")
    public void deleteFile(String name, String id, HttpServletResponse response, HttpServletRequest request) throws SQLException, IOException {
        String filenames = dBo.getString("select UploadFileName from DangerTaskApplicationTable_T where TaskID=" + id, "UploadFileName");
        if (filenames.contains(name + "?")) {
            filenames = filenames.replace(name + "?", "");
        }
        dBo.executeUpdate("update DangerTaskApplicationTable_T set UploadFileName='" + filenames + "' where TaskID='" + id + "'");
        FileIO.deleteFiles(DangerTaskApplyManPath, id, name);
        response.setContentType("charset=utf-8");
        response.getWriter().write("success");
    }

    //批复记录——信息加载
    @RequestMapping("/DangerTaskApplyMan/getHistoryRecord")
    public void get2Record(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "TaskID", required = false, defaultValue = "") String TaskID, @RequestParam(value = "ApprovalID", required = false, defaultValue = "") String ApprovalID) throws Exception {
        try {
            if (request.getParameter("tag").equals("Approve")) {
                TaskID = dBo.getString("select TaskID from DangerTaskApprovalTable_T where ApprovalID='" + ApprovalID + "'", "TaskID");
            }
            List<Map<String, Object>> list = null;
            list = dBo.executeQuery("select ApprovalID,TaskID,ApproveInstitution,Employee_t.EmployeeName ApprovalPeopleName,institution_t.InstitutionName  as ApproveInstitutionName,\n" +
                    "ApproveSuggestion,ApproveResult,Finished,Employee_t.EmployeeName,CONVERT(VARCHAR(10), dbo.DangerTaskApprovalTable_T.ApproveTime, 23) AS ApproveTime\n" +
                    "from DangerTaskApprovalTable_T,Employee_t,institution_t \n" +
                    "where Employee_t.InstitutionNum=institution_t.InstitutionNum and DangerTaskApprovalTable_T.ApproveInstitution=Employee_t.EmployeeNum  \n" +
                    "and  DangerTaskApprovalTable_T.TaskID ='" + TaskID + "' ORDER BY DangerTaskApprovalTable_T.ApprovalID");
            if (list.size() > 0 && !list.get(list.size() - 1).get("Finished").toString().equals("4")) {
                String[] ApproverList = dBo.getString("select ApproverList from DangerTaskApplicationTable_T where taskid =" + TaskID, "ApproverList").split(",");
                String EmployeeNum = "";
                List<Map<String, Object>> list1 = null;
                for (int i = list.size(); i < ApproverList.length; i++) {
                    //list1 = dBo.executeQuery("select '' as ApprovalID,'" + TaskID + "' as TaskID ,'' as ApproveInstitution,'' as ApproveSuggestion,'' as ApproveResult, '0' as Finished, a.EmployeeName,b.InstitutionName from Employee_t a,institution_t b where b.InstitutionNum=a.InstitutionNum AND a.EmployeeNum  ="+ApproverList[i]  );
                    list1 = dBo.executeQuery("select '' as ApprovalID,'" + TaskID + "' as TaskID ,'' as ApproveInstitution,'' as ApproveSuggestion,'' as ApproveResult, '0' as Finished,\n" +
                            "CONVERT(VARCHAR,'1970-01-01', 23) AS ApproveTime,a.EmployeeName as ApprovalPeopleName,b.InstitutionName as ApproveInstitutionName\n" +
                            "from Employee_t a,institution_t b where b.InstitutionNum=a.InstitutionNum AND a.EmployeeNum  =" + ApproverList[i]);
                    list.addAll(list1);
                }
            }
            HashMap<String, String> re = new HashMap<>();
            re.put("data", JSONArray.fromObject(list).toString());
            JSONArray json = JSONArray.fromObject(re);
            response.setContentType("charset=utf-8");
            response.getWriter().write(json.toString());
        } catch (Exception e) {
            response.setContentType("charset=utf-8");
            response.getWriter().write("error");
        }

    }

    //绑定下拉列表数据
    @RequestMapping("/TaskApproveMan/getInfoList")
    @ResponseBody
    public Map<String, Object> getInfoListForApproval(HttpSession session) throws Exception {
        System.out.println("进入了getInfoList");
        String UserInstitution = session.getAttribute("UserInstitution").toString();
        //String UserInstitution = "AQB";
        DBOperator db = new DBOperator("");
        Map[] InstitutionList = db.getTableByOneProc("InstitutionProcApproval", UserInstitution);            /*绑定机构编号*/
        Map<String, Object> CategoryList = associateMangementService.getCategory();
        Map<String, Object> result = new HashMap<>();
        result.put("InstitutionList", InstitutionList);
        result.put("CategoryList", CategoryList);
        System.out.println("=========================================="+result.toString());
        return result;
    }

    //审批主界面——绑定数据
    //待批作业
    @RequestMapping("/TaskApproveMan/getApprovalList")
    @ResponseBody
    public Map<String, Object> getApprovalList(@RequestParam(value = "conditions", required = false, defaultValue = "") String conditions, @RequestParam(value = "pageindex", required = false, defaultValue = "1") Integer pageindex, HttpSession session) throws SQLException {
        System.out.println("进入了getApprovalList");
        String username = session.getAttribute("UserNum").toString();
        String UserInstitution = session.getAttribute("UserInstitution").toString();
        System.out.println("username: "+username+"  UserInstitution:"+UserInstitution);
        /*String username = "20";
        String UserInstitution = "AQB";*/
        System.out.println(conditions);
        Map[] approvalList = associateMangementService.getAllApprovalListInfo(conditions, pageindex, username, UserInstitution);
        Map[] allApprovalList =associateMangementService.getAllApprovalList(conditions, username);
        int pagenum;
        if (allApprovalList == null) {
            pagenum = 0;
        } else {
            pagenum = allApprovalList.length % 10 == 0 ? allApprovalList.length / 10 : allApprovalList.length / 10 + 1;
        }
        // int pagenum=approvalList.length % 10 == 0 ? approvalList.length / 10 : approvalList.length / 10 + 1;
        Map<String, Object> result = new HashMap<>();
        result.put("approvalList", approvalList);
        result.put("pagenum", pagenum);
        return result;

    }

    //审批功能界面——绑定数据
    @RequestMapping("/TaskApproveMan/ApproveInfo")
    @ResponseBody
    public Map<String, Object> ApproveInfo(HttpServletRequest request, HttpSession session) throws Exception {
        //String  username = session.getAttribute("UserNum").toString();
        String ApprovalID = request.getParameter("ApprovalID");
        String usernum = session.getAttribute("UserNum").toString();
        String username = session.getAttribute("UserName").toString();
        String UserInstitution = session.getAttribute("UserInstitution").toString();
        String UserInstitutionName =dBo.getString("select InstitutionName from Institution_t where InstitutionNum = '"+UserInstitution+"'","InstitutionName");
        //String UserInstitutionName = session.getAttribute("UserInstitutionName").toString();
        List<Map<String, Object>> ApplyInfo = associateMangementService.getApplyInfo(ApprovalID);
        DBOperator db = new DBOperator("");
        DBo dbo = new DBo();
        String ApproverList = dbo.getString("select ApproverList from DangerTaskApplicationTable_T where TaskID='" + ApplyInfo.get(0).get("TaskID").toString() + "'", "ApproverList");
        ApproverList = ApproverList.substring(0, ApproverList.indexOf(usernum) + usernum.length());
        // Map[] InstitutionList = db.getNextApprovalProc("InstitutionProcNextApproval",UserInstitution,ApproverList);

        String sql = "declare @InstitutionCategoryNum varchar(1)\n" +
                "   select @InstitutionCategoryNum=InstitutionCategoryNum from institution_t where institutionNum='" + UserInstitution + "'\n" +
                "   if @InstitutionCategoryNum='3'\n" +
                "      begin \n" +
                "\t   \tselect PeopleInCharge employeeNum,RTRIM(institutionName)+'-'+RTRIM(EmployeeName)+'-'+RTRIM(EmployeeID) institutionName from institution_t a inner join Employee_t on institutionCategoryNum='3' and IsDelete = '否' and a.institutionNum<>'" + UserInstitution + "' and PeopleInCharge not in (" + ApproverList + ") and PeopleInCharge=EmployeeNum  and OutCompanyOrNot<>'1' and IsRetire<>'1'\n" +
                "\t\t union \n" +
                "\t    select employeeNum,RTRIM('高管-')+RTRIM(employeeName)+'-'+RTRIM(EmployeeID) institutionName from employee_t where employee_t.institutionNum='GG' and employee_t.EmployeeNum not in(" + ApproverList + ")  and OutCompanyOrNot<>'1' and IsRetire<>'1'\n" +
                "\t  end\n" +
                "\telse if @InstitutionCategoryNum='4'\n" +
                "\t  begin\n" +
                "\t    \tselect PeopleInCharge employeeNum,RTRIM(institutionName)+'-'+RTRIM(EmployeeName)+'-'+RTRIM(EmployeeID) institutionName  from institution_t a inner join Employee_t on (institutionCategoryNum='3' or institutionCategoryNum='4') and a.institutionNum<>'" + UserInstitution + "' and IsDelete = '否' and PeopleInCharge=EmployeeNum  and OutCompanyOrNot<>'1' and IsRetire<>'1' \n" +
                "\t\t union \n" +
                "\t    select employeeNum,RTRIM('高管-')+RTRIM(employeeName)+'-'+RTRIM(EmployeeID) institutionName from employee_t where employee_t.institutionNum='GG' and employee_t.EmployeeNum not in(" + ApproverList + ") and OutCompanyOrNot<>'1' and IsRetire<>'1'\n" +
                "\t  end\n" +
                "\telse if @InstitutionCategoryNum='5'\n" +
                "\t  begin\n" +
                "\t    \tselect PeopleInCharge employeeNum,RTRIM(institutionName)+'-'+RTRIM(EmployeeName)+'-'+RTRIM(EmployeeID) institutionName  from institution_t a inner join Employee_t on (institutionCategoryNum='3' or institutionCategoryNum='4' or institutionCategoryNum='5') and a.institutionNum<>'" + UserInstitution + "' and IsDelete = '否' and PeopleInCharge=EmployeeNum  and OutCompanyOrNot<>'1' and IsRetire<>'1'  \n" +
                "\t\t union \n" +
                "\t    select employeeNum,RTRIM('高管-')+RTRIM(employeeName)+'-'+RTRIM(EmployeeID) institutionName from employee_t where employee_t.institutionNum='GG' and employee_t.EmployeeNum not in(" + ApproverList + ") and OutCompanyOrNot<>'1' and IsRetire<>'1'\n" +
                "\t  end\n" +
                "\telse if @InstitutionCategoryNum='2'\n" +
                "\t  begin\n" +
                "\t     select employeeNum,RTRIM('高管-')+RTRIM(employeeName)+'-'+RTRIM(EmployeeID) institutionName from employee_t where employee_t.institutionNum='GG'  and employee_t.EmployeeNum not in(" + ApproverList + ") and OutCompanyOrNot<>'1' and IsRetire<>'1'\n" +
                "\t  end\n" +
                "\telse if @InstitutionCategoryNum='1'\n" +
                "\t  begin\n" +
                "\t    select employeeNum,RTRIM('高管-')+RTRIM(employeeName)+'-'+RTRIM(EmployeeID) institutionName from employee_t where employee_t.institutionNum='GG'  and employee_t.EmployeeNum not in(" + ApproverList + ") and OutCompanyOrNot<>'1' and IsRetire<>'1'\n" +
                "\t  end";
        List<Map<String, Object>> InstitutionList = dbo.executeQuery(sql);
        String Finished = dbo.getString("select Finished from DangerTaskApprovalTable_T where ApprovalID='" + ApprovalID + "'", "Finished");
        String WaterMarkSignPath = dbo.getString("select WaterMarkSignPath from SignatureInfo_T where EmployeeNum = '" + usernum + "'", "WaterMarkSignPath");

        Map<String, Object> result = new HashMap<>();
        result.put("ApplyInfo", ApplyInfo);
        result.put("usernum", usernum);
        result.put("username", username);
        result.put("UserInstitution", UserInstitution);
        result.put("UserInstitutionName", UserInstitutionName);
        result.put("InstitutionList", InstitutionList);
        result.put("Finished", Finished);

        result.put("WaterMarkSignPath", WaterMarkSignPath);
        return result;
    }

    //退回界面——绑定数据
    @RequestMapping("/TaskApproveMan/callbackInfo")
    @ResponseBody
    public Map<String, Object> callbackInfo(HttpServletRequest request) throws SQLException {
        //String  username = session.getAttribute("UserNum").toString();
        String ApprovalID = request.getParameter("ApprovalID");
        Map[] SubmitterInfo = associateMangementService.getSubmitter(ApprovalID);
        Map<String, Object> result = new HashMap<>();
        result.put("SubmitterInfo", SubmitterInfo);
        return result;
    }

    //审批--确定
    @RequestMapping("/TaskApproveMan/ApprovalSubmit")
    public void ApprovalSubmit(HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        try {
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date currentTime = new Date();
            String TaskID = request.getParameter("TaskID");//1
            String Approver;
            //String ApproveInstitution = request.getParameter("ApproveInstitution");
            String ApprovalResult = request.getParameter("ApprovalResult");//1、同意，0、不同意 //1
            String ApprovalFinished = request.getParameter("ApprovalFinished");//0、待批、 2、结束 //1
            String ApproveSuggestion = request.getParameter("ApproveSuggestion");//1
            String nextApproveInstitutionList = request.getParameter("nextApproveInstitutionList");//1
            String ApproveTime;
            String flag = request.getParameter("flag");//1 //是否是审批序列最后一个人
            String ApprovalID = "";
            String sourceImageUrl = "";
            String SignPath = "";
            if (!request.getParameter("tag").equals("Mobile")) {
                ApprovalID = request.getParameter("ApprovalID");
                sourceImageUrl = request.getParameter("imageUrl");//图片的完整路径 "../../Image/xxxxx/usexxx/UserID+时间"
                Approver = session.getAttribute("UserNum").toString();
                ApproveTime = dateformat.format(new Date(request.getParameter("ApproveTime")));//1
            } else {
                ApproveTime = request.getParameter("ApproveTime");
                Approver = request.getParameter("UserNum").toString();
                ApprovalID = dBo.getString("select TOP(1) ApprovalID from DangerTaskApprovalTable_T where TaskID='" + TaskID + "' and ApproveName='" + Approver + "' order by ApprovalID DESC", "ApprovalID");
                SignPath="../../upload/AssociateManagement/TaskApproveMan/SignaturePhoto/"+TaskID+"/" + request.getParameter("UserNum").toString() + request.getParameter("time").toString() + ".jpg";
            }

            if (!sourceImageUrl.equals("") && !request.getParameter("tag").equals("Mobile")&&!sourceImageUrl.contains("white.jpg")) {
                String filename = sourceImageUrl.split("/")[6].split("\\.")[0];
                filename = filename.substring(0, filename.length() - 14);
                String type = sourceImageUrl.split("/")[6].split("\\.")[1];
                String dest = request.getServletContext().getRealPath("/") + "upload/AssociateManagement/TaskApproveMan/SignaturePhoto/" + TaskID + "/" + filename + TaskID + formatter.format(currentTime) + "." + type;
                String source = request.getServletContext().getRealPath("/") + "upload/SystemMan/UserMan/SignaturePhoto/" + sourceImageUrl.split("/")[6].split("\\.")[0] + "." + type;
                FileIO.copySignPhoto(source, dest);
                SignPath = "../../"+dest.substring(dest.indexOf("upload"), dest.lastIndexOf("/")) + "/" + filename + TaskID + formatter.format(currentTime) + "." + type;
            }
            String returntext = associateMangementService.Approve(ApprovalID, TaskID, Approver, nextApproveInstitutionList, ApprovalResult, ApproveTime, ApprovalFinished, ApproveSuggestion, SignPath, flag) > 0 ? "success" : "error";
            response.setContentType("charset=utf-8");
            response.getWriter().write(returntext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //转发界面——绑定数据
    @RequestMapping("/TaskApproveMan/relayInfo")
    @ResponseBody
    public Map<String, Object> relayInfo(HttpServletRequest request, HttpSession session) throws Exception {
        String username = session.getAttribute("UserNum").toString();
        String UserInstitution = session.getAttribute("UserInstitution").toString();
        // String ApprovalID = request.getParameter("ApprovalID");
        Map<String, Object> result = new HashMap<>();
        Map[] relayPeopleList = associateMangementService.getrelayPeopleList(UserInstitution, username);   //获得转发对象
        //Map<String,Object> taskInfo = as.getTaskInfoApproval(ApprovalID);        Map<String, Object> result = new HashMap<>();
        result.put("relayPeopleList", relayPeopleList);
        //result.put("taskInfo", taskInfo);
        return result;
    }
    //转发
    @RequestMapping("/TaskApproveMan/relaySave")
    public void replaySave(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws IOException {
        try {
            String username;
            List<String> ApprovalIDList = new ArrayList<>();
            String ApproveSuggestion = request.getParameter("ApproveSuggestion");
            String relayPeople = request.getParameter("relayPeople").split(",")[0];
            String TaskID = request.getParameter("TaskID");
            if (request.getParameter("tag").equals("Mobile")) {
                //未完成
                username = request.getParameter("UserNum");
                List<String> ls = new ArrayList<>();
                for (String s : TaskID.split(",")) {
                    String temp = dBo.getString("select top(1) ApprovalID from DangerTaskApprovalTable_T where ApproveName='" + username + "' and taskid ='" + s + "' order by ApprovalID desc", "ApprovalID");
                    ApprovalIDList.add(temp);
                }


            } else {
                for (String s : request.getParameter("ApprovalIDList").split(",")) {
                    ApprovalIDList.add(s);
                }

                username = session.getAttribute("UserNum").toString();
                ApproveSuggestion = request.getParameter("ApproveSuggestion");
            }
            String[] ApprovalIDLists = ApprovalIDList.toArray(new String[ApprovalIDList.size()]);
            String returntext = associateMangementService.replayApplication(ApprovalIDLists, username, relayPeople, ApproveSuggestion) > 0 ? "success" : "error";

            Collection<String> alias = new ArrayList<>();
            Collection<String> tags = new ArrayList<>();
            alias.add(relayPeople);
            PushResult TST = JiguangPush.push(alias, null, "您有一条新的审批待处理！");

            if (request.getParameter("tag").equals("Mobile")) {
                HashMap<String, String> re = new HashMap<>();
                re.put("tag", "relaySave");
                re.put("data", returntext);
                JSONArray json = JSONArray.fromObject(re);
                response.setContentType("charset=utf-8");
                response.getWriter().write(json.toString());
            } else {
                response.setContentType("charset=utf-8");
                response.getWriter().write(returntext);
            }

        } catch (Exception e) {
            response.setContentType("charset=utf-8");
            response.getWriter().write("error");
        }
    }
    //退回
    @RequestMapping("/TaskApproveMan/callbackSave")
    public void callbackSave(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws IOException {
        try {
            String ApprovalID;
            String SubmitterID;
            String Finished;
            String Applicant;
            String ApproveSuggestion = request.getParameter("ApproveSuggestion");
            if (!request.getParameter("tag").equals("Mobile")) {
                ApprovalID = request.getParameter("ApprovalID");
                SubmitterID = request.getParameter("SubmitterID");
                Applicant = request.getParameter("Applicant");
                ApproveSuggestion = request.getParameter("ApproveSuggestion");
                Finished = request.getParameter("Finished");
            } else {
                String username = request.getParameter("UserNum");
                String TaskID = request.getParameter("TaskID");
                ApprovalID = dBo.getString("select ApprovalID from DangerTaskApprovalTable_T where TaskID='" + TaskID + "' and ApproveName='" + username + "'", "ApprovalID");
                SubmitterID = dBo.getString("select SubmitterID from DangerTaskApprovalTable_T where TaskID='" + TaskID + "' and ApproveName='" + username + "'", "SubmitterID");
                Finished = dBo.getString("select Finished from DangerTaskApprovalTable_T where TaskID='" + TaskID + "' and ApproveName='" + username + "'", "Finished");

                Applicant = dBo.getString("select Applicant from DangerTaskApplicationTable_T where TaskID='" + TaskID + "'", "Applicant");
            }
            String returntext = associateMangementService.returnApplication(ApprovalID, SubmitterID, Applicant, ApproveSuggestion, Finished) > 0 ? "success" : "error";

            Collection<String> alias = new ArrayList<>();
            Collection<String> tags = new ArrayList<>();
            alias.add(SubmitterID);
            PushResult TST = JiguangPush.push(alias, null, "您有一条危险作业申请被退回！");

            if (request.getParameter("tag").equals("Mobile")) {
                HashMap<String, String> re = new HashMap<>();
                re.put("tag", "callbackSave");
                re.put("data", returntext);
                JSONArray json = JSONArray.fromObject(re);
                response.setContentType("charset=utf-8");
                response.getWriter().write(json.toString());
            } else {
                response.setContentType("charset=utf-8");
                response.getWriter().write(returntext);
            }
        } catch (Exception e) {
            response.setContentType("charset=utf-8");
            response.getWriter().write("error");
        }
    }

}
