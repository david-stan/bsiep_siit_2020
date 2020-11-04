package com.davidstan;

import javax.annotation.PostConstruct;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.davidstan.socket.SLLServerSocketService;

@Component
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MsServerCertApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(MsServerCertApplication.class);
		SLLServerSocketService socketService = context.getBean(SLLServerSocketService.class);
		try {
			socketService.initServer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
