package org.example.infrastructure.dcc;

import cn.bugstack.wrench.dynamic.config.center.types.annotations.DCCValue;
import org.example.types.common.Constants;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName : DCCService
 * @Description :
 * @Author : Bingo
 * @Date: 2025/12/31  17:09
 */
@Service
public class DCCService {

    //降级开关
    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;

    //切量开关
    @DCCValue("cutRange:100")
    private String cutRange;

    //渠道黑名单 - 可以配置多个渠道，用都好分隔
    @DCCValue("scBlacklist:s02c02")
    private String scBlacklist;

    @DCCValue("cacheOpenSwitch:1")
    private String cacheOpenSwitch;

    //提供外部访问 是否降级
    public boolean isDowngradeSwitch() {
        return "1".equals(downgradeSwitch);
    }

    public boolean isCutRange(String userId) {

        //取用户id哈希值的后两位作为对用户切量的标准
        int hashCode = Math.abs(userId.hashCode());

        int lastTwoDigits = hashCode % 100;

        // 哈希后两位小于切量值 返回true
        if (lastTwoDigits <= Integer.parseInt(cutRange)) {
            return true;
        }
        return false;
    }

    public boolean isScBlacklist(String source, String channel) {

        List<String> list = Arrays.asList(scBlacklist.split(Constants.SPLIT));
        return list.contains(source + channel);

    }

    public boolean isCacheOpenSwitch() {
        // 0关闭 1开启缓存
        return "1".equals(cacheOpenSwitch);
    }
}
