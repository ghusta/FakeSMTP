package com.nilhcem.fakesmtp.gui.javafx;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.nilhcem.fakesmtp.core.ArgsHandler;
import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.core.exception.BindPortException;
import com.nilhcem.fakesmtp.core.exception.InvalidHostException;
import com.nilhcem.fakesmtp.core.exception.InvalidPortException;
import com.nilhcem.fakesmtp.core.exception.OutOfRangePortException;
import com.nilhcem.fakesmtp.gui.reactive.ConsumerSubscriber;
import com.nilhcem.fakesmtp.log.SmtpLogPublisherAppender;
import com.nilhcem.fakesmtp.model.EmailModel;
import com.nilhcem.fakesmtp.model.UIModel;
import com.nilhcem.fakesmtp.server.MailSaver;
import com.nilhcem.fakesmtp.server.SMTPServerHandler;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class FakeSmtpController implements Initializable {

    private final BooleanProperty serverStarting = new SimpleBooleanProperty(false);
    private final BooleanProperty serverStarted = new SimpleBooleanProperty(false);
    private final StringProperty emailsDirectory = new SimpleStringProperty("---");
    private final BooleanProperty memoryModeEnabled = new SimpleBooleanProperty(false);

    @FXML
    private Label listeningPortLabel;
    @FXML
    private ProgressIndicator startProgressIndicator;
    @FXML
    @Getter
    private TextField portNumberTextField;
    @FXML
    private Button startServerButton;
    @FXML
    private Button emailsDirChooserButton;
    @FXML
    private Label numberMailsReceivedLabel;
    @FXML
    @Getter
    private TextField emailsDirectoryTextField;
    @FXML
    private Button clearAllButton;
    @FXML
    private Button quitButton;
    @FXML
    private TableView<EmailModel> emailsTableView;
    @FXML
    private TextArea lastMessageTextArea;
    @FXML
    private TextArea smtpLogTextArea;

    private ResourceBundle messages;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ArgsHandler argsHandler = ArgsHandler.INSTANCE;
        memoryModeEnabled.set(argsHandler.isMemoryModeEnabled());

        this.messages = resourceBundle;
        Properties configProperties = Configuration.getInstance().getAllProperties();
        String appName = configProperties.getProperty("application.name");
        String appVersion = configProperties.getProperty("application.version");
        // welcomeLabel.setText("%s – v%s".formatted(appName, appVersion));

        setupSmtpLogs();

        initEmailsTableView(emailsTableView);

        String smtpDefaultPort = configProperties.getProperty("smtp.default.port");
        portNumberTextField.setText(smtpDefaultPort);

        String emailsDefaultDir = configProperties.getProperty("emails.default.dir", messages.getString("emails.default.dir"));
        emailsDirectory.addListener((observable, oldValue, newValue) -> UIModel.INSTANCE.setSavePath(newValue));
        emailsDirectory.set(emailsDefaultDir);

        portNumberTextField.setTooltip(new Tooltip(resourceBundle.getString("porttextfield.tooltip")));
        emailsDirChooserButton.setTooltip(new Tooltip(resourceBundle.getString("savemsgfield.tooltip")));
        clearAllButton.setTooltip(new Tooltip(resourceBundle.getString("clearall.delete.email")));

        startProgressIndicator.visibleProperty().bind(serverStarting);
        startServerButton.disableProperty().bind(Bindings.or(serverStarting, serverStarted));
        emailsDirChooserButton.disableProperty().bind(Bindings.or(serverStarting, memoryModeEnabled));
        portNumberTextField.disableProperty().bind(serverStarted);
        BooleanBinding emailsTableViewEmpty = Bindings.isEmpty(emailsTableView.getItems());
        clearAllButton.disableProperty().bind(emailsTableViewEmpty);

        emailsDirectoryTextField.textProperty().bind(emailsDirectory);
        emailsDirectoryTextField.disableProperty().bind(memoryModeEnabled);

        portNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> UIModel.INSTANCE.setPort(newValue));

        IntegerBinding emailsTableViewSize = Bindings.size(emailsTableView.getItems());
        numberMailsReceivedLabel.textProperty().bind(emailsTableViewSize.asString());

        MailSaver mailSaver = SMTPServerHandler.INSTANCE.getMailSaver();
        mailSaver.getEmailPublisher().subscribe(new ConsumerSubscriber<>(emailModel ->
                Platform.runLater(() -> {
                    emailsTableView.getItems().add(emailModel);
                    UIModel.INSTANCE.getListMailsMap().put(emailsTableView.getItems().size() + 1, emailModel.filePath());
                })));

        mailSaver.getEmailPublisher().subscribe(new ConsumerSubscriber<>(emailModel ->
                Platform.runLater(() -> lastMessageTextArea.setText(emailModel.emailStr()))));

        Platform.runLater(() -> startServerButton.requestFocus());

        syncModelWithArgs();
    }

    private void setupSmtpLogs() {
        SmtpLogPublisherAppender smtpLogPublisherAppender = findSmtpLogPublisherAppender();
        if (smtpLogPublisherAppender != null) {
            smtpLogPublisherAppender.getSmtpLogEventPublisher()
                    .subscribe(new ConsumerSubscriber<>(iLoggingEvent ->
                            Platform.runLater(() -> {
                                // inject header line if needed
                                if (iLoggingEvent.getFormattedMessage().startsWith("SMTP connection from")) {
                                    smtpLogTextArea.appendText("-".repeat(100) + "\n");
                                }
                                smtpLogTextArea.appendText(iLoggingEvent.getFormattedMessage() + "\n");
                            })));
        }
    }

    private SmtpLogPublisherAppender findSmtpLogPublisherAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (ch.qos.logback.classic.Logger logger : loggerContext.getLoggerList()) {
            Iterator<Appender<ILoggingEvent>> iter = logger.iteratorForAppenders();
            while (iter.hasNext()) {
                Appender<ILoggingEvent> appender = iter.next();
                if (appender instanceof SmtpLogPublisherAppender smtpLogPublisherAppender) {
                    return smtpLogPublisherAppender;
                }
            }
        }
        return null;
    }

    private void initEmailsTableView(TableView<EmailModel> emailsTableView) {
        TableColumn<EmailModel, LocalDateTime> dateColumn = new TableColumn<>(messages.getString("mailslist.col.received"));
        dateColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().receivedDate()));
        dateColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                if (empty || item == null) {
                    setText("");
                } else {
                    String formatted = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM).format(item);
                    setText(formatted);
                }
            }
        });
//        dateColumn.setMinWidth(75.0);
//        dateColumn.setMaxWidth(120.0);
//        dateColumn.setPrefWidth(80.0);
        emailsTableView.getColumns().add(dateColumn);

        TableColumn<EmailModel, String> fromColumn = new TableColumn<>(messages.getString("mailslist.col.from"));
        fromColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().from()));
        emailsTableView.getColumns().add(fromColumn);

        TableColumn<EmailModel, String> toColumn = new TableColumn<>(messages.getString("mailslist.col.to"));
        toColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().to()));
        emailsTableView.getColumns().add(toColumn);

        TableColumn<EmailModel, String> subjectColumn = new TableColumn<>(messages.getString("mailslist.col.subject"));
        subjectColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().subject()));
        emailsTableView.getColumns().add(subjectColumn);

        // manage double-click on TableRow
        emailsTableView.setRowFactory(param -> {
            TableRow<EmailModel> emailModelTableRow = new TableRow<>();
            emailModelTableRow.setOnMouseClicked(this::onEmailsListClick);
            return emailModelTableRow;
        });
    }

    private void syncModelWithArgs() {
        ArgsHandler argsHandler = ArgsHandler.INSTANCE;
        UIModel uiModel = UIModel.INSTANCE;

        if (argsHandler.getPort().isPresent()) {
            uiModel.setPort(String.valueOf(argsHandler.getPort()));
            portNumberTextField.setText(String.valueOf(argsHandler.getPort()));
        } else {
            portNumberTextField.setText(Configuration.getInstance().get("smtp.default.port"));
            uiModel.setPort(Configuration.getInstance().get("smtp.default.port"));
        }

        if (argsHandler.isStartServerAtLaunch()) {
            startServerButton.fire();
        }
    }

    @FXML
    private void onStartServer(ActionEvent actionEvent) {
        AtomicBoolean hasErrors = new AtomicBoolean(false);
        AtomicReference<String> errorMessage = new AtomicReference<>();
        Runnable bgTask = () -> {
            Platform.runLater(() -> {
                // reset focus
                listeningPortLabel.requestFocus();
                serverStarting.setValue(true);
            });
            try {
                startServerIfNeeded(UIModel.INSTANCE);
            } catch (OutOfRangePortException e) {
                log.error(e.getMessage(), e);
                errorMessage.set(messages.getString("startsrv.err.range").formatted(e.getPort()));
                hasErrors.set(true);
            } catch (InvalidHostException e) {
                log.error(e.getMessage(), e);
                errorMessage.set(messages.getString("startsrv.err.invalidHost").formatted(e.getHost()));
                hasErrors.set(true);
            } catch (InvalidPortException e) {
                log.error(e.getMessage(), e);
                errorMessage.set(messages.getString("startsrv.err.invalidPort").formatted());
                hasErrors.set(true);
            } catch (BindPortException e) {
                log.error(e.getMessage(), e);
                errorMessage.set(messages.getString("startsrv.err.bound").formatted(e.getPort()));
                hasErrors.set(true);
            }
            Platform.runLater(() -> {
                serverStarting.setValue(false);
                if (!hasErrors.get()) {
                    serverStarted.setValue(true);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText(errorMessage.get());
                    alert.show();
                }
            });
        };
        new Thread(bgTask).start();
    }

    @FXML
    private void onChooseEmailsDirectory(ActionEvent actionEvent) {
        Window parentWindow = ((Node) actionEvent.getTarget()).getScene().getWindow();
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        if (Files.exists(Path.of(emailsDirectory.getValue()))) {
            directoryChooser.initialDirectoryProperty().set(new File(emailsDirectory.getValue()));
        } else {
            directoryChooser.initialDirectoryProperty().set(new File("."));
        }
        final File selectedDirectory = directoryChooser.showDialog(parentWindow);
        if (selectedDirectory != null) {
            String absolutePath = selectedDirectory.getAbsolutePath();
            emailsDirectory.set(absolutePath);
        }
    }

    @FXML
    private void onClearAll(ActionEvent actionEvent) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(messages.getString("clearall.title").formatted(Configuration.getInstance().get("application.name")));
        confirmation.setContentText(messages.getString("clearall.delete.email"));
        confirmation.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    synchronized (SMTPServerHandler.INSTANCE.getMailSaver().getLock()) {
                        SMTPServerHandler.INSTANCE.getMailSaver().deleteEmails();
                        emailsTableView.getItems().clear();
                    }
                });
    }

    @FXML
    private void onEmailsListContextMenu(ContextMenuEvent contextMenuEvent) {
        EmailModel selectedItem = emailsTableView.getSelectionModel().getSelectedItem();
        log.debug("Clicked item #{} @ {}", emailsTableView.getSelectionModel().getSelectedIndex(), selectedItem.filePath());
    }

    @FXML
    private void onEmailsListKeyReleased(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) {
            keyEvent.consume();
            int selectedIndex = emailsTableView.getSelectionModel().getSelectedIndex();
            EmailModel selectedItem = emailsTableView.getSelectionModel().getSelectedItem();
            log.debug("DELETION REQUESTED (item #{} with path '{}')", selectedIndex, selectedItem.filePath());
        }
    }

    @FXML
    private void onEmailsListClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
            mouseEvent.consume();
            EmailModel selectedItem = emailsTableView.getSelectionModel().getSelectedItem();
            if (!memoryModeEnabled.get()) {
                FakeSmtpApplication.hostServices.showDocument(Paths.get(selectedItem.filePath()).normalize().toString());
            }
        }
    }

    @FXML
    private void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    /**
     * @throws InvalidPortException    when the port is invalid.
     * @throws BindPortException       when the port cannot be bound.
     * @throws OutOfRangePortException when the port is out of range.
     * @throws InvalidHostException    when the address cannot be resolved.
     */
    private void startServerIfNeeded(UIModel uiModel) throws OutOfRangePortException, BindPortException, InvalidPortException, InvalidHostException {
        if (uiModel.isServerStarted()) {
            // Do nothing. We can't stop the server. User has to quit the app (issue with SubethaSMTP)
        } else {
            try {
                int port = Integer.parseInt(uiModel.getPort());
                InetAddress host = null;
                if (uiModel.getHost() != null && !uiModel.getHost().isEmpty()) {
                    host = InetAddress.getByName(uiModel.getHost());
                }

                SMTPServerHandler.INSTANCE.startServer(port, host);
            } catch (NumberFormatException e) {
                throw new InvalidPortException(e);
            } catch (UnknownHostException e) {
                throw new InvalidHostException(e, uiModel.getHost());
            }
        }
        uiModel.setServerStarted(!uiModel.isServerStarted());
    }

}
