package com.maxiye.first;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.maxiye.first.util.MyLog;
import com.maxiye.first.util.PermissionUtil;
import com.maxiye.first.util.Util;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class CommonActivity extends AppCompatActivity {
    public static final String TYPE = "type";
    public static final String TYPE_PROXY = "proxy_setting";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String type = bundle.getString(TYPE, TYPE_PROXY);
            if (TYPE_PROXY.equals(type)) {
                new ProxySetting().init();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] pers, @NonNull int[] res) {
        PermissionUtil.res(this, reqCode, pers, res);
    }

    /**
     * 代理设置
     */
    class ProxySetting {
        /**
         * 初始化代理设置页面
         */
        void init() {
            setContentView(R.layout.setting_proxy);
            SharedPreferences sp = getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
            TextView host = findViewById(R.id.proxy_host_view);
            TextView port = findViewById(R.id.proxy_port_view);
            SwitchMaterial proxyOnOff = findViewById(R.id.proxy_on_off);
            proxyOnOff.setChecked(getStatus());
            host.setText(sp.getString(SettingActivity.PROXY_HOST, getText(R.string.not_set).toString()));
            port.setText(sp.getString(SettingActivity.PROXY_PORT, "0"));
            proxyOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    on(String.valueOf(host.getText()), Integer.parseInt(String.valueOf(port.getText())));
                } else {
                    off();
                }
            });
            findViewById(R.id.proxy_host_cfg).setOnClickListener(view -> editProxyHost());
            findViewById(R.id.proxy_port_cfg).setOnClickListener(view -> editProxyPort());
        }

        // 获取wifi配置列表
        private WifiConfiguration getCurrentWifiConfiguration(WifiManager wifiManager) {
            if (!wifiManager.isWifiEnabled()) {
                return null;
            }
            if (ActivityCompat.checkSelfPermission(CommonActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            List<WifiConfiguration> configurationList = wifiManager.getConfiguredNetworks();
            WifiConfiguration configuration = null;
            int cur = wifiManager.getConnectionInfo().getNetworkId();
            for (int i = 0; i < configurationList.size(); ++i) {
                WifiConfiguration wifiConfiguration = configurationList.get(i);
                if (wifiConfiguration.networkId == cur) {
                    configuration = wifiConfiguration;
                    break;
                }
            }
            return configuration;
        }


        /**
         * 开启代理
         *
         * @param host String
         * @param port String
         */
        private void on(String host, int port) {
            PermissionUtil.req(CommonActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE}, PermissionUtil.RequestCode.CHANGE_WIFI_STATE, (result) -> {
                WifiManager wifiManager =(WifiManager)getSystemService(Context.WIFI_SERVICE);
                WifiConfiguration config = getCurrentWifiConfiguration(wifiManager);
                if(config == null) {
                    Toast.makeText(CommonActivity.this, "wifi配置获取为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    config.setHttpProxy(null);
                    config.setHttpProxy(ProxyInfo.buildDirectProxy(host, port));
                    // 必须为DeviceOwner
                    int updateResult = wifiManager.updateNetwork(config);
//                wifiManager.disconnect();
//                wifiManager.reconnect();
                    Toast.makeText(CommonActivity.this, "代理设置" + (updateResult != -1), Toast.LENGTH_SHORT).show();
                    MyLog.i("开启设置wifi代理", "结果：" + updateResult);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(CommonActivity.this, "代理设置失败：" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * 获取代理开启状态
         * @return bool
         */
        private boolean getStatus() {
            WifiManager wifiManager =(WifiManager)getSystemService(Context.WIFI_SERVICE);
            WifiConfiguration config = getCurrentWifiConfiguration(wifiManager);
            if(config == null) {
                Toast.makeText(CommonActivity.this, "wifi配置获取为空", Toast.LENGTH_SHORT).show();
                return false;
            }
            ProxyInfo proxyInfo = config.getHttpProxy();
            return proxyInfo != null;
        }

        /**
         * 关闭代理
         *
         */
        private void off() {
            PermissionUtil.req(CommonActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE}, PermissionUtil.RequestCode.CHANGE_WIFI_STATE, (result) -> {
                WifiManager wifiManager =(WifiManager)getSystemService(Context.WIFI_SERVICE);
                WifiConfiguration config = getCurrentWifiConfiguration(wifiManager);
                if(config == null) {
                    Toast.makeText(CommonActivity.this, "wifi配置获取为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                config.setHttpProxy(null);
                // 必须为DeviceOwner
                int updateResult = wifiManager.updateNetwork(config);
//                wifiManager.disconnect();
//                wifiManager.reconnect();
                Toast.makeText(CommonActivity.this, "取消代理设置" + (updateResult != -1), Toast.LENGTH_SHORT).show();
                MyLog.i("关闭wifi代理", "结果：" + updateResult);
            });
        }

        /**
         * 编辑代理主机弹窗
         */
        private void editProxyHost() {
            SharedPreferences sp = getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
            Util.showEditDialog(
                    CommonActivity.this,
                    getString(R.string.host),
                    sp.getString(SettingActivity.PROXY_HOST, ""),
                    InputType.TYPE_CLASS_TEXT,
                    "127.0.0.1",
                    input -> {
                        sp.edit().putString(SettingActivity.PROXY_HOST, input)
                                .apply();
                        TextView textView = findViewById(R.id.proxy_host_view);
                        textView.setText(input);
                    });
        }

        /**
         * 编辑代理端口弹窗
         */
        private void editProxyPort() {
            SharedPreferences sp = getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
            Util.showEditDialog(
                    CommonActivity.this,
                    getString(R.string.port),
                    sp.getString(SettingActivity.PROXY_PORT, ""),
                    InputType.TYPE_NUMBER_FLAG_SIGNED,
                    "8888",
                    input -> {
                        sp.edit().putString(SettingActivity.PROXY_PORT, input).apply();
                        TextView textView = findViewById(R.id.proxy_port_view);
                        textView.setText(input);
                    });
        }
    }
}
