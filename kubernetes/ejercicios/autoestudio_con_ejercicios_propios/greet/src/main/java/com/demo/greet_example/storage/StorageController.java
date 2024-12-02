package com.demo.greet_example.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("storage")
public class StorageController {

    @Value("${file.save_path}")
    private String savePath;

    @PostMapping
    public File save(@RequestBody File file) throws IOException {
        final byte[] content = file.content().getBytes(StandardCharsets.UTF_8);
        final String filePath = getSavePath();
        Files.write(Path.of(filePath), content);
        return new File(filePath, file.content());
    }

    @GetMapping(path = "/{fileName}")
    public File get(@PathVariable(name = "fileName") final String fileName) throws IOException {
        final String filePath = getSavePath(fileName);
        String content = Files.readAllLines(Path.of(filePath)).stream().reduce("", String::concat);
        return new File(fileName, content);
    }

    @DeleteMapping(path = "/{fileName}")
    public File delete(@PathVariable(name = "fileName") final String fileName) throws IOException {
        File file = get(fileName);
        final String filePath = getSavePath(fileName);
        Files.delete(Path.of(filePath));
        return file;
    }

    private String getSavePath() {
        final String fileName = generateRandomString();
        return savePath.endsWith("/") ? savePath + fileName : savePath + "/" + fileName ;
    }

    private String getSavePath(String fileName) {
        return savePath.endsWith("/") ? savePath + fileName : savePath + "/" + fileName ;
    }

    private String generateRandomString() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
