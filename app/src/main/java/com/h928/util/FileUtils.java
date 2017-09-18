package com.h928.util;

import java.io.File;

/**
 * Created by xiechunping on 2017/6/29.
 */

public class FileUtils {
    public static void list(String path) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                System.out.println("dir is empty!");
                return;
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        System.out.println("file:" + file2.getAbsolutePath());
                        list(file2.getAbsolutePath());
                    } else {
                        System.out.println("dir:" + file2.getAbsolutePath());
                    }
                }
            }
        } else {
            System.out.println("file not exist!");
        }
    }
}
