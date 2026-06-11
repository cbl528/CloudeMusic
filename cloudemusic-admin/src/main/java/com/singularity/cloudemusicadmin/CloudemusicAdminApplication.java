package com.singularity.cloudemusicadmin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.singularity.cloudemusicadmin.mapper")
public class CloudemusicAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudemusicAdminApplication.class, args);
    }

}
