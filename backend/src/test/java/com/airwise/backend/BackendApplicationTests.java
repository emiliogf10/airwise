package com.airwise.backend;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
		+ "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
class BackendApplicationTests {

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Test
	void demoContextLoadsWithoutPersistence() {
		assertTrue(applicationContext.isActive());
		assertNotNull(applicationContext.getBean(BackendApplication.class));
		assertTrue(applicationContext.getBeansOfType(DataSource.class).isEmpty());
	}

}
