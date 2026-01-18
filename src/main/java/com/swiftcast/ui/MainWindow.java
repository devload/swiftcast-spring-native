package com.swiftcast.ui;

import com.swiftcast.model.Account;
import com.swiftcast.model.BackupInfo;
import com.swiftcast.proxy.ProxyServer;
import com.swiftcast.service.AccountService;
import com.swiftcast.service.BackupService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MainWindow {

    private final AccountService accountService;
    private final BackupService backupService;
    private final ProxyServer proxyServer;

    private Label statusLabel;
    private Button toggleProxyButton;
    private ListView<Account> accountListView;
    private ListView<BackupInfo> backupListView;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public void start(Stage primaryStage) {
        primaryStage.setTitle("SwiftCast - Claude/GLM Proxy");

        // 메인 레이아웃
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // 프록시 제어 섹션
        TitledPane proxyPane = createProxyControlPane();

        // 계정 관리 섹션
        TitledPane accountPane = createAccountManagementPane();

        // 백업 관리 섹션
        TitledPane backupPane = createBackupManagementPane();

        root.getChildren().addAll(proxyPane, accountPane, backupPane);

        Scene scene = new Scene(root, 700, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // 초기 로드
        loadAccounts();
        loadBackups();
        updateProxyStatus();
    }

    private TitledPane createProxyControlPane() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        statusLabel = new Label("프록시 상태: 중지됨");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        toggleProxyButton = new Button("프록시 시작");
        toggleProxyButton.setOnAction(e -> toggleProxy());

        Label portLabel = new Label("포트: 8080");

        content.getChildren().addAll(statusLabel, toggleProxyButton, portLabel);

        TitledPane pane = new TitledPane("프록시 제어", content);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createAccountManagementPane() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // 계정 목록
        accountListView = new ListView<>();
        accountListView.setPrefHeight(150);
        accountListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Account account, boolean empty) {
                super.updateItem(account, empty);
                if (empty || account == null) {
                    setText(null);
                } else {
                    String activeMarker = account.getIsActive() ? "[활성] " : "";
                    setText(activeMarker + account.getName() + " - " + account.getBaseUrl());
                }
            }
        });

        // 버튼들
        HBox buttons = new HBox(5);
        Button addButton = new Button("계정 추가");
        Button switchButton = new Button("활성화");
        Button deleteButton = new Button("삭제");

        addButton.setOnAction(e -> showAddAccountDialog());
        switchButton.setOnAction(e -> switchSelectedAccount());
        deleteButton.setOnAction(e -> deleteSelectedAccount());

        buttons.getChildren().addAll(addButton, switchButton, deleteButton);

        content.getChildren().addAll(new Label("계정 목록:"), accountListView, buttons);

        TitledPane pane = new TitledPane("계정 관리", content);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createBackupManagementPane() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // 백업 목록
        backupListView = new ListView<>();
        backupListView.setPrefHeight(200);
        backupListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(BackupInfo backup, boolean empty) {
                super.updateItem(backup, empty);
                if (empty || backup == null) {
                    setText(null);
                } else {
                    String date = DATE_FORMATTER.format(Instant.ofEpochSecond(backup.getTimestamp()));
                    String size = formatFileSize(backup.getSize());
                    setText(date + " (" + size + ")");
                }
            }
        });

        // 버튼들
        HBox buttons = new HBox(5);
        Button backupButton = new Button("백업 생성");
        Button restoreButton = new Button("복원");
        Button deleteButton = new Button("삭제");

        backupButton.setOnAction(e -> createBackup());
        restoreButton.setOnAction(e -> restoreSelectedBackup());
        deleteButton.setOnAction(e -> deleteSelectedBackup());

        buttons.getChildren().addAll(backupButton, restoreButton, deleteButton);

        content.getChildren().addAll(new Label("Claude 설정 백업:"), backupListView, buttons);

        TitledPane pane = new TitledPane("백업 관리", content);
        pane.setCollapsible(false);
        return pane;
    }

    private void toggleProxy() {
        try {
            if (proxyServer.isRunning()) {
                proxyServer.stop();
            } else {
                proxyServer.start(8080);
            }
            updateProxyStatus();
        } catch (Exception e) {
            showError("프록시 오류", e.getMessage());
        }
    }

    private void updateProxyStatus() {
        boolean running = proxyServer.isRunning();
        statusLabel.setText("프록시 상태: " + (running ? "실행 중" : "중지됨"));
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " +
                (running ? "green" : "gray"));
        toggleProxyButton.setText(running ? "프록시 중지" : "프록시 시작");
    }

    private void showAddAccountDialog() {
        Dialog<Account> dialog = new Dialog<>();
        dialog.setTitle("계정 추가");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField urlField = new TextField("https://api.anthropic.com");
        PasswordField apiKeyField = new PasswordField();

        grid.add(new Label("이름:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Base URL:"), 0, 1);
        grid.add(urlField, 1, 1);
        grid.add(new Label("API Key:"), 0, 2);
        grid.add(apiKeyField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return accountService.createAccount(
                        nameField.getText(),
                        urlField.getText(),
                        apiKeyField.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(account -> {
            loadAccounts();
            showInfo("성공", "계정이 추가되었습니다.");
        });
    }

    private void switchSelectedAccount() {
        Account selected = accountListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            accountService.switchAccount(selected.getId());
            loadAccounts();
            showInfo("성공", "계정이 활성화되었습니다.");
        }
    }

    private void deleteSelectedAccount() {
        Account selected = accountListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "계정 '" + selected.getName() + "'을(를) 삭제하시겠습니까?",
                    ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    accountService.deleteAccount(selected.getId());
                    loadAccounts();
                }
            });
        }
    }

    private void createBackup() {
        try {
            BackupInfo backup = backupService.backupClaudeSettings();
            loadBackups();
            showInfo("성공", "백업이 생성되었습니다.");
        } catch (Exception e) {
            showError("백업 실패", e.getMessage());
        }
    }

    private void restoreSelectedBackup() {
        BackupInfo selected = backupListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "백업을 복원하시겠습니까?\nClaude Code를 재시작해야 합니다.",
                    ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        backupService.restoreClaudeSettings(selected.getFilename());
                        showInfo("성공", "설정이 복원되었습니다. Claude Code를 재시작하세요.");
                    } catch (Exception e) {
                        showError("복원 실패", e.getMessage());
                    }
                }
            });
        }
    }

    private void deleteSelectedBackup() {
        BackupInfo selected = backupListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                backupService.deleteBackup(selected.getFilename());
                loadBackups();
            } catch (Exception e) {
                showError("삭제 실패", e.getMessage());
            }
        }
    }

    private void loadAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        Platform.runLater(() -> {
            accountListView.getItems().clear();
            accountListView.getItems().addAll(accounts);
        });
    }

    private void loadBackups() {
        try {
            List<BackupInfo> backups = backupService.listBackups();
            Platform.runLater(() -> {
                backupListView.getItems().clear();
                backupListView.getItems().addAll(backups);
            });
        } catch (Exception e) {
            log.error("Failed to load backups", e);
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
