package com.nilhcem.fakesmtp.gui.javafx;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.nilhcem.fakesmtp.gui.javafx.FakeSmtpApplication.loadConfigProperties;

@Slf4j
public class FakeSmtpController implements Initializable {

    private final BooleanProperty serverStarting = new SimpleBooleanProperty(false);
    private final LongProperty numberMailsReceived = new SimpleLongProperty(0);
    private final StringProperty emailsDirectoryProperty = new SimpleStringProperty("---");

    @FXML
    public Label listeningPortLabel;

    @FXML
    public ProgressIndicator startProgressIndicator;

    @FXML
    public TextField portNumberTextField;

    @FXML
    public Button startServerButton;

    @FXML
    public Button emailsDirChooserButton;

    @FXML
    public Label numberMailsReceivedLabel;

    @FXML
    public TextField emailsDirectoryTextField;

    @FXML
    public Button clearAllButton;

    @FXML
    public Button quitButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Properties configProperties = loadConfigProperties();
        String appName = configProperties.getProperty("application.name");
        String appVersion = configProperties.getProperty("application.version");
        // welcomeLabel.setText("%s – v%s".formatted(appName, appVersion));

        String smtpDefaultPort = configProperties.getProperty("smtp.default.port");
        portNumberTextField.setText(smtpDefaultPort);

        String emailsDefaultDir = resourceBundle.getString("emails.default.dir");
        System.out.println("Default dir = " + emailsDefaultDir);
        emailsDirectoryProperty.set(emailsDefaultDir);

        // crashes SceneBuilder in FXML ?
        portNumberTextField.setTooltip(new Tooltip(resourceBundle.getString("porttextfield.tooltip")));
        emailsDirChooserButton.setTooltip(new Tooltip(resourceBundle.getString("savemsgfield.tooltip")));

        // startProgressIndicator.setVisible(false);
        startProgressIndicator.visibleProperty().bind(serverStarting);
        startServerButton.disableProperty().bind(serverStarting);
        emailsDirChooserButton.disableProperty().bind(serverStarting);

        emailsDirectoryTextField.textProperty().bind(emailsDirectoryProperty);

        numberMailsReceivedLabel.textProperty().bind(numberMailsReceived.asString());

        Platform.runLater(() -> startServerButton.requestFocus());
    }

    public void onStartServer(ActionEvent actionEvent) {
        Runnable bgTask = () -> {
            Platform.runLater(() -> {
                // reset focus
                listeningPortLabel.requestFocus();
                serverStarting.setValue(true);
            });
            try {
                Thread.sleep(1000);
                log.info("Done !");
                System.out.println("--> " + numberMailsReceived.get());
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            Platform.runLater(() -> {
                numberMailsReceived.set(numberMailsReceived.get() + 1);
                serverStarting.setValue(false);
            });
        };
        new Thread(bgTask).start();
    }

    public void onChooseEmailsDirecory(ActionEvent actionEvent) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        final File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            String absolutePath = selectedDirectory.getAbsolutePath();
            emailsDirectoryProperty.set(absolutePath);
        }
    }

    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }

}
