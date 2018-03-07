package com.luciferche.lukmeup;

import java.text.SimpleDateFormat;

/**
 * Created by luciferche on 2/26/18.
 */

public class Constants {
    public interface NUMBER_FORMATS {
        String DISTANCE = "#,###.0##";
    }

    public interface ACTION {
        public static String MAIN_ACTION = "com.luciferche.lukmeup.action.main";
        public static String PREV_ACTION = "com.luciferche.lukmeup.action.prev";
        public static String PLAY_ACTION = "com.luciferche.lukmeup.action.play";
        public static String NEXT_ACTION = "com.luciferche.lukmeup.action.next";
        public static String STARTFOREGROUND_ACTION = "com.luciferche.lukmeup.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.luciferche.lukmeup.action.stopforeground";


    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
        int NOTIFICATION_FROM_INTENT = 42;
    }

    public interface DATE_FORMATS {

        SimpleDateFormat LOCAL_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat LOCAL_DATE_FORMAT= new SimpleDateFormat("dd.MM.yyyy");
    }

    public interface ICON_ANCHOR {
        float ANCHOR_HEIGHT = 1f;
        float ANCHOR_WIDTH = 0.5f;
    }
}
