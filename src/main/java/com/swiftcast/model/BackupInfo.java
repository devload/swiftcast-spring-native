package com.swiftcast.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupInfo {
    private String filename;
    private Long timestamp;
    private Long size;
}
