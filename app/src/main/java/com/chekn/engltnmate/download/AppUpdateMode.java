package com.chekn.engltnmate.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.chekn.engltnmate.APPAplication;
import com.chekn.engltnmate.LogUtils;
import com.chekn.engltnmate.R;
import com.chekn.engltnmate.ToastUtil;
import com.chekn.engltnmate.ZipUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import java.io.File;

public class AppUpdateMode {

    private Context mContext = APPAplication.getContext();

    private NotificationManager notificationManager;
    private Notification notification; //下载通知进度提示
    private NotificationCompat.Builder builder;
    private boolean flag = false; //进度框消失标示 之后发送通知
    public static boolean isUpdate = false; //是否正在更新

    private AppUpdateMode() {

    }

    private static AppUpdateMode instance;
    public static AppUpdateMode getInstance() {
        if(instance == null) {
            return new AppUpdateMode();
        }
        return instance;
    }


    //初始化通知
    private void initNotification() {
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle("正在更新...") //设置通知标题
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher_round)) //设置通知的大图标
                .setDefaults(Notification.DEFAULT_LIGHTS) //设置通知的提醒方式： 呼吸灯
                .setPriority(NotificationCompat.PRIORITY_MAX) //设置通知的优先级：最大
                .setAutoCancel(false)//设置通知被点击一次是否自动取消
                .setContentText("下载进度:" + "0%")
                .setProgress(100, 0, false);
        notification = builder.build();//构建通知对象
    }

    public void download(String url, final String filePath) {
        final String destFileDir = filePath.substring(0, filePath.lastIndexOf("/"));
        String mDestFileName = filePath.replaceAll("^.*/","");
        LogUtils.i("下载位置: " + destFileDir + ", " + mDestFileName );
        OkGo.<File>get(url).execute(new FileCallback(destFileDir, mDestFileName) {

            @Override
            public void onSuccess(Response<File> response) {
                File file = response.body();
                LogUtils.i("onSuccess: 下载完成" + file.getPath() + file.getName());
                builder.setContentTitle("下载完成")
                        .setContentText("")
                        .setAutoCancel(true);//设置通知被点击一次是否自动取消
                notification = builder.build();
                notificationManager.notify(1, notification);

                AppUpdateMode.this.unZipFile(destFileDir, filePath);
/*
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri data;
                // 判断版本大于等于7.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // "com.yhx.loan.fileprovider"即是在清单文件中配置的authorities
                    data = FileProvider.getUriForFile(mContext, "com.yhx.loan.fileprovider", file);
                    // 给目标应用一个临时授权
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    data = Uri.fromFile(file);
                }
                intent.setDataAndType(data, "application/vnd.android.package-archive");
                PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);
                notification = builder.setContentIntent(pi).build();
                notificationManager.notify(1, notification);
*/
            }

            //下载进度
            @Override
            public void downloadProgress(Progress progress) {
                super.downloadProgress(progress);
                builder.setProgress(100, (int) (progress.fraction * 100), false);
                builder.setContentText("下载进度:" + (int) (progress.fraction * 100) + "%");
                notification = builder.build();
                notificationManager.notify(1, notification);
            }

            @Override
            public void onStart(Request<File, ? extends Request> request) {
                super.onStart(request);
                initNotification();
            }

            @Override
            public void onError(Response<File> response) {
                super.onError(response);
                Toast.makeText(mContext, "下载错误", Toast.LENGTH_SHORT).show();
            }

        });

    }

    //ref12: https://blog.csdn.net/wrg_20100512/article/details/49300171 (资料 Aysnctask Object[]->Void[]转换异常)
    public void unZipFile(final String destFileDir, final String filePath) {
        AsyncTask<Void, Integer, Boolean> at = new AsyncTask<Void, Integer, Boolean>() {

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                ToastUtil.showToast("解压完成");
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                ZipUtil.getInstance().unZip(destFileDir, filePath);
                LogUtils.i("解压完成: " + filePath );
                return null;
            }
            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }
        };
        at.execute();
    }


}
