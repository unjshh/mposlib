package com.cxycxx.mposcore.log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.annimon.stream.Optional;
import com.cxycxx.mposcore.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 日志器
 */
public class PosLogger {
    /**
     * 写日志，必须在某个地方调用过init方法
     *
     * @param log
     */
    public static void writeLog(LogModel log) {
        if (log == null || db == null) return;
        try {
            if (Util.isEmpty(log.getAllTime())) {
                Optional<Date> optional = Util.s2Date(log.getStartTime(), Util.DATETIMEFORMT);
                Date now = Util.calendarToDate(Calendar.getInstance());
                log.setAllTime((now.getTime() - optional.get().getTime()) + "");
            }
            ContentValues cv = new ContentValues();
            cv.put("Interface", log.getInterface());
            cv.put("StartTime", log.getStartTime());
            cv.put("AllTime", log.getAllTime());
            cv.put("PostData", log.getPostData());
            cv.put("Response", log.getResponse());
            cv.put("State", log.getState());
            cv.put("ServiceUrl", log.getServiceUrl());
            new Thread(() -> db.insert("PosLogs", null, cv)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询日志(未开启新线程)
     *
     * @param condition 条件
     * @return 数据
     */
    public static List<LogModel> queryLogs(LogModel condition) {
        if (db == null) return Collections.emptyList();
        if (condition == null) condition = new LogModel();
        List<LogModel> logs = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append("select * from PosLogs where 1=1");
        if (!Util.isEmpty(condition.getTag()))
            builder.append(" and Tag='" + condition.getTag() + "'");
        if (!Util.isEmpty(condition.getInterface()))
            builder.append(" and Interface='" + condition.getInterface() + "'");
        if (!Util.isEmpty(condition.getStartTime()))
            builder.append(" and StartTime>='" + condition.getStartTime() + "'");
        if (!Util.isEmpty(condition.getAllTime()))
            builder.append(" and StartTime<'" + condition.getAllTime() + "'");
        if (!Util.isEmpty(condition.getState()))
            builder.append(" and State='" + condition.getState() + "'");
        String pageSize=condition==null||Util.isEmpty(condition.getServiceUrl())?"30":condition.getServiceUrl();
        builder.append(" order by StartTime desc limit "+pageSize);
        Cursor cursor = db.rawQuery(builder.toString(), null);
        while (cursor.moveToNext()) {
            LogModel log = new LogModel();
            log.setTag(cursor.getString(cursor.getColumnIndex("Tag")));
            log.setInterface(cursor.getString(cursor.getColumnIndex("Interface")));
            log.setStartTime(cursor.getString(cursor.getColumnIndex("StartTime")));
            log.setPostData(cursor.getString(cursor.getColumnIndex("PostData")));
            log.setResponse(cursor.getString(cursor.getColumnIndex("Response")));
            log.setAllTime(cursor.getString(cursor.getColumnIndex("AllTime")));
            log.setState(cursor.getString(cursor.getColumnIndex("State")));
            log.setServiceUrl(cursor.getString(cursor.getColumnIndex("ServiceUrl")));

            logs.add(log);
        }
        return logs;
    }

    /**
     * 查询日志Tag(未开启新线程)
     * @return 日志Tags
     */
    public static List<String> queryTags(){
        if (db == null) return Collections.emptyList();
        List<String> tags = new ArrayList<>();
        Cursor cursor = db.rawQuery("select distinct Tag from PosLogs", null);
        while (cursor.moveToNext()) {
            tags.add(cursor.getString(cursor.getColumnIndex("Tag")));
        }
        return tags;
    }
    /**
     * 查询日志（开启新线程）
     *
     * @param condition 条件
     * @param reciever  接收者
     */
    /*public static void queryLogs(LogModel condition, OnFBCommuFinish reciever) {
        if (reciever == null) return;
        new Thread(() -> {
            JsonObject obj = new JsonObject();
            JsonArray array = Stream.of(queryLogs(condition)).map(p -> Util.fromJson(Util.GSON.toJson(p), JsonObject.class).get()).collect(JsonArray::new, JsonArray::add);
            obj.add("items", array);
            reciever.onFBCommuFinish(obj, "查询日志");
        }).start();
    }*/

    /**
     * 初始化记日志用的数据库
     *
     * @param context
     */
    public static void init(Context context) {
        if (context == null || db != null) return;
        File path = new File(context.getFilesDir().getParent(), "databases");
        File f = new File(path, "log.db");
        if (!path.exists()) {
            if (!path.mkdirs()) {
                Util.showMsg(context, "不能创建日志目录");
                return;
            }
        }
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (Exception e) {
                Util.showMsg(context, "不能创建日志文件" + f);
            }
        }
        if (f.exists()) {
            db = context.openOrCreateDatabase(f.toString(), Context.MODE_PRIVATE, null);
            if (isTableExist("PosLogs")) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -30);//删除30天以前的日志
                db.delete("PosLogs", "StartTime<?", new String[]{Util.calendar2S(calendar)});
            } else {
                db.execSQL("CREATE Table PosLogs (Id integer primary key autoincrement,Tag text,Interface text,StartTime text, " +
                        "AllTime text,PostData text,Response text,State text,ServiceUrl text)");
            }
        }
    }

    /**
     * 判断某张表是否存在
     *
     * @param tabName 表名
     * @return
     */
    private static boolean isTableExist(String tabName) {
        if (Util.isEmpty(tabName) || db == null) return false;
        try {
            String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tabName.trim() + "'";
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 创建日志实例
     *
     * @param tag 标记
     * @return
     */
    public static LogModel getLogModel(String tag) {
        LogModel model = new LogModel();
        model.setTag(tag);
        model.setStartTime(Util.calendar2S(Calendar.getInstance()));
        model.setState("未知");
        return model;
    }

    private static SQLiteDatabase db;
}
