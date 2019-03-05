package com.chekn.engltnmate;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;


/**
 * Created by sensyang on 2018/1/5.
 * @author: 一个人的暗
 * @Emial:532245792@qq.com
 */

public class ZipUtil {

    public static ZipUtil instance;

    public static ZipUtil getInstance(){
        if (instance == null) {
            instance = new ZipUtil();
        }
        return instance;
    }

    /**
     * 解压
     * @param PATH    解压到的地址
     * @param zipName  zip文件
     */

    public boolean unZip(String PATH,String zipName) {
        boolean isOver = true;
        File file = new File(zipName);
        try {
            upZipFile(file, PATH);
            // upZipFile(zip文件,解压到的地址);
        } catch (IOException e) {
            e.printStackTrace();
            isOver = false;
        }
        return isOver;
    }

    //https://blog.csdn.net/weixin_40855673/article/details/79301122
    private final String nameEncode = "GBK";

    /**
     * 解压缩
     * 将zipFile文件解压到folderPath目录下.
     * @param zipFile zip文件
     * @param folderPath 解压到的地址
     * @throws IOException
     */
    public void upZipFile(File zipFile, String folderPath) throws IOException {
        OutputStream os = null;
        InputStream is = null;
        ZipFile zf = null;
        try {
            zf = new ZipFile(zipFile, nameEncode);
            Enumeration entryEnum = zf.getEntries();
            if (null != entryEnum) {
                ZipEntry zipEntry = null;
                while (entryEnum.hasMoreElements()) {
                    zipEntry = (ZipEntry) entryEnum.nextElement();
                    if (zipEntry.isDirectory()) {
                        //不处理文件夹
                        String directoryPath = folderPath + File.separator + zipEntry.getName();
                        LogUtils.i( "不处理文件夹" + directoryPath);
                        continue;
                    }
                    if (zipEntry.getSize() > 0) {

                        File targetFile = new File(folderPath+ File.separator + zipEntry.getName());
                        if (!targetFile.exists()) {
                            //如果不存在就创建
                            File fileParentDir = targetFile.getParentFile();
                            if (!fileParentDir.exists()) {
                                fileParentDir.mkdirs();
                            }
                            targetFile.createNewFile();
                        }
                        os = new BufferedOutputStream(new FileOutputStream(targetFile));
                        is = zf.getInputStream(zipEntry);
                        byte[] buffer = new byte[4096];
                        int readLen = 0;
                        while ((readLen = is.read(buffer, 0, 1024)) >= 0) {
                            os.write(buffer, 0, readLen);
                        }

                        os.flush();
                        os.close();
                    }
                }
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (null != is) {
                is.close();
            }
            if (null != os) {
                os.close();
            }
        }
    }

    /**
     * 给定根目录，返回一个相对路径所对应的实际文件，连带把相关的父目录全都创建了.
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
                    substr = new String(substr.getBytes("8859_1"), nameEncode);
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
                substr = new String(substr.getBytes("8859_1"), nameEncode);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ret = new File(ret, substr);
            return ret;
        }
        return ret;
    }

}