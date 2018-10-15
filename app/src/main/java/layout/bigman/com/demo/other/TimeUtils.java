package layout.bigman.com.demo.other;

import java.text.DecimalFormat;

/**
 * @author 曾凡达
 * @date 2018/5/24
 */
public class TimeUtils {
    /**
     * 时间格式化
     */
    public static String formattedTime(long second) {
        String hs, ms, ss, formatTime;

        long h, m, s;
        h = second / 3600;
        m = (second % 3600) / 60;
        s = (second % 3600) % 60;
        if (h < 10) {
            hs = "0" + h;
        } else {
            hs = "" + h;
        }

        if (m < 10) {
            ms = "0" + m;
        } else {
            ms = "" + m;
        }

        if (s < 10) {
            ss = "0" + s;
        } else {
            ss = "" + s;
        }
        formatTime = hs + ":" + ms + ":" + ss;

        return formatTime;
    }

    /**
     * 获取分钟
     */
    public static String getMinStr(long second) {
        if (second == 0) {
            return "0";
        }
        DecimalFormat df = new DecimalFormat("0");
        return df.format(second / 60);
    }

    /**
     * 获取小时
     */
    public static String getHourStr(long second) {
        if (second == 0) {
            return "0.0";
        }
        DecimalFormat df = new DecimalFormat("#.0");
        return df.format(Float.valueOf(String.valueOf(second)) / 3600);
    }

    /**
     * time的单位是秒
     *
     * @param durationString
     * @return
     */
    public static String formatMusicTime(String durationString) {
        try {
            final long duration = Long.parseLong(durationString) * 1000;
            return formatMusicTime(duration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "00:00";
    }

    /**
     * duration单位毫秒
     *
     * @param duration
     * @return
     */
    public static String formatMusicTime(long duration) {
        String time = "";
        long minute = duration / 60000;
        long seconds = duration % 60000;
        long second = Math.round((int) seconds / 1000);
        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";
        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;
    }
}
