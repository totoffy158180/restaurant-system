package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.storage.FileStorage;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {

    private static final Set<String> ALLOWED_EXTENSIONS =
            new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));

    @Autowired
    private FileStorage fileStorage;


    @PostMapping("/upload")
    @ApiOperation("documents upload")
    public Result<String> upload(MultipartFile file) {
        log.info("upload pic: {}", file.getOriginalFilename());
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return Result.error(MessageConstant.UPLOAD_TYPE_NOT_ALLOWED);
        }
        try {
            String objectName = UUID.randomUUID().toString() + extension;
            String filePath = fileStorage.upload(file.getBytes(), objectName, file.getContentType());
            return Result.success(filePath);
        } catch (Exception e) {
            log.error("file upload fail", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

    private String extensionOf(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") < 0) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase(Locale.ROOT);
    }
}
