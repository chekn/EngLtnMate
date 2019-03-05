package com.chekn.engltnmate;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by CHEKN on 2018-12-19.
 */

public class AndPkgUtils {

    public static String loadAssetAsString(String name) {
        Context mScene = APPAplication.getContext();

        try {
            InputStream in = mScene.getAssets().open(name);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try {
                byte[] temp = new byte[1024];
                while (true) {
                    int len = in.read(temp, 0, temp.length);
                    if (len < 0) break;
                    buf.write(temp, 0, len);
                }
                return buf.toString("UTF-8");
            } finally {
                in.close();
            }
        } catch (Exception e) {
            LogUtils.e("fail to load asset: " + name, e);
            return "";
        }
    }
}
