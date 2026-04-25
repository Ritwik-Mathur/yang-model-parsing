package com.yangparser.controller;

import com.yangparser.model.YangModule;
import com.yangparser.service.YangParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/yang")
@CrossOrigin(origins = "*")
public class YangParserController {

    @Autowired
    private YangParserService yangParserService;

    /**
     * POST /api/yang/parse
     * Body: { "content": "<yang content>" }
     */
    @PostMapping("/parse")
    public ResponseEntity<?> parseYangContent(@RequestBody Map<String, String> body) {
        try {
            String content = body.get("content");
            YangModule module = yangParserService.parseContent(content);
            return ResponseEntity.ok(module);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Parse error: " + e.getMessage()));
        }
    }

    /**
     * POST /api/yang/upload
     * Multipart file upload of a .yang file
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadYangFile(@RequestParam("file") MultipartFile file) {
        try {
            YangModule module = yangParserService.parseFile(file);
            return ResponseEntity.ok(module);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to read file: " + e.getMessage()));
        }
    }

    /**
     * GET /api/yang/sample
     * Returns the built-in sample YANG content
     */
    @GetMapping("/sample")
    public ResponseEntity<?> getSample() {
        return ResponseEntity.ok(Map.of("content", yangParserService.getSampleYang()));
    }

    /**
     * GET /api/yang/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "YANG Model Parser"));
    }
}
