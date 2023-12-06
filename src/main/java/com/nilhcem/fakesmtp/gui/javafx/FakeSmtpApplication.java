package com.nilhcem.fakesmtp.gui.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;

@Slf4j
public class FakeSmtpApplication extends Application {

    @Override
    public void init() {
        log.debug("{} - JavaFX init", this.getClass().getSimpleName());
    }

    @Override
    public void start(Stage stage) throws Exception {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        String welcomeMsg = "GUI made with JavaFX %s, running on Java %s.".formatted(javafxVersion, javaVersion);
        log.info(welcomeMsg);

        Properties configProperties = loadConfigProperties();

        // for i18n
        ResourceBundle messages = ResourceBundle.getBundle("i18n/messages");

        FXMLLoader fxmlLoader = new FXMLLoader(FakeSmtpApplication.class.getResource("fake-smtp.fxml"), messages);
        Scene scene = new Scene(fxmlLoader.load(), 640, 400);
        stage.setTitle(configProperties.getProperty("application.title"));
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        log.debug("{} - JavaFX stop", this.getClass().getSimpleName());
    }

    public static void main(String[] args) {
        launch(args);
    }

    static Properties loadConfigProperties() {
        Properties configProperties = new Properties();
        try {
            configProperties.load(FakeSmtpApplication.class.getResourceAsStream("/configuration.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return configProperties;
    }

}
