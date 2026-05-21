package org.example.domain.trade.service.refund;

import cn.bugstack.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import org.example.domain.trade.adapter.repository.ITradeRepository;
import org.example.domain.trade.model.entity.*;
import org.example.domain.trade.model.valobj.TeamRefundSuccess;
import org.example.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import org.example.domain.trade.service.ITradeRefundOrderService;
import org.example.domain.trade.service.refund.business.IRefundOrderStrategy;
import org.example.domain.trade.service.refund.factory.TradeRefundRuleFilterFactory;
import org.example.types.enums.GroupBuyOrderEnumVO;
import org.example.domain.trade.model.valobj.RefundTypeEnumVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName : TradeRefundOrderService
 * @Description : 退单，逆向流程服务
 * @Author : Bingo
 * @Date: 2026/2/12  9:02
 */
@Slf4j
@Service
public class TradeRefundOrderService implements ITradeRefundOrderService {


    private final ITradeRepository repository;
    // Spring 自动将同接口的多个服务注入 Map
    private final Map<String, IRefundOrderStrategy> refundOrderStrategyMap;

    @Resource
    private BusinessLinkedList<TradeRefundCommandEntity, TradeRefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> tradeRefundRuleFilter;

    public TradeRefundOrderService(ITradeRepository repository, Map<String, IRefundOrderStrategy> refundOrderStrategyMap) {
        this.repository = repository;
        this.refundOrderStrategyMap = refundOrderStrategyMap;
    }

    @Override
    public TradeRefundBehaviorEntity refundOrder(TradeRefundCommandEntity tradeRefundCommandEntity) throws Exception {
        log.info("逆向流程，退单操作 userId:{} outTradeNo:{}", tradeRefundCommandEntity.getUserId(), tradeRefundCommandEntity.getOutTradeNo());

        return tradeRefundRuleFilter.apply(tradeRefundCommandEntity, new TradeRefundRuleFilterFactory.DynamicContext());
    }

    @Override
    public void restoreTeamLockStock(TeamRefundSuccess teamRefundSuccess) throws Exception {
        log.info("逆向流程，恢复锁单量 userId:{} outTradeNo:{}", teamRefundSuccess.getUserId(), teamRefundSuccess.getTeamId(), teamRefundSuccess.getOrderId());

        String typeCode = teamRefundSuccess.getType();

        RefundTypeEnumVO refundTypeEnumVO = RefundTypeEnumVO.getRefundTypeEnumVOByCode(typeCode);

        IRefundOrderStrategy refundOrderStrategy = refundOrderStrategyMap.get(refundTypeEnumVO.getStrategy());

        // 根据返回的信息执行拼团可用库存的增加
        refundOrderStrategy.reverseStock(teamRefundSuccess);
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryTimeoutUnpaidOrderList() {
        log.info("扫描数据，超时组队未支付订单");
        return repository.queryTimeoutUnpaidOrderList();
    }
}
