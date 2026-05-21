package org.example.trigger.http;

import cn.bugstack.wrench.dynamic.config.center.domain.model.valobj.AttributeVO;
import lombok.extern.slf4j.Slf4j;
import org.example.api.IDCCService;
import org.example.api.response.Response;
import org.example.types.enums.ResponseCode;
import org.redisson.api.RTopic;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @ClassName : DCCController
 * @Description :
 * @Author : Bingo
 * @Date: 2026/1/3  21:08
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/dcc")
public class DCCController implements IDCCService {

    @Resource
    private RTopic dccTopic;

    @RequestMapping(value = "update_config", method = RequestMethod.GET)
    @Override
    public Response<Boolean> updateConfig(@RequestParam String key, @RequestParam String value) {
        try {
            log.info("DCC 动态配置值变更 key:{} value:{}", key, value);
//            dccTopic.publish(key + "," + value);
            dccTopic.publish(new AttributeVO(key, value));

            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("DCC 动态配置值变更失败 key:{} value:{}", key, value, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
