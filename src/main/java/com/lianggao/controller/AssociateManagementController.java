package com.lianggao.controller;

import com.lianggao.bean.Application;
import com.lianggao.bean.ApplicationInstance;
import com.lianggao.bean.UserInfo;
import com.lianggao.dao.ApplicationMapper;
import com.lianggao.dao.UserInfoMapper;
import com.lianggao.service.AssociateMangementService;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理危险作业相关
 * 1.提交危险作业申请 2.
 */
@Controller
@Scope("prototype")
public class AssociateManagementController {
    @Autowired
    private AssociateMangementService associateMangementService;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @RequestMapping("/DangerTaskApplyMan/SubmitAdd")
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
        applicationMapper.insert(application);
        String BusinessKey = application.getDangerTaskId().toString();
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
    }
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






}
