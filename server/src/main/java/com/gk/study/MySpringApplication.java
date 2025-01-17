package com.gk.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lengqin1024(微信)
 * @email net936@163.com
 */
@SpringBootApplication
public class MySpringApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(MySpringApplication.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
