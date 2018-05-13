package com.cloud.uaa.config;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.cloud.uaa")
public class FeignConfiguration {

}
