package com.swiftcast;

import com.swiftcast.ui.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SwiftCastApplication extends Application {

    private static ConfigurableApplicationContext springContext;
    private static String[] args;

    public static void main(String[] args) {
        SwiftCastApplication.args = args;
        // JavaFX 애플리케이션 시작
        Application.launch(SwiftCastApplication.class, args);
    }

    @Override
    public void init() throws Exception {
        // Spring Boot 컨텍스트 시작 (JavaFX 초기화 전)
        springContext = SpringApplication.run(SwiftCastApplication.class, args);
    }

    @Override
    public void start(javafx.stage.Stage primaryStage) throws Exception {
        // JavaFX UI 초기화
        MainWindow mainWindow = springContext.getBean(MainWindow.class);
        mainWindow.start(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        // Spring 컨텍스트 종료
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }
}
