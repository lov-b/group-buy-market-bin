package org.example.test.infrastructure.dao;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.infrastructure.dao.IGroupBuyActivityDao;
import org.example.infrastructure.dao.IGroupBuyDiscountDao;
import org.example.infrastructure.dao.po.GroupBuyActivity;
import org.example.infrastructure.dao.po.GroupBuyDiscount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : GroupBuyDiscountDaoTest
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/23  11:30
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GroupBuyDiscountDaoTest {

    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;

    @Test
    public void test_queryGroupBuyDiscountList() {
        List<GroupBuyDiscount> groupBuyActivities = groupBuyDiscountDao.queryGroupBuyDiscountList();
        log.info("测试结果：{}", JSON.toJSONString(groupBuyActivities));
    }
}
