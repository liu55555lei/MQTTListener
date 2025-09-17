package com.example.mqtt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import com.example.mqtt.service.ConfigService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.mqtt.mapper")
@EnableRetry
@EnableScheduling
public class MqttMysqlServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MqttMysqlServiceApplication.class, args);
    }

    @Bean
    public ApplicationRunner loadDbConfigOnStart(ConfigService configService) {
        return args -> configService.reloadAndApply();
    }
}


