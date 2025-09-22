package com.xiaoyu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.xiaoyu", "com.xiaoyua"})
@MapperScan({"com.xiaoyu.**.mapper", "com.xiaoyua.**.mapper"})
@EnableElasticsearchRepositories(basePackages = "com.xiaoyua.es")
@EnableScheduling
public class XiaoyuServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(XiaoyuServerApplication.class, args);
	}

}
