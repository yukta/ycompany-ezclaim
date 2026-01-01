package com.ycompany.claimservice.controller;

import com.ycompany.claimservice.dto.PresignRequest;
import com.ycompany.claimservice.dto.PresignResponse;
import com.ycompany.claimservice.security.Authorization;
import com.ycompany.claimservice.security.Role;
import com.ycompany.claimservice.service.DocumentService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/claims/{claimId}/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // 1️⃣ Presign URL generation
    @PostMapping("/presign")
    public PresignResponse presign(@PathVariable String claimId,
                                   @RequestBody PresignRequest request) {

        Authorization.require(
                Role.CUSTOMER,
                Role.ADMINISTRATOR
        );

        return documentService.presignUpload(
                claimId,
                request.getFileName(),
                request.getContentType()
        );
    }

    @PostMapping("/confirm")
    public Map<String, String> confirmUpload(
            @PathVariable String claimId,
            @RequestBody Map<String, String> payload) {

        Authorization.require(
                Role.CUSTOMER,
                Role.ADMINISTRATOR
        );

        return documentService.confirmUpload(claimId, payload);
    }
}
