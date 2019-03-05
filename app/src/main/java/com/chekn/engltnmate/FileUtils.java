package com.chekn.engltnmate;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * Created by CHEKN on 2018-12-16.
 */

public class FileUtils {

    /**
     * 删除整个文件夹 或者文件
     *
     * @param file
     */
    public void deleteDir(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
            }

            for (int index = 0; index < childFiles.length; index++) {
                deleteDir(childFiles[index]);
            }
        }
        file.delete();
    }

    /**
     * 给定根目录，返回一个相对路径所对应的实际文件名.
     *
     * @param baseDir     指定根目录
     * @param relFileName 相对路径名，来自于ZipEntry中的name
     * @return java.io.File 实际的文件
     */
    public File getRealFileName(String baseDir, String relFileName) {
        String[] dirs = relFileName.split("/");
        File ret = new File(baseDir);
        String substr = null;
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                substr = dirs[i];
                try {
                    substr = new String(substr.getBytes("8859_1"), "GB2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                ret = new File(ret, substr);
            }
            if (!ret.exists()) {
                ret.mkdirs();
            }
            substr = dirs[dirs.length - 1];
            try {
                substr = new String(substr.getBytes("8859_1"), "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            ret = new File(ret, substr);
            return ret;
        }
        return ret;
    }
}
