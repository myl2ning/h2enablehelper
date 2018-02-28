package ray.devkit.h2enablehelper;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ray on 2018/2/27.
 */

public class H2EnableHelper {
    private final static String TAG = H2EnableHelper.class.getSimpleName();
    private final static String OKHTTP_HTTPS_HANDLER_FULL_NAME = "com.android.okhttp.HttpsHandler";
    private final static String OKHTTP_PROTOCOL_FULL_NAME = "com.android.okhttp.Protocol";

    public static boolean tryEnableH2() {
        if (canEnable()) {
            try {
                Class cls = Class.forName(OKHTTP_HTTPS_HANDLER_FULL_NAME);
                Field supportProtocols = cls.getDeclaredField(getAndroidSupportProtocolsFieldName());
                List protocols = getH2IncludeProtocols();
                if (protocols.size() > 1) {
                    supportProtocols.setAccessible(true);
                    supportProtocols.set(null, protocols);
                    supportProtocols.setAccessible(false);
                }

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Enable fail:" + e);
            }
        }

        return false;
    }

    private static List getH2IncludeProtocols() {
        String[] protocolNames = getSupportProtocolNames();
        List list = new ArrayList();
        try {
            Class cls = Class.forName(OKHTTP_PROTOCOL_FULL_NAME);
            if (cls.isEnum()) {
                Field[] fields = cls.getDeclaredFields();
                for (Field field : fields) {
                    if (inArray(field.getName(), protocolNames)) {
                        field.setAccessible(true);
                        list.add(field.get(null));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Get protocols fail:" + e);
        }

        return list;
    }

    private static <T> boolean inArray(T search, T[] array) {
        if (array == null || search == null) {
            return false;
        }

        for (T suspect : array) {
            if (search.equals(suspect)) {
                return true;
            }
        }

        return false;
    }

    private static String[] getSupportProtocolNames(){
        switch (Build.VERSION.SDK_INT) {
            case Build.VERSION_CODES.LOLLIPOP:
            case Build.VERSION_CODES.LOLLIPOP_MR1:
                return new String[]{"HTTP_11", "HTTP_2", "SPDY_3"};
        }
        return new String[]{"HTTP_1_1", "HTTP_2", "SPDY_3"};
    }

    private static String getAndroidSupportProtocolsFieldName() {
        switch (Build.VERSION.SDK_INT) {
            case Build.VERSION_CODES.LOLLIPOP:
            case Build.VERSION_CODES.LOLLIPOP_MR1:
                return "ENABLED_PROTOCOLS";
        }
        return "HTTP_1_1_ONLY";
    }

    private static boolean canEnable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
