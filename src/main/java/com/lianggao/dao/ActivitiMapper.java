package com.lianggao.dao;

import com.lianggao.bean.Application;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 因为增加动态审批人,不可避免需要修改评论完成的时间，来同步时间。
 */
public interface ActivitiMapper {
    //根据instanceid和processid修改评论的时间
    void updateCommentTime(@Param("TIME_") String TIME_, @Param("TASK_ID_")String TASK_ID_, @Param("PROC_INST_ID_")String PROC_INST_ID_);
    //查询act_hi_procinst判断该实例是否被删除,如果返回的"deleted"，则表示该实例已经被删除
    String selectDeleteReason(@Param("ID_") String ID_);

}