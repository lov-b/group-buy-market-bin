package org.example.domain.trade.service;

import org.example.domain.trade.model.entity.NotifyTaskEntity;

import java.util.Map;

/**
 * @ClassName : ITradeTaskService
 * @Description : 交易任务（MT/HTTP）服务接口
 * @Author : Bingo
 * @Date: 2026/2/13  10:15
 */
public interface ITradeTaskService {

    /**
     * 执行结算通知任务
     *
     * @return 结算数量
     * @throws Exception
     */
    Map<String, Integer> execNotifyJob() throws Exception;


    /**
     * 执行结算通知任务
     *
     * @param teamId 指定结算组ID
     * @return 结算数量
     * @throws Exception
     */
    Map<String, Integer> execNotifyJob(String teamId) throws Exception;


    /**
     * 执行结算通知任务
     *
     * @param notifyTaskEntity 通知任务对象
     * @return 结算数量
     * @throws Exception
     */
    Map<String, Integer> execNotifyJob(NotifyTaskEntity notifyTaskEntity) throws Exception;

}
