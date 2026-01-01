package com.ycompany.claimservice.dto;

public class PresignResponse {
    private String uploadUrl;
    private String objectKey;

    public PresignResponse(String uploadUrl, String objectKey) {
        this.uploadUrl = uploadUrl;
        this.objectKey = objectKey;
    }

    public String getUploadUrl() { return uploadUrl; }
    public String getObjectKey() { return objectKey; }
}