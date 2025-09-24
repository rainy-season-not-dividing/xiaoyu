package com.xiaoyu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoyu.controller.user.UserController;
import com.xiaoyu.entity.UsersPO;
import com.xiaoyu.result.Result;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
class XiaoyuServerApplicationTests {

	@Resource
	private UserController userController;

	@Resource
	private ObjectMapper objectMapper;

	@Test
	void testGetUserPublicInfo() throws InterruptedException {
		Result<UsersPO> result = userController.getUserSelfInfo();
		System.out.println(result);
	}

	@Test
	void contextLoads() throws InterruptedException, JsonProcessingException {
		testGetUserPublicInfo();
		testGetUserPublicInfo();
//		String json = "{\"@clazz\":\"com.xiaoyu.entity.UsersPO\",\"id\":1,\"birthday\":\"2025-09-20\",\"updatedAt\":\"2025-09-20 19:21:54\"}";
//		UsersPO po = objectMapper.readValue(json, UsersPO.class);
//		System.out.println(po.getUpdatedAt());   // 应该打印 LocalDateTime
	}



}
