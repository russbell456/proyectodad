package org.example.msventa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "org.example.msventa.feign")
@SpringBootApplication
public class MsVentaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsVentaApplication.class, args);
    }
}