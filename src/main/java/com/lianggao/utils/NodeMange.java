package com.lianggao.utils;

import com.lianggao.bean.Application;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:applicationContext.xml",
        "classpath:SpringMVC.xml"
})
@Slf4j
public class NodeMange {
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private HistoryService historyService;

    /**
     * 使用代码动态增加节点
     */
    @Test
    public void addNode(){
        // 创建开始
        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");
        startEvent.setName("startEvent");
        // 创建初级审批人
        UserTask third = new UserTask();
        third.setId("third");
        third.setName("third");
        third.setAssignee("tanghao");
        // 创建次级审批成员
        UserTask fourth = new UserTask();
        fourth.setId("fourth");
        fourth.setName("fourth");
        fourth.setAssignee("fengxinxin");
        // 创建结束点
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");
        endEvent.setName("endEvent");

        // 创建连线
        SequenceFlow s1 = new SequenceFlow();
        s1.setId("s1");
        s1.setName("s1");
        s1.setSourceRef("startEvent");
        s1.setTargetRef("third");

        SequenceFlow s2 = new SequenceFlow();
        s2.setId("s2");
        s2.setName("s2");
        s2.setSourceRef("third");
        s2.setTargetRef("fourth");

        SequenceFlow s3 = new SequenceFlow();
        s3.setId("s3");
        s3.setName("s3");
        s3.setSourceRef("fourth");
        s3.setTargetRef("endEvent");

        // 连接Task
        List<SequenceFlow> start2first = new ArrayList<>();
        start2first.add(s1);
        startEvent.setOutgoingFlows(start2first);
        third.setIncomingFlows(start2first);

        List<SequenceFlow> first2Sencond = new ArrayList<>();
        first2Sencond.add(s2);
        third.setOutgoingFlows(first2Sencond);
        fourth.setIncomingFlows(first2Sencond);

        List<SequenceFlow> second2End = new ArrayList<>();
        second2End.add(s3);
        fourth.setOutgoingFlows(second2End);
        endEvent.setIncomingFlows(second2End);


        // 创建流程
        Process process = new Process();
        process.setName("Fourth");
        process.setId("Fourth");
        process.addFlowElement(startEvent);
        process.addFlowElement(third);
        process.addFlowElement(fourth);
        process.addFlowElement(endEvent);
        process.addFlowElement(s1);
        process.addFlowElement(s2);
        process.addFlowElement(s3);
        // 创建Bpmnmodel
        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);

        processEngine.getRepositoryService().createDeployment()
                .name("bpmn")
                .addBpmnModel("Fourth.bpmn", bpmnModel) // 这个addBpmnModel第一个参数一定要带后缀.bpmn
                .deploy().getId();


    }

    /**
     * 使用代码动态增加节点
     * Applicant:创建危险作业申请人编号
     * ApproverList:险作业申请审批人编号列表
     * String Applicant, List<String> ApproverList
     */
    @Test
    public void addNodeDynamic(){
        String Applicant = "lianggao";
        List<String> ApproverList = new ArrayList<>();
        ApproverList.add("tanghao");
        ApproverList.add("liushuai");
        ApproverList.add("wangwu");
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
        Process process = new Process();
        process.setName("Fifth");
        process.setId("Fifth");
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
        processEngine.getRepositoryService().createDeployment()
                .name("bpmn")
                .addBpmnModel("Fifth.bpmn", bpmnModel) // 这个addBpmnModel第一个参数一定要带后缀.bpmn
                .deploy().getId();


    }


    public String addNodeDynamic2(String Applicant, List<String>ApproverList){

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
        Process process = new Process();
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

    }



    /**
     * 根据危险作业审批人列表创建审批节点列表
     * List<String>ApproverList:危险作业审批人列表
     * List<UserTask>:工作流审批节点列表
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
     * String applicant:申请人
     * List<String>ApproverList:危险作业审批人列表
     */
    public List<SequenceFlow> createSequenceFlowList(String applicant, List<String>ApproverList){
        List<SequenceFlow> sequenceFlowList = new ArrayList<>();
        int sequenceNumber = 1;
        int approverListLength = ApproverList.size();
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



    @Test
    public void startProcess(){
        String processKey="Fifth";
        //String processId
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey(processKey);
        System.out.println("getProcessDefinitionId "+processInstance.getProcessDefinitionId());
        System.out.println("getProcessDefinitionKey "+processInstance.getProcessDefinitionKey());
        System.out.println("getName "+processInstance.getName());
        System.out.println("getProcessDefinitionName "+processInstance.getProcessDefinitionName());
        System.out.println("getDeploymentId "+processInstance.getDeploymentId());

    }
    @Test
    public void isProcessActive() {
        String processInstanceId = "95001";
        ProcessInstance pi = processEngine.getRuntimeService()//表示正在执行的流程实例和执行对象
                .createProcessInstanceQuery()//创建流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();

        if (pi == null) {
            log.info("流程已经结束");
        } else {
            log.info("流程没有结束");
            //获取任务状态
            log.info("节点id：" + pi.getActivityId());
        }
    }

    /**
     * 任务交接,只能将当阶段的任务交接给下一个人实现
     */
    @Test
    public void turnTask(){
        String assignee = "yangming";
        List<Task> taskList = processEngine.getTaskService()
                .createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskCreateTime()
                .asc()
                .list();
        if(taskList!=null&&taskList.size()>0){
            for(Task task:taskList){
                log.info("getProcessInstanceId：" + task.getProcessInstanceId());
                log.info("getAssignee：" + task.getAssignee());
                log.info("getId：" + task.getId());
                taskService.setAssignee(task.getId(), "yangming");
            }
        }

    }

    /**
     * 完成任务
     */
    @Test
    public void completePersonalTask() {
        List <Task> List = processEngine.getTaskService()
                .createTaskQuery()
                .taskAssignee("a4")
                .orderByTaskCreateTime().asc()
                .list();
        if(List != null&&List.size()>0){
            for(Task task: List){
                log.info("完成任务Id"+ task.getId());
                taskService.complete(task.getId());
            }
        }
    }

    private List<HistoricActivityInstance> getHisUserTaskActivityInstanceList(String processInstanceId) {
        List<HistoricActivityInstance> hisActivityInstanceList = ((HistoricActivityInstanceQuery) historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).activityType("userTask")
                .finished().orderByHistoricActivityInstanceEndTime().desc())
                .list();
        return hisActivityInstanceList;
    }

    /**
     * 历史活动查询接口
     */
    @Test
    public void findHistoryActivity() {
        String processInstanceId = "15001";
        List<HistoricActivityInstance> hais = processEngine.getHistoryService()//
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        for (HistoricActivityInstance hai : hais) {
            log.info("活动id：" + hai.getActivityId()
                    + "   审批人：" + hai.getAssignee()
                    + "   任务id：" + hai.getTaskId());
            System.out.println(hai.getStartTime());
            System.out.println(hai.getEndTime());
            log.info("************************************");
        }
    }
    /**
     * 通过BusinessKey启动实例，可以启动多个实例
     * 不能直接通过BusinessKey启动，不然别人申请进行部署时，可能会得到启动别人的部署对象
     * 需要根据act_re_procdef中的ID_进行启动
     */
    @Test
    public void startProcessByBusinessKey(){
        String processKey="Fifth";
        //String processId
        Application application = new Application();
        application.setDangerTaskName("爆破作业");
        application.setApplicant(2);
        application.setStartTime("2021-01-02");
        application.setEndTime("2021-01-03");
        application.setState(0);


        //这里需要存入Application表里面
        Map<String, Object> variables = new HashMap<>();
        variables.put("application", application);

        //部署完成后会有一个部署ID
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("Apply",variables);
        if(processInstance!=null){
            System.out.println(processInstance.getId()+"启动成功");
        }


    }

    /**
     * 根据proceInstance.ID和用户名来获得用户需要执行的任务，并且在comment表中添加评论
     */
    @Test
    public void queryAndCompleteTask(){
        TaskQuery taskQuery = processEngine.getTaskService().createTaskQuery();
        taskQuery.taskAssignee("tanghao");
        taskQuery.processInstanceId("17504");
        Task task = taskQuery.singleResult();
        String taskId = task.getId();
        System.out.println("taskId"+taskId);
        Authentication.setAuthenticatedUserId("tanghao");
        processEngine.getTaskService().addComment(taskId, "17504","我是唐豪，我同意通过");
        processEngine.getTaskService().complete(taskId);
    }

    /**
     * 根据姓名和实例ID，让用户完成当前的任务，并且复制评论，修改时间还没有完成
     */
    public void queryAndCompleteTask2(String Assignee, String InstanceID, Comment comment){
        System.out.println("Assignee "+Assignee+" InstanceID "+InstanceID);
        TaskQuery taskQuery = processEngine.getTaskService().createTaskQuery();
        taskQuery.taskAssignee(Assignee);
        taskQuery.processInstanceId(InstanceID);
        Task task = taskQuery.singleResult();
        String taskId = task.getId();
        System.out.println("taskId"+taskId);


        Authentication.setAuthenticatedUserId(comment.getUserId());//这里设置用户的姓名或者ID
        processEngine.getTaskService().addComment(taskId, InstanceID,comment.getFullMessage());
        //processEngine.getTaskService().deleteComment(comment.getTaskId());
        processEngine.getTaskService().complete(taskId);

    }
    @Test
    public void isProcessActive2() {
        String processInstanceId = "127501";
        ProcessInstance pi = processEngine.getRuntimeService()//表示正在执行的流程实例和执行对象
                .createProcessInstanceQuery()//创建流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();

        if (pi == null) {
            log.info("流程已经结束");
        } else {
            log.info("流程没有结束");
            //获取任务状态
            log.info("节点id：" + pi.getActivityId());
        }
    }



    /**
     * 找到该实例已经完成的任务和下一个将要进行的任务
     * 查找所有
     */
    @Test
    public void  findAllApprover(){
        List<HistoricTaskInstance> historicTaskInstances = processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceId("112501")
                .finished()
                .list();
        List<HistoricTaskInstance> historicTaskInstances2 = processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceId("127501")
                .unfinished()
                .list();

        for(HistoricTaskInstance  historicTaskInstance:historicTaskInstances){
            System.out.println("1"+historicTaskInstance.getAssignee()+" "+historicTaskInstance.getId());
            try{
                /*Comment comment = processEngine.getTaskService().getComment("132504");
                System.out.println(comment.getFullMessage());*/
                /*List<Comment> commentList = processEngine.getTaskService().getTaskComments("112501");
                System.out.println(commentList.get(0).getFullMessage());*/

            }
            catch (Exception e){
                System.out.println(e.fillInStackTrace());
            }
            //System.out.println("1"+historicTaskInstance.getAssignee()+historicTaskInstance.getId());
        }
        for(HistoricTaskInstance  historicTaskInstance:historicTaskInstances2){
            System.out.println("2"+historicTaskInstance.getAssignee());
        }

    }


    /**
     * 根据现在的任务ID获得该实例所有的评论
     */
    @Test
    public void findCommentByTaskId() {

        String taskId="135004"; // 现在的任务id
        HistoryService historyService=processEngine.getHistoryService();
        TaskService taskService=processEngine.getTaskService();
        List <Comment>list = new ArrayList();
        //使用当前的任务ID，查询当前流程对应的历史任务ID

        //使用当前任务ID，获取当前任务对象
        Task task = taskService.createTaskQuery()//
                .taskId(taskId)//使用任务ID查询
                .singleResult();
        //获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        /*//使用流程实例ID，查询历史任务，获取历史任务对应的每个任务ID
        List <HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()//历史任务表查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .list();
        //遍历集合，获取每个任务ID
        if(htiList!=null && htiList.size()>0){
            for(HistoricTaskInstance hti:htiList){
                 //任务ID
                String htaskId = hti.getId();
                //System.out.println("ID "+htaskId);
                //获取批注信息
                List<Comment>taskList = taskService.getTaskComments(htaskId);//对用历史完成后的任务ID
                list.addAll(taskList);
            }
        }*/
        list = taskService.getProcessInstanceComments(processInstanceId);


        for(Comment com:list){
            System.out.println("ID:"+com.getId());
            System.out.println("Message:"+com.getFullMessage());
            System.out.println("TaskId:"+com.getTaskId());
            System.out.println("ProcessInstanceId:"+com.getProcessInstanceId());
            System.out.println("UserId:"+com.getUserId());
        }

        System.out.println(list);
    }

    /**
     * 根据InstanceID找到所有的评论返回
     */

    public List<Comment> findCommentByInstanceId(String processInstanceId) {
        //
        List <Comment>list = new ArrayList();
        list = taskService.getProcessInstanceComments(processInstanceId);
        for(Comment com:list){
            System.out.println("ID:"+com.getId());
            System.out.println("Message:"+com.getFullMessage());
            System.out.println("TaskId:"+com.getTaskId());
            System.out.println("ProcessInstanceId:"+com.getProcessInstanceId());
            System.out.println("UserId:"+com.getUserId());

        }

        System.out.println(list);
        return list;
    }
    /**
     * 当当前实例即将完成，需要增加节点的时候，重新设置一个实例，关闭当前实例
     */
    @Test
    public void copyInstance(){
        List<HistoricTaskInstance> historicTaskInstances = processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceId("4")
                .finished()
                .list();
        List<String>ApproverList = new ArrayList<>();
        String applicant ="";
        for(HistoricTaskInstance historicTaskInstance:historicTaskInstances){
            System.out.println(historicTaskInstance.getAssignee());
            System.out.println(historicTaskInstance.getStartTime());
            System.out.println(historicTaskInstance.getEndTime());
            ApproverList.add(historicTaskInstance.getAssignee());

        }
        applicant = ApproverList.get(0);
        ApproverList.remove(0);
        System.out.println("================");
        System.out.println(applicant);
        for(String approver:ApproverList){
            System.out.println(approver);
        }
        String processInstanceId = addNodeDynamic2(applicant, ApproverList);
        System.out.println(processInstanceId);
        /*List<Comment>commentList = findCommentByInstanceId(processInstanceId);
        for(Comment comment:commentList){
            queryAndCompleteTask2(comment.getUserId(), processInstanceId, comment);
        }*/

    }


    /**
     * 根据deployment_Id找到procdef_Id
     * 根据act_re_procedef中的ID进行启动
     * 返回InstanceID
     */
    public String startProcessByID(String deploymentID){
        String processKey="Fifth";
        //150001
        Application application = new Application();
        application.setDangerTaskName("爆破作业");

        application.setApplicant(2);

        application.setStartTime("2021-01-02");
        application.setEndTime("2021-01-03");
        application.setState(0);


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

    /**
     * 创建并且启动实例
     */
    @Test
    public void createDeployAndStartInstance(){
        String Applicant = "lianggao";
        List<String> ApproverList = new ArrayList<>();
        ApproverList.add("tanghao");
        ApproverList.add("liushuai");
        ApproverList.add("wangwu");
        addNodeDynamic2(Applicant, ApproverList);

    }

    /**
     * 为通过复制已经启动的实例复制评论, 并且删除原来的评论
     */
    @Test
    public void addComment(){
        String copiedProcessInstanceID = "4";
        String currentProcessInstanceId = "17501";
        List<Comment>commentList = findCommentByInstanceId(copiedProcessInstanceID);
        //commentList.
        Collections.reverse(commentList);
        for(Comment comment:commentList){
            queryAndCompleteTask2(comment.getUserId(), currentProcessInstanceId, comment);
        }
    }

    /**
     * 根据processDefineId找到所有的节点
     */
    @Test
    public void queryAllNode(){
        String processDefineId = "Apply:3:17503";
        BpmnModel bpmnModel = processEngine.getRepositoryService().getBpmnModel(processDefineId);
        if(bpmnModel!=null){
            Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();
            for(FlowElement e : flowElements) {
                //System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
                if((e.getClass().toString()).equals("class org.activiti.bpmn.model.UserTask")){
                    System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
                }

            }

        }
    }

    /**
     * 查询实例对应的BusinessKey
     */
    @Test
    public void findBusinesskeyByInstanceID(){
        //ProcessInstance processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId("");
    }
    @Test
    public void findAllInstance(){
        List<ProcessInstance> processInstanceList = processEngine.getRuntimeService().createProcessInstanceQuery().list();
        List<HistoricProcessInstance>  historicProcessInstanceList = processEngine.getHistoryService().createHistoricProcessInstanceQuery().list();
        for(HistoricProcessInstance historicProcessInstance:historicProcessInstanceList){
            System.out.println(historicProcessInstance.getId());
            System.out.println(historicProcessInstance.getName());
            System.out.println(historicProcessInstance.getBusinessKey());
            System.out.println(historicProcessInstance.getProcessVariables());
            System.out.println(historicProcessInstance.getBusinessKey());
            Application application = (Application)processEngine.getTaskService().getVariable(historicProcessInstance.getId(), "application");

            //Application application = (Application)processEngine.getTaskService().getVariables();
        }
    }

    /**
     * 只能查询当前走到了那个节点
     */
    @Test
    public void queryAllTask(){
        String processId= "4";
        System.out.println("=========================================1");
        List<Task>taskList = processEngine.getTaskService().createTaskQuery().processInstanceId(processId).list();
        System.out.println("=========================================2");
        for(Task task:taskList){
            System.out.println("=========================================3");
            System.out.println(task.getId());
            System.out.println(task.getAssignee());
            System.out.println(task.getName());
        }

    }
}
