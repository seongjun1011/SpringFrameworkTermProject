package com.kopo.portal.service;

import com.kopo.portal.model.UploadedFile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 첨부파일을 디스크(uploads 폴더)에 저장하고, DB에 기록할 파일 정보를 돌려준다.
 */
@Service
public class FileStorageService {

    public List<UploadedFile> store(String uploadDir, MultipartFile[] files) throws IOException {
        List<UploadedFile> result = new ArrayList<>();
        if (files == null) return result;
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) {
                String ext = StringUtils.getFilenameExtension(f.getOriginalFilename());
                String stored = UUID.randomUUID() + (ext != null ? "." + ext : "");
                f.transferTo(dir.resolve(stored).toAbsolutePath());
                result.add(new UploadedFile(f.getOriginalFilename(), stored));
            }
        }
        return result;
    }
}
