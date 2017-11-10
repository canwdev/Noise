package com.canwdev.noise.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import com.canwdev.noise.service.BackgroundService;

import java.util.ArrayList;
import java.util.List;

public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll(Context context) {
        // 停止BackgroundService服务
        Intent intent = new Intent(context, BackgroundService.class);
        context.stopService(intent);

        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }


        // 再次确保关闭
        // Process.killProcess(Process.myPid());
    }
}
