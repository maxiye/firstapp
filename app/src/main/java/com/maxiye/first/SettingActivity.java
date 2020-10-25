package com.maxiye.first;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.maxiye.first.part.PageListPopupWindow;
import com.maxiye.first.util.ApiUtil;
import com.maxiye.first.util.Util;

import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;

/**
 * @author due
 */
public class SettingActivity extends AppCompatActivity {
    // 设备管理应用请求
    private static final int INTENT_ADMIN_DEVICE_REQCODE = 100;
    private SharedPreferences sp;
    public static final String SETTING = "com.maxiye.first.SETTING";
    /**
     * 是否显示所有应用
     * bool
     */
    public static final String SHOW_SYSTEM = "show_system_apps";
    /**
     * 上次备份时间
     * long
     */
    public static final String BACKUP_TIME = "backup_time";
    /**
     * 书签json数据
     * string
     */
    public static final String BOOKMARK = "bookmark";
    /**
     * web-dav user
     * string
     */
    public static final String WEBDAV_USER = "webdav_user";
    /**
     * web-dav pwd
     * string
     */
    public static final String WEBDAV_PWD = "webdav_pwd";
    /**
     * 是否开启web-dav备份
     * bool
     */
    public static final String WEBDAV_ON_OFF = "webdav_on_off";
    /**
     * 查重程度控制，越小越严格，默认5
     * int
     */
    public static final String DUPLICATE_LEVEL = "duplicate_level";
    /**
     * 代理ip
     * string
     */
    public static final String PROXY_HOST = "proxy_host";
    /**
     * 代理端口
     * string
     */
    public static final String PROXY_PORT = "proxy_port";
    /**
     * 是否开启代理
     * bool
     */
    @SuppressWarnings("unused")
    public static final String PROXY_ON_OFF = "proxy_on_off";

    /**
     * web-dav user
     * string
     */
    public static final String API_USER = "api_user";
    /**
     * web-dav pwd
     * string
     */
    public static final String API_PWD = "api_pwd";
    /**
     * api780的cookie修改时间
     */
    public static final String API_COOKIE_MTIME = "api_cookie_mtime";
    /**
     * api780的cookie
     */
    public static final String API_COOKIE = "api_cookie";
    /**
     * api780的appKey
     */
    public static final String API_APP_KEY = "api_app_key";
    /**
     * api780的sign
     */
    public static final String API_SIGN = "api_sign";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sp = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        //初始化设置
        initSetting();
        enableDeviceAdmin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            alert("资源请求拒绝");
            return;
        }
        switch (requestCode) {
            case INTENT_ADMIN_DEVICE_REQCODE: {
                alert("请求已通过");
            }
            case 0: {
                break;
            }
            default:
                break;
        }
    }

    private void alert(String tip) {
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
    }

    /**
     * {@code 第42条：lambda表达式优于匿名类}
     */
    private void initSetting() {
        SwitchMaterial showSystemApps = findViewById(R.id.setting_show_system);
        SwitchMaterial webdavOnOff = findViewById(R.id.webdav_on_off);
        TextView webdavUser = findViewById(R.id.webdav_user_view);
        TextView webdavPwd = findViewById(R.id.webdav_pwd_view);
        TextView apiUser = findViewById(R.id.api_user_view);
        TextView apiPwd = findViewById(R.id.api_pwd_view);
        TextView duplicateLevel = findViewById(R.id.duplicate_level);
        showSystemApps.setChecked(sp.getBoolean(SHOW_SYSTEM, false));
        webdavOnOff.setChecked(sp.getBoolean(WEBDAV_ON_OFF, false));
        webdavUser.setText(sp.getString(WEBDAV_USER, getText(R.string.not_set).toString()));
        webdavPwd.setText(sp.getString(WEBDAV_PWD, getText(R.string.not_set).toString()));
        apiUser.setText(sp.getString(API_USER, getText(R.string.not_set).toString()));
        apiPwd.setText(sp.getString(API_PWD, getText(R.string.not_set).toString()));
        duplicateLevel.setText(String.valueOf(sp.getInt(DUPLICATE_LEVEL, 5)));
        showSystemApps.setOnCheckedChangeListener((buttonView, isChecked) -> sp.edit()
                .putBoolean(SHOW_SYSTEM, isChecked)
                .apply());
        webdavOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> sp.edit()
                .putBoolean(WEBDAV_ON_OFF, isChecked)
                .apply());
        findViewById(R.id.webdav_user_cfg).setOnClickListener(view -> editWebdavUser());
        findViewById(R.id.webdav_pwd_cfg).setOnClickListener(view -> editWebdavPwd());
        findViewById(R.id.api_user_cfg).setOnClickListener(view -> editApiUser());
        findViewById(R.id.api_pwd_cfg).setOnClickListener(view -> editApiPwd());
        findViewById(R.id.duplicate_level_cfg).setOnClickListener(view -> editDuplicateLevel());
        findViewById(R.id.api_list_cfg).setOnClickListener(view -> openApiList());
    }

    private void openApiList() {
        new ApiManager().show();
    }

    class ApiManager {
        private int count;
        private List<Map<String, Object>> list;

        void show() {
            Util.loading(SettingActivity.this);
            Util.getDefaultSingleThreadExecutor().execute(() -> {
                ApiUtil apiUtil = ApiUtil.getInstance();
                list = apiUtil.apiList();
                if (list == null || list.isEmpty()) {
                    runOnUiThread(() -> {
                        Util.loaded();
                        alert(getString(R.string.no_data));
                    });
                    return;
                }
                Drawable icon = ContextCompat.getDrawable(SettingActivity.this, R.drawable.ic_cloud_queue_black_24dp);
                for (Map<String, Object> item : list) {
                    String name = item.get("title").toString();
                    String status = item.get("status").toString();
                    String type = item.get("type").toString();
                    String limit = item.get("limit").toString();
                    String start = item.get("start").toString();
                    String end = item.get("end").toString();
                    item.put("name", "<span style='color: #00796b'>" + name + "</span><br/><span style='color: #969696; font-size: 10px'>" + type + " _ " + status + " _ " + limit + " _ "+ start + " - "+ end + "</span>");
                    item.put("icon", icon);
                }
                count = list.size();
                runOnUiThread(() -> {
                    Util.loaded();
                    PopupWindow pageWindow = new PageListPopupWindow.Builder(SettingActivity.this)
                            .setListCountGetter(where -> count)
                            .setListGetter((page, list1, where) -> list)
                            .setItemClickListener(this::showActions)
                            .setItemLongClickListener((pageListPopupWindow, position) -> false)
                            .setPageSize(count)
                            .setWindowHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                            .setMask(findViewById(R.id.setting_layout))
                            .build();
                    pageWindow.showAtLocation(findViewById(R.id.setting_layout), Gravity.BOTTOM, 0, 0);
                });
            });
        }

        private void showActions(PageListPopupWindow popupWindow, int position) {
            SettingActivity activity = SettingActivity.this;
            ListPopupWindow listMenu = new ListPopupWindow(activity);
            listMenu.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, new
                    String[]{getString(R.string.renewal), getString(R.string.fetch_api_key), getString(R.string.refresh_api_key)}));
            listMenu.setAnchorView(popupWindow.getItemView(position));
            listMenu.setWidth(450);
            listMenu.setOnItemClickListener((parent, view1, position1, id) -> {
                Map<String, Object> item = popupWindow.getItemData(position);
                switch (position1) {
                    case 0:
                        Util.getDefaultSingleThreadExecutor().execute(() -> {
                            String res = ApiUtil.getInstance().apiRenewal(item.get("id").toString());
                            runOnUiThread(() -> Toast.makeText(activity, res, Toast.LENGTH_LONG).show());
                        });
                        break;
                    case 1:
                        Util.getDefaultSingleThreadExecutor().execute(() -> {
                            String res = ApiUtil.getInstance().fetchKey();
                            runOnUiThread(() -> Toast.makeText(activity, res, Toast.LENGTH_LONG).show());
                        });
                        break;
                    case 2:
                        Util.getDefaultSingleThreadExecutor().execute(() -> {
                            String res = ApiUtil.getInstance().refreshKey();
                            runOnUiThread(() -> Toast.makeText(activity, res, Toast.LENGTH_LONG).show());
                        });
                        break;
                    default:
                        break;
                }
                listMenu.dismiss();
            });
            listMenu.show();
        }
    }

    /**
     * 编辑查重等级
     */
    private void editDuplicateLevel() {
        Util.showEditDialog(
                this,
                getString(R.string.duplicate_level),
                String.valueOf(sp.getInt(DUPLICATE_LEVEL, 5)),
                InputType.TYPE_CLASS_NUMBER,
                null,
                input -> {
                    sp.edit().putInt(DUPLICATE_LEVEL, Integer.parseInt(input))
                            .apply();
                    TextView duplicateLevel = findViewById(R.id.duplicate_level);
                    duplicateLevel.setText(input);
                });
    }

    /**
     * 编辑webdav密码
     */
    private void editWebdavPwd() {
        Util.showEditDialog(
                this,
                getString(R.string.webdav_password),
                sp.getString(WEBDAV_PWD, ""),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                getString(R.string.webdav_password_tip),
                input -> {
                    sp.edit().putString(WEBDAV_PWD, input)
                            .apply();
                    TextView webdavPwd = findViewById(R.id.webdav_pwd_view);
                    webdavPwd.setText(input);
                });
    }

    /**
     * 编辑webdav账号
     */
    private void editWebdavUser() {
        Util.showEditDialog(
                this,
                getString(R.string.webdav_user),
                sp.getString(WEBDAV_USER, ""),
                InputType.TYPE_CLASS_TEXT,
                getString(R.string.webdav_user_tip),
                input -> {
                    sp.edit().putString(WEBDAV_USER, input).apply();
                    TextView webdavUser = findViewById(R.id.webdav_user_view);
                    webdavUser.setText(input);
                });
    }

    /**
     * 编辑api密码
     */
    private void editApiPwd() {
        Util.showEditDialog(
                this,
                getString(R.string.api_password),
                sp.getString(API_PWD, ""),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                getString(R.string.api_password_tip),
                input -> {
                    sp.edit().putString(API_PWD, input)
                            .apply();
                    TextView apiPwd = findViewById(R.id.api_pwd_view);
                    apiPwd.setText(input);
                });
    }

    /**
     * 编辑api账号
     */
    private void editApiUser() {
        Util.showEditDialog(
                this,
                getString(R.string.api_user),
                sp.getString(API_USER, ""),
                InputType.TYPE_CLASS_TEXT,
                getString(R.string.api_user_tip),
                input -> {
                    sp.edit().putString(API_USER, input).apply();
                    TextView apiUser = findViewById(R.id.api_user_view);
                    apiUser.setText(input);
                });
    }

    /**
     * 激活设备管理应用
     *
     * 设置为DeviceOwner {@code adb shell dpm set-device-owner com.maxiye.first/.MyDeviceAdminReceiver}
     */
    public void enableDeviceAdmin() {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager)getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        ComponentName comName = new ComponentName(this, MyDeviceAdminReceiver.class);
        if (!devicePolicyManager.isAdminActive(comName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "激活此设备管理员后可免root停用应用");
            startActivityForResult(intent, INTENT_ADMIN_DEVICE_REQCODE);
        } else {
            Toast.makeText(this, "此App已成为设备管理应用", Toast.LENGTH_SHORT).show();
        }
    }
}
