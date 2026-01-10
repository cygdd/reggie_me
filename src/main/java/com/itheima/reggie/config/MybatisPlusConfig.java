package com.itheima.reggie.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置MP的分页插件
 */
@Configuration
public class MybatisPlusConfig {
    // 设置为Bean，受管理权限
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        // 1.创建一个大型Interceptor
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 2.添加PaginationInnerInterceptor进Interceptor里即可
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // 3.返回Interceptor
        return mybatisPlusInterceptor;
    }
}
