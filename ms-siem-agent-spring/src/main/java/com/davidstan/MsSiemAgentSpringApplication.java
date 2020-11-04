package com.davidstan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

import com.davidstan.socket.SSLClientSocketService;

@SpringBootApplication
@EnableDiscoveryClient
public class MsSiemAgentSpringApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(MsSiemAgentSpringApplication.class);
		SSLClientSocketService clientSocket = context.getBean(SSLClientSocketService.class);
		try {
			clientSocket.initClient();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
