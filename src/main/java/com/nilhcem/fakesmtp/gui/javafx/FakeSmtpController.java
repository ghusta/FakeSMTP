package com.nilhcem.fakesmtp.gui.javafx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.nilhcem.fakesmtp.gui.javafx.FakeSmtpApplication.loadConfigProperties;

public class FakeSmtpController implements Initializable {

    @FXML
    public Label welcomeLabel;

    @FXML
    public Label statusLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Properties configProperties = loadConfigProperties();
        String appName = configProperties.getProperty("application.name");
        String appVersion = configProperties.getProperty("application.version");
        welcomeLabel.setText("%s – v%s".formatted(appName, appVersion));
    }

}
