package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "shop related apis")
@Slf4j
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("set shop operation status")
    public Result setStatus(@PathVariable Integer status){
        log.info("shop status changed to: {}", status);
        redisTemplate.opsForValue().set(KEY, String.valueOf(status));
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("get shop operation status")
    public Result<Integer> getStatus(){
        Object value = redisTemplate.opsForValue().get(KEY);
        Integer status = value == null ? null : Integer.valueOf(value.toString());
        return Result.success(status);
    }


}
