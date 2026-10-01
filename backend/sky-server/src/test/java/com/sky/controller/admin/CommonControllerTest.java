package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.handler.GlobalExceptionHandler;
import com.sky.result.Result;
import com.sky.storage.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommonControllerTest {

    private MockMvc mockMvc;
    private FileStorage fileStorage;

    @BeforeEach
    void setUp() {
        fileStorage = mock(FileStorage.class);
        CommonController controller = new CommonController();
        ReflectionTestUtils.setField(controller, "fileStorage", fileStorage);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static MockMultipartFile file(String name, String contentType) {
        return new MockMultipartFile("file", name, contentType, new byte[]{1, 2, 3});
    }

    @Test
    void uploadsAllowedImage() throws Exception {
        when(fileStorage.upload(any(), anyString(), eq("image/png"))).thenReturn("/uploads/x.png");

        mockMvc.perform(multipart("/admin/common/upload").file(file("photo.PNG", "image/png")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("/uploads/x.png"));

        verify(fileStorage).upload(any(), matches("[0-9a-f-]{36}\\.png"), eq("image/png"));
    }

    @Test
    void rejectsNonImageFile() throws Exception {
        mockMvc.perform(multipart("/admin/common/upload").file(file("evil.html", "text/html")))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(MessageConstant.UPLOAD_TYPE_NOT_ALLOWED));

        verifyNoInteractions(fileStorage);
    }

    @Test
    void rejectsFileWithoutExtension() throws Exception {
        mockMvc.perform(multipart("/admin/common/upload").file(file("photo", "image/png")))
                .andExpect(jsonPath("$.msg").value(MessageConstant.UPLOAD_TYPE_NOT_ALLOWED));

        verifyNoInteractions(fileStorage);
    }

    @Test
    void returnsUploadFailedWhenStorageThrows() throws Exception {
        when(fileStorage.upload(any(), anyString(), any())).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(multipart("/admin/common/upload").file(file("photo.jpg", "image/jpeg")))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(MessageConstant.UPLOAD_FAILED));
    }

    @Test
    void oversizedUploadReturnsErrorKey() {
        Result result = new GlobalExceptionHandler()
                .exceptionHandler(new MaxUploadSizeExceededException(2 * 1024 * 1024));

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getMsg()).isEqualTo(MessageConstant.UPLOAD_SIZE_EXCEEDED);
    }
}
