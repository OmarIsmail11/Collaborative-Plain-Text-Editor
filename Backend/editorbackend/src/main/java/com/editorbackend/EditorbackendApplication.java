package com.editorbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
	
@SpringBootApplication(scanBasePackages = {"com.editorbackend", "com.editorbackend.Controllers"})
public class EditorbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EditorbackendApplication.class, args);
	}

}
