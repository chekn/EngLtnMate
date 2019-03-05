package com.chekn.engltnmate;

import android.widget.Toast;

/**
 * Created by CHEKN on 2018-12-15.
 */

public class ToastUtil {

    private static Toast mToast;

    public static void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(APPAplication.getContext(), "", Toast.LENGTH_LONG);
        }
        mToast.setText(msg);
        mToast.show();
    }

}
