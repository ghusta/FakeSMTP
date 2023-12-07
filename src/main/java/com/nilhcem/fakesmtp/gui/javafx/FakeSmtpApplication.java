package com.nilhcem.fakesmtp.gui.javafx;

import com.nilhcem.fakesmtp.core.ArgsHandler;
import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.server.SMTPServerHandler;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

@Slf4j
public class FakeSmtpApplication extends Application {

    static HostServices hostServices;

    private FakeSmtpController mainController;

    @Override
    public void init() {
        log.debug("{} - JavaFX init", this.getClass().getSimpleName());

        // keep a reference on HostServices, to call showDocument() later
        hostServices = getHostServices();

        // preload Configuration
        @SuppressWarnings("unused")
        Configuration configuration = Configuration.getInstance();
    }

    @Override
    public void start(Stage stage) throws Exception {
        log.debug("Current locale is : " + Locale.getDefault());
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        String welcomeMsg = "GUI made with JavaFX %s, running on Java %s.".formatted(javafxVersion, javaVersion);
        log.info(welcomeMsg);

        Properties configProperties = Configuration.getInstance().getAllProperties();
        String appName = configProperties.getProperty("application.name");
        String appTitle = configProperties.getProperty("application.title");
        String appVersion = configProperties.getProperty("application.version");

        // for i18n
        ResourceBundle messages = ResourceBundle.getBundle("i18n/messages");

        // app icons
        stage.getIcons().add(new Image(Objects.requireNonNull(FakeSmtpApplication.class.getResourceAsStream("/icon.gif"))));

        FXMLLoader fxmlLoader = new FXMLLoader(FakeSmtpApplication.class.getResource("fake-smtp.fxml"), messages);
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        scene.getStylesheets().add(Objects.requireNonNull(FakeSmtpApplication.class.getResource("styles.css")).toExternalForm());
        stage.setTitle("%s - %s".formatted(appTitle, appVersion));
        stage.setScene(scene);
        mainController = fxmlLoader.getController();
        stage.show();
    }

    @Override
    public void stop() {
        log.debug("Closing the application and saving the configuration");

        if (mainController != null) {
            Configuration.getInstance().set(Configuration.Settings.SMTP_DEFAULT_PORT, mainController.getPortNumberTextField().getText());
            Configuration.getInstance().set(Configuration.Settings.EMAILS_DEFAULT_DIR, mainController.getEmailsDirectoryTextField().getText());
        }

        try {
            Configuration.getInstance().saveToUserProfile();
        } catch (IOException ex) {
            log.error("Could not save configuration", ex);
        }

        // try stop SMTPServer
        SMTPServerHandler.INSTANCE.stopServer();
    }

    public static void main(String[] args) throws ParseException {
        parseArgs(args);
        launch(args);
    }

    private static void parseArgs(String[] args) throws ParseException {
        ArgsHandler argsHandler = ArgsHandler.INSTANCE;
        argsHandler.handleArgs(args);
    }

}
