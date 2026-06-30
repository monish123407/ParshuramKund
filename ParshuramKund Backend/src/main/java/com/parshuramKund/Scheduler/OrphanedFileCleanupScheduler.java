package com.parshuramKund.Scheduler;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.parshuramKund.Entity.Applicant;
import com.parshuramKund.Repository.ApplicantRepository;

@Component
public class OrphanedFileCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrphanedFileCleanupScheduler.class);

    @Autowired
    private ApplicantRepository applicantRepository;

    // Run every hour, with an initial delay of 30 seconds
    @Scheduled(initialDelay = 30000, fixedDelay = 3600000)
    public void cleanupOrphanedFiles() {
        log.info("Starting scheduled cleanup of orphaned Aadhaar upload files...");
        try {
            Path uploadDir = Paths.get("aadhar-photos").toAbsolutePath().normalize();
            File folder = uploadDir.toFile();
            if (!folder.exists() || !folder.isDirectory()) {
                log.info("Upload directory does not exist yet. Skipping cleanup.");
                return;
            }

            File[] files = folder.listFiles();
            if (files == null || files.length == 0) {
                log.info("No files found in upload directory.");
                return;
            }

            // Retrieve all valid file paths registered in the database
            List<Applicant> applicants = applicantRepository.findAll();
            Set<String> activeFileNames = applicants.stream()
                    .map(Applicant::getAadharPhotoPath)
                    .filter(path -> path != null && !path.trim().isEmpty())
                    .collect(Collectors.toSet());

            log.info("Found {} active Aadhaar photo file name(s) in the database.", activeFileNames.size());

            int deletedCount = 0;
            long currentTime = System.currentTimeMillis();
            
            // Delete files that are older than 15 minutes (to avoid deleting active uploads in progress)
            // and are not registered in the database.
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (!activeFileNames.contains(fileName)) {
                        long fileAgeMs = currentTime - file.lastModified();
                        if (fileAgeMs > 15 * 60 * 1000) { // 15 minutes
                            if (file.delete()) {
                                log.info("Deleted orphaned Aadhaar file: {}", fileName);
                                deletedCount++;
                            } else {
                                log.warn("Failed to delete orphaned Aadhaar file: {}", fileName);
                            }
                        }
                    }
                }
            }
            log.info("Orphaned Aadhaar upload files cleanup completed. Deleted {} file(s).", deletedCount);
        } catch (Exception e) {
            log.error("Error occurred during orphaned files cleanup", e);
        }
    }
}
