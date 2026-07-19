package com.huigrowth.babycare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 好芽儿育儿平台主应用类
 * 
 * @author HuiGrowth Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class BabyCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(BabyCareApplication.class, args);
        System.out.println("========================================");
        System.out.println("🍼 好芽儿育儿平台后端服务启动成功! 🍼");
        System.out.println("📱 API文档地址: http://localhost:8080/swagger-ui.html");
        System.out.println("📊 监控地址: http://localhost:8080/actuator");
        System.out.println("========================================");
    }
}