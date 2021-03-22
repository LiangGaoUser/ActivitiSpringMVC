package com.lianggao.utils;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.task.Task;

import java.util.List;

public class Node {
    public void queryAllTask(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        String processId= "12504";
        System.out.println("=========================================1");
        List<Task> taskList = processEngine.getTaskService().createTaskQuery().processInstanceId(processId).list();
        System.out.println("=========================================2");
        for(Task task:taskList){
            System.out.println("=========================================3");
            System.out.println(task.getId());
            System.out.println(task.getAssignee());
            System.out.println(task.getName());
        }

    }
    public static void main(String args[]){
        Node node = new Node();
        node.queryAllTask();
    }

}
