package com.example.emulator;

import com.example.emulator.EmulatorService.LocalBinder;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Emulator extends Activity {
	
	private Button btn_start,btn_stop;
	private Button mWifi_Setting;
	EmulatorService mService = null;
	NetworkInfo mWifiInfo;
	TextView wifiStatus;
	TextView wifi_ip;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Toast.makeText(Emulator.this, "onserviceDisConnected" ,Toast.LENGTH_SHORT).show();
			mService =null;
		}
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			Toast.makeText(Emulator.this, "onserviceConnected" ,Toast.LENGTH_SHORT).show();
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
		}
	};
	
	private BroadcastReceiver wReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("####BroadcastReceiver####","BroadcastReceiver Call");
			
			String action = intent.getAction();
			Log.d("ACTIVITY","action = "+action);
					
				if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
					mWifiInfo= intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
						if(mWifiInfo.isConnected()) {
    
							Log.d("Inetify", "1)Wifi is connected: " + String.valueOf(mWifiInfo));
						}
				}
			
				if(WifiManager.WIFI_STATE_CHANGED_ACTION.equalsIgnoreCase(action)){	
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
				Log.d("ACTIVITY","state=="+state);
				WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				int ipAddress = wifiInfo.getIpAddress();
				
				
				if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
					mWifiInfo= intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
						if(mWifiInfo.isConnected()) {
    
							Log.d("Inetify", "2)Wifi is connected: " + String.valueOf(mWifiInfo));
						}
				}
				//if (state==WifiManager.WIFI_STATE_ENABLED)
				if( ipAddress != 0)
				{
					Log.i("####IFIFIFIFIF####","IP Address");
				}
				
				if(mWifiInfo.getState() != null)
				{
					Log.i("####IFIFIFIFIF####","getstate");
				}
				if ( mWifiInfo.isConnected() )
				{
					Log.i("####IFIFIFIFIF####","Network Connected Button Enalbe");
					String sIp = String.format("%d.%d.%d.%d",
						       (ipAddress & 0xff),
						       (ipAddress >> 8 & 0xff),
						       (ipAddress >> 16 & 0xff),
						       (ipAddress >> 24 & 0xff));
					
					Toast.makeText(Emulator.this, sIp, Toast.LENGTH_LONG).show();
					wifi_ip.setText("Ip:" + sIp);
					StringBuilder wifiString= new StringBuilder();
					wifiString.append("WIFI: ")	
					.append(mWifiInfo.isAvailable());
					wifiStatus.setText(wifiString);
					btn_start.setEnabled(true);
					btn_stop.setEnabled(true);
				}
			}
			}
		};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);	
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(wReceiver,filter);
		
		setContentView(R.layout.activity_emulator);
		//make Button	
		btn_start= (Button) findViewById(R.id.btn_start);
		btn_stop= (Button) findViewById(R.id.btn_stop);
		mWifi_Setting = (Button)findViewById(R.id.ip_setting);
		
		wifi_ip = (TextView) findViewById(R.id.wifi_ip);
		wifiStatus= (TextView) findViewById(R.id.wifi_state);
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifiInfo = mConnectivityManager.getNetworkInfo(mConnectivityManager.TYPE_WIFI);
		
		StringBuilder wifiString= new StringBuilder();
		wifiString.append("WIFI: ")	
		.append(mWifiInfo.isAvailable());
		registerReceiver(wReceiver,filter);
		
		wifiStatus.setText(wifiString);
		mWifi_Setting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
				startActivity(gpsOptionsIntent); 
			}
		});

		//BindService		
		btn_start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {		
				Intent intent = new Intent(Emulator.this, EmulatorService.class);
				bindService(intent,mConnection,Context.BIND_AUTO_CREATE);
				Toast.makeText(Emulator.this, "Bind()" ,Toast.LENGTH_SHORT).show();	
			}
		});
		
	//unBindService
		btn_stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				unbindService(mConnection);
				
//				Log.d("UNBIND","unbindservice");
//				Toast.makeText(Emulator.this, "UnBind()" ,Toast.LENGTH_SHORT).show();
			}
		});
	}

}
