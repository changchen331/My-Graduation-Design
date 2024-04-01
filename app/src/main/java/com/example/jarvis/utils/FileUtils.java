package com.example.jarvis.utils;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * 将字符串写入到文本文件中
     * 该方法根据append参数的值决定是追加内容还是覆盖文件。
     * 如果append为true，则在文件末尾追加内容。
     * 如果append为false，则覆盖原有文件内容。
     *
     * @param content  要写入文件的字符串内容
     * @param filePath 文件所在的路径
     * @param fileName 要创建或写入的文件名
     * @param append   布尔值，指示是否追加内容
     */
    public static void writeTxtToFile(String content, String filePath, String fileName, Boolean append) {
        try {
            // 创建或打开文件
            File file = makeFile(filePath, fileName);
            if (!file.exists()) {
                // 如果文件不存在，则创建新文件
                boolean newFile = file.createNewFile();
                if (!newFile) {
                    Log.e(TAG, "无法创建文件: " + filePath + fileName);
                    return;
                }
            }

            // 每次写入时，都换行写
            String completedContent = content + "\r\n";

            // 根据append参数决定文件写入模式
            RandomAccessFile mRandomAccessFile;
            if (append) {
                // 追加模式
                mRandomAccessFile = new RandomAccessFile(file, "rwd");
                // 移动到文件末尾
                mRandomAccessFile.seek(file.length());
            } else {
                // 覆盖模式，首先删除原有文件然后创建新文件
                if (file.delete()) {
                    mRandomAccessFile = new RandomAccessFile(file, "rwd");
                } else {
                    Log.e(TAG, "无法创建文件: " + filePath + fileName);
                    return;
                }
            }

            // 写入内容
            mRandomAccessFile.write(completedContent.getBytes());
            mRandomAccessFile.close(); // 关闭文件
        } catch (IOException e) {
            Log.e(TAG, "写入错误: " + e);
        }
    }

    /**
     * 创建一个新的文件或检查文件是否存在
     * 该方法首先会调用makeDirectory方法来确保指定的文件路径存在。
     * 如果文件路径存在，它会检查指定的文件是否已经存在。
     * 如果文件不存在，它会尝试创建一个新文件。
     *
     * @param filePath 指定文件所在的路径
     * @param fileName 要创建的文件的名称，应包含扩展名，如 "example.txt"
     * @return 返回创建或已存在的文件的File对象
     */
    public static File makeFile(String filePath, String fileName) {
        // 初始化File对象
        File file = null;

        // 调用makeDirectory方法确保文件路径存在
        makeDirectory(filePath);
        try {
            // 创建一个新的File对象
            file = new File(filePath + fileName);
            // 检查文件是否存在，如果不存在则创建
            if (!file.exists()) {
                // 尝试创建新文件
                boolean newFile = file.createNewFile();
                if (!newFile) {
                    // 如果无法创建文件，记录错误日志
                    Log.e(TAG, "无法创建文件: " + fileName);
                }
            }
        } catch (IOException e) {
            // 如果发生I/O异常，记录错误日志
            Log.e(TAG, "生成文件错误: " + e);
        }
        // 返回文件的File对象
        return file;
    }

    /**
     * 创建文件夹
     * 该方法用于创建指定路径的文件夹。
     * 如果文件夹已经存在，则不会重复创建。
     * 如果创建过程中发生任何异常，将会记录错误日志。
     *
     * @param filePath 要创建的文件夹的路径
     */
    public static void makeDirectory(String filePath) {
        // 创建一个File对象，用于表示指定的文件夹路径
        File directory = new File(filePath);

        // 检查文件夹是否存在
        if (!directory.exists()) {
            try {
                // 如果文件夹不存在，尝试创建它
                boolean isCreated = directory.mkdir();
                // 判断是否创建成功
                if (!isCreated) {
                    // 如果无法创建文件夹，记录错误日志
                    Log.e(TAG, "无法创建文件夹: " + filePath);
                }
            } catch (Exception e) {
                // 如果在尝试创建文件夹时发生异常，记录错误日志
                Log.e(TAG, "生成文件夹错误: " + e);
            }
        }
        // 如果文件夹已存在，则不需要执行任何操作
    }
}
