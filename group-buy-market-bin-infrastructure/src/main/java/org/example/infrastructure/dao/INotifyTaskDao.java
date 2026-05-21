package org.example.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.example.domain.trade.model.entity.NotifyTaskEntity;
import org.example.infrastructure.dao.po.NotifyTask;

import java.util.List;

/**
 * @ClassName : INotifyTaskDao
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/7  21:11
 */
@Mapper
public interface INotifyTaskDao {

    void insert(NotifyTask notifyTask);

    List<NotifyTask> queryUnExecutedNotifyTaskList();

    NotifyTask queryUnExecutedNotifyTaskByTeamId(String teamId);

    int updateNotifyTaskStatusSuccess(NotifyTask notifyTask);

    int updateNotifyTaskStatusError(NotifyTask notifyTask);

    int updateNotifyTaskStatusRetry(NotifyTask notifyTask);

}
