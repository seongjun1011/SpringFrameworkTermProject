package com.kopo.portal.model;

import jakarta.persistence.Embeddable;

/**
 * 첨부파일 정보(원본 파일명 / 서버 저장 파일명). 파일 자체는 uploads 폴더에 저장되고
 * DB에는 이름만 저장된다.
 */
@Embeddable
public class UploadedFile {

    private String originalName;
    private String storedName;

    protected UploadedFile() {
    }

    public UploadedFile(String originalName, String storedName) {
        this.originalName = originalName;
        this.storedName = storedName;
    }

    public String getOriginalName() { return originalName; }
    public String getStoredName() { return storedName; }
}
