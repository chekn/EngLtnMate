package com.chekn.engltnmate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by CHEKN on 2018-12-15.
 */

public class IOUtils {

        public static byte[] readBytes(File file) throws Exception {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len = -1;
            while((len = fis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            fis.close();
            return bos.toByteArray();
        }

}
