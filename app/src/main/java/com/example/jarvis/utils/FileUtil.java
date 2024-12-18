package com.example.jarvis.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    /**
     * 读取文件内容并返回为字符串。
     *
     * @param filePath 文件路径
     * @return 文件内容字符串，如果文件不存在或读取失败则返回null
     */
    public static String readFile(String filePath) {
        Path path = Paths.get(filePath);

        try {
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
