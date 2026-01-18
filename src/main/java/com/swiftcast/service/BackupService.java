package com.swiftcast.service;

import com.swiftcast.model.BackupInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class BackupService {

    private static final String BACKUP_DIR_NAME = "swiftcast-backups";
    private static final Pattern BACKUP_FILENAME_PATTERN = Pattern.compile("settings_backup_(\\d+)\\.json");

    private Path getClaudeSettingsPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String appData;

        if (os.contains("win")) {
            appData = System.getenv("APPDATA");
        } else if (os.contains("mac")) {
            appData = System.getProperty("user.home") + "/Library/Application Support";
        } else {
            appData = System.getProperty("user.home") + "/.config";
        }

        return Paths.get(appData, "Claude", "settings.json");
    }

    private Path getBackupDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String appData;

        if (os.contains("win")) {
            appData = System.getenv("APPDATA");
        } else {
            appData = System.getProperty("user.home") + "/.config";
        }

        return Paths.get(appData, BACKUP_DIR_NAME);
    }

    public BackupInfo backupClaudeSettings() throws IOException {
        Path settingsPath = getClaudeSettingsPath();

        if (!Files.exists(settingsPath)) {
            throw new IOException("Claude settings.json not found at: " + settingsPath);
        }

        Path backupDir = getBackupDir();
        Files.createDirectories(backupDir);

        long timestamp = Instant.now().getEpochSecond();
        String filename = "settings_backup_" + timestamp + ".json";
        Path backupPath = backupDir.resolve(filename);

        Files.copy(settingsPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

        long size = Files.size(backupPath);
        log.info("Backup created: {}", filename);

        return new BackupInfo(filename, timestamp, size);
    }

    public void restoreClaudeSettings(String backupFilename) throws IOException {
        Path backupDir = getBackupDir();
        Path backupPath = backupDir.resolve(backupFilename);

        if (!Files.exists(backupPath)) {
            throw new IOException("Backup file not found: " + backupFilename);
        }

        Path settingsPath = getClaudeSettingsPath();

        // Claude 설정 디렉토리가 없으면 생성
        Files.createDirectories(settingsPath.getParent());

        Files.copy(backupPath, settingsPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Settings restored from: {}", backupFilename);
    }

    public List<BackupInfo> listBackups() throws IOException {
        Path backupDir = getBackupDir();

        if (!Files.exists(backupDir)) {
            return new ArrayList<>();
        }

        List<BackupInfo> backups = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupDir, "*.json")) {
            for (Path path : stream) {
                String filename = path.getFileName().toString();
                Matcher matcher = BACKUP_FILENAME_PATTERN.matcher(filename);

                if (matcher.matches()) {
                    long timestamp = Long.parseLong(matcher.group(1));
                    long size = Files.size(path);
                    backups.add(new BackupInfo(filename, timestamp, size));
                }
            }
        }

        // 최신 순으로 정렬
        backups.sort(Comparator.comparingLong(BackupInfo::getTimestamp).reversed());

        return backups;
    }

    public void deleteBackup(String backupFilename) throws IOException {
        Path backupDir = getBackupDir();
        Path backupPath = backupDir.resolve(backupFilename);

        if (!Files.exists(backupPath)) {
            throw new IOException("Backup file not found: " + backupFilename);
        }

        Files.delete(backupPath);
        log.info("Backup deleted: {}", backupFilename);
    }
}
