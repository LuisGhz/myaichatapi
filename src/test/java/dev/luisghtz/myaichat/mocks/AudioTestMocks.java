package dev.luisghtz.myaichat.mocks;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

/**
 * Mock utilities for audio-related testing
 */
public class AudioTestMocks {

    /**
     * Creates a mock MultipartFile for audio testing
     */
    public static MultipartFile createMockAudioFile(String filename, byte[] content) {
        return new MockMultipartFile(
            "audio",
            filename,
            "audio/wav",
            content
        );
    }

    /**
     * Creates a mock MultipartFile with default content
     */
    public static MultipartFile createMockAudioFile() {
        byte[] audioContent = "mock audio content".getBytes();
        return createMockAudioFile("test-audio.wav", audioContent);
    }

    /**
     * Creates a mock MultipartFile with large content for testing
     */
    public static MultipartFile createLargeMockAudioFile() {
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        return createMockAudioFile("large-audio.wav", largeContent);
    }

    /**
     * Creates a mock MultipartFile that throws IOException when accessing bytes
     */
    public static MultipartFile createFaultyMockAudioFile() {
        return new MockMultipartFile("audio", "faulty.wav", "audio/wav", "content".getBytes()) {
            @Override
            public byte[] getBytes() throws IOException {
                throw new IOException("Mock IO Exception");
            }
        };
    }
}
