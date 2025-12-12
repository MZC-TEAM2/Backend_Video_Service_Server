package com.teambind.springproject;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Video Service Application.
 * Handles video upload progress, watch session management,
 * fraud detection, and learning progress tracking.
 */
@SpringBootApplication
public class SpringProjectApplication {

  /**
   * Application entry point.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure()
        .ignoreIfMissing()
        .load();

    dotenv.entries().forEach(entry ->
        System.setProperty(entry.getKey(), entry.getValue())
    );

    SpringApplication.run(SpringProjectApplication.class, args);
  }

}
