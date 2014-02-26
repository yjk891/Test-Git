package com.example.emulator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


import com.example.emulator.NanoHTTPD.CommandReceiver;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Filter;
import android.widget.SimpleAdapter;
import android.widget.Toast;


@SuppressLint("NewApi")
public class EmulatorService extends Service {

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}


	public static final String TAG = "EmulatorService";
	public final static int SCREEN_ON = 1;
	public static final int STATUS_CHANGE = 5;
	public static final int LAUNCH_MEMO=4;
	public static final int MSG_WIFI_CHECK = 100;
	public int memo=1;
	public String user=null;
	boolean flag = true;
	public Information info=null;
	
	
	NotificationManager mNM;
	
	private final IBinder mbinder = new LocalBinder();

	public class LocalBinder extends Binder{
		EmulatorService getService(){
			return EmulatorService.this;
		}
	}
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		try {
			NanoHttpd();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mbinder;
	}
	
	public interface sendToClass{
		public void getstatus(Information info_send);
	}
	
	private ArrayList<sendToClass> ClassList = new ArrayList<sendToClass>();

	public void getfrom(){
		sendToClass send = (sendToClass)getApplicationContext() ;
	}
	public void registertoList(sendToClass send) {
		if (!ClassList.contains(send))
			ClassList.add(send);
	}

	public void unregisterfromList(sendToClass send) {
		if (ClassList.contains(send))
			ClassList.remove(send);
	}
	
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d("STATUS","(0)inside broadcastReceiver");
			Log.d("STATUS","intent.getaction =" +action);
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	///WIFI	
			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equalsIgnoreCase(action)) {
				Log.d("STATUS","inside) wifi Changed");
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
				Log.d("STATUS","state = "+state);
				if (state == WifiManager.WIFI_STATE_ENABLED||state==WifiManager.WIFI_STATE_ENABLING) {	
					Toast.makeText(EmulatorService.this, "Wifi ON!", Toast.LENGTH_LONG).show();
					Log.d("STATUS","wifi on");
					info.wifi ="ON";
				}
				
				else if (state==WifiManager.WIFI_STATE_DISABLED||state==WifiManager.WIFI_STATE_DISABLING){
					Toast.makeText(EmulatorService.this, "Wifi OFF!", Toast.LENGTH_LONG).show();
					Log.d("STATUS","wifi off");
					info.wifi="OFF";
				}
				else{
					Log.d("STATUS", "No recognition");	
					Log.d("STATUS","wifi off");
				}
			}
	///BLUETOOTH		
			else if(BluetoothAdapter.ACTION_STATE_CHANGED.equalsIgnoreCase(action)){
				Log.d("STATUS","inside)bluetooth on");
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				Log.d("STATUS","state= "+state);
				//EXTRA_STATE= state_on/off / state_turning_on/off
	
				if(state== BluetoothAdapter.STATE_ON){
					Toast.makeText(EmulatorService.this, "BlueTooth ON!", Toast.LENGTH_LONG).show();
						Log.d("STATUS","bluetooth on");
						info.bluetooth="ON";
					}
				else if(state==BluetoothAdapter.STATE_OFF){

					Toast.makeText(EmulatorService.this, "Bluetooth OFF", Toast.LENGTH_LONG).show();
					Log.d("STATUS","bluetooth off");
					info.bluetooth="OFF";
				}
				else{
					Log.d("STATUS", "No recognition");
					Log.d("STATUS","bluetooth off");
				}
			}
	  ///SCREEN
			else if(Intent.ACTION_SCREEN_OFF.equalsIgnoreCase(action)){
				Toast.makeText(EmulatorService.this, "Screen OFF", Toast.LENGTH_LONG).show();
				Log.d("STATUS","screen_off");
				info.screen="OFF";
			}
			else if(Intent.ACTION_SCREEN_ON.equalsIgnoreCase(action)){
				Toast.makeText(EmulatorService.this, "Screen On", Toast.LENGTH_LONG).show();
				Log.d("STATUS","screen_on");
				info.screen="ON";
			}
//			Log.d("STATUS","1)get status = "+nt);
//			Log.d("STATUS","1++ nt= "+nt.sCmd+"= "+nt.sValue);
			 mHandler.sendMessage(mHandler.obtainMessage(STATUS_CHANGE, info));
		}
    };
	
	
	
//notify the change of BlueTooth or Wifi	
	private Handler mHandler= new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Log.d("STATUS", "1msg.waht ="+msg.what);
			super.handleMessage(msg);
			switch(msg.what){
			case STATUS_CHANGE:
			    Log.i("Inside Status Change", "WOW");
                Information send= (Information)msg.obj;
				for (int i = 0; i < ClassList.size(); i++) {
					sendToClass tp = ClassList.get(i);
					if (tp != null){
						tp.getstatus(send);	
					}
				}
				break;
				
			
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		showNotification();
	}
	
	private NanoHTTPD mHttpd = null;
	private void NanoHttpd() throws IOException {
		
			File_Read();
			
			
			Log.i("###EmulatorService","wifi : "+info.wifi);
			Log.i("###EmulatorService","bluetooth : "+info.bluetooth);
			Log.i("###EmulatorService","screen : "+info.screen);
			mHttpd = new NanoHTTPD(this, 8091, write_str, info);
			mHttpd.registerCommandReceiver(mCommandReceiver);
			Log.d("HTML","html:"+write_str);
		
	}
	
	//저기 클래스의 interface 를 받아와 (commandReceiver)
	private NanoHTTPD.CommandReceiver mCommandReceiver = new NanoHTTPD.CommandReceiver() {
		
		@Override
		public void onCommandReceived(String cmd, String value) {
			Log.d(TAG, "onCommandReceived cmd = " + cmd + " value = " + value);
			Log.d("SCREEN","value.len = "+value.length());
			
			if (cmd.equalsIgnoreCase("screen")) {
				Log.d("screen","value= "+value);
				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				if (value.equalsIgnoreCase("on")) {
					Log.i("###YYS###", "Screen on");
					pm.wakeUp(2000);
					//screen on
					pm.userActivity(SystemClock.uptimeMillis(), false);			
				}else if (value.equalsIgnoreCase("off")) {
					Log.i("###YYS###", "Screen off");
					//screen off
					pm.goToSleep(2000);
					pm.wakeUp(2000);
				}
				
			}
			

			else if(cmd.equalsIgnoreCase("keyboard")){
		
				keyEvent(theKeyBoard.get(value).toString());
				//아..왜 파이널로해야되????으앙!	
			}
			else if(cmd.equalsIgnoreCase("wifi")){
			//	wifi_Manager(cmd, value);
				WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo mWifiInfo = mConnectivityManager.getNetworkInfo(mConnectivityManager.TYPE_WIFI);
					if(value.equalsIgnoreCase("on")){
						wifiManager.setWifiEnabled(true);
					}
					else if(value.equalsIgnoreCase("off")){
						wifiManager.setWifiEnabled(false);
						mHttpd.unregisterCommandReceiver(mCommandReceiver);
						mHttpd.stop();
					}			
			}
			else if(cmd.equalsIgnoreCase("bluetooth")){
//				bluetooth_Manager(cmd, value);
				BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
				mBtAdapter.enable();
					if(value.equalsIgnoreCase("on")){
						mBtAdapter.isEnabled();
					}
					else if(value.equalsIgnoreCase("off")){
						mBtAdapter.disable();
					}
			}
			else
			{
				
				Log.i("Not Command","No exist Command");
			}
		}

	};
	private void wifi_Manager(String cmd, String value){	
		
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifiInfo = mConnectivityManager.getNetworkInfo(mConnectivityManager.TYPE_WIFI);
			if(value.equalsIgnoreCase("on")){
				wifiManager.setWifiEnabled(true);
			}
			else if(value.equalsIgnoreCase("off")){
				wifiManager.setWifiEnabled(false);				
			}
	}
		    	
	private void bluetooth_Manager(String cmd, String value){
		BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();	
		Log.i("Receiver","Log.d");
		mBtAdapter.enable();
			if(value.equalsIgnoreCase("on")){
				mBtAdapter.isEnabled();
			}
			else if(value.equalsIgnoreCase("off")){
				mBtAdapter.disable();
			}

	}
	

	private void keyEvent(String key_value){
				
		final int input = Integer.parseInt(key_value);
		Log.d("kb","input="+input);
		new Thread(new Runnable(){
			public void run(){
				new Instrumentation().sendKeyDownUpSync(input);	
			}
		}).start();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, "Service Destroy", Toast.LENGTH_SHORT).show();
		mNM.cancel(R.string.remote_service_started);
		mHttpd.unregisterCommandReceiver(mCommandReceiver);
		mHttpd.stop();
	}
	
	public void screenOnOff(String value) throws RemoteException {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	
		if (value.equalsIgnoreCase("on")) {
			//screen on
			pm.userActivity(SystemClock.uptimeMillis(), false);			
		} else if (value.equalsIgnoreCase("off")) {
			//screen off
			pm.goToSleep(2000);
			pm.wakeUp(2000);
		}
	}

	
	private void showNotification() {
		// TODO Auto-generated method stub
		CharSequence text = getText(R.string.remote_service_started);

		Notification notification = new Notification(R.drawable.stat_sample,text, System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,new Intent(this, Emulator.class), 0);

		notification.setLatestEventInfo(this,getText(R.string.remote_service_label), text, contentIntent);

		mNM.notify(R.string.remote_service_started, notification);
	}

	byte buffer[];				//저장할 버퍼
	int rlen;						//파일 읽어오는 것의 크기(글자 수)
	int line_Cnt=1;					//Total line cnt
	ByteArrayInputStream bin_s=null;
	BufferedReader reader=null;
	String submit_cmd;
	String write_str;
	String status="";
	Boolean check= true;
	public void File_Read() throws IOException{

		info = new Information();
		Log.i("####EmulatorService","info.blutooth : "+info.bluetooth);
		Log.i("####EmulatorService","info.wifi : "+info.wifi);
		Log.i("####EmulatorService","info.screen : "+info.screen);
	//wifi 상태확인 필터
		IntentFilter wfilter = new IntentFilter();
		wfilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);		
		registerReceiver(mReceiver, wfilter);
	//bluetooth 상태확인 필터
		IntentFilter bfilter = new IntentFilter();
		bfilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(mReceiver,bfilter);
		
		IntentFilter sfilter = new IntentFilter();
		sfilter.addAction(Intent.ACTION_SCREEN_OFF);
		sfilter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(mReceiver, sfilter);
				
		
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifiInfo = mConnectivityManager.getNetworkInfo(mConnectivityManager.TYPE_WIFI);
		
		if(wifiManager.isWifiEnabled())
		{
			info.wifi = "ON";
		}
		BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if(mBtAdapter.isEnabled())
		{
			info.bluetooth = "ON";
		}
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if(pm.isScreenOn())
		{
			info.screen = "ON";
		}

		Log.i("####EmulatorService","info.blutooth : "+info.bluetooth);
		Log.i("####EmulatorService","info.screen : "+info.screen);
		Log.i("####EmulatorService","info.wifi : "+info.wifi);
		
		
		write_str="<html>" +
					"<head>" +
					"<title>Emulator ver 0.1</title>" +
					"</head>" +
					"<body>";
		status= "";
		int user=0;
		InputStream in_s;
		

			Resources res = getResources();							//res
			in_s = res.openRawResource(R.raw.cmd);		//cmd.txt를 InputStream에
	
	   rlen=in_s.available();			//Total Length
	   
		//Returns an estimated number of bytes that can be read or skipped without blocking for more input
	   byte[] buffer = new byte[in_s.available()];
	   in_s.read(buffer);
	   
	   bin_s = new ByteArrayInputStream(buffer, 0, rlen); //Parsing을	(bufferreader 사용)
	   reader = new BufferedReader( new InputStreamReader( bin_s )); //편하게 하기 위해서
	   
	   for(int i=0;i<rlen;i++){			//Check text Total line
		   if(buffer[i]== 10){
			   line_Cnt++;
		   }
	   }
	   
	   int cnt = 0;		//Check for current readed line cnt
	   
	   String str;
	   
	   while((cnt < line_Cnt)&&(str=reader.readLine())!=null){
						//read one line
			Log.d("READ","str="+str);
			cnt++;							//readed one line cnt increase +1
			
			//아마 넘어오면서 뭔가 인코딩되서 넘어오는거같은데, 그거때매 여기서 자꾸죽는듯.....
			if(str.length() == 0){			//Newline '\n' 
			
				write_str = write_str + "</select> <input type=\"submit\"" +"value =" +"\"send\"" + "/>" 
						+ "</form>" +
						"<text><br><br></text>";
			}else{			//한 줄 띄기가 아닐 때만 Parsing을 호출
				Parsing(str);
			}
		}
	
		write_str = write_str + "</select> <input type=\"submit\"" + 
				"value =" +"\"send\"" + "/>" 
						+ "</form>";
		}
	
	
    public void Parsing(String parsing){
			StringTokenizer stoken1 = new StringTokenizer( parsing, "#" );
			String str_partition=null;
			if(stoken1.hasMoreTokens())
			{
				str_partition = stoken1.nextToken();
				if(parsing != str_partition){		//#이 있다는 것
			
					int space_index=str_partition.indexOf(' ');
					if(space_index != -1)
					{
						str_partition = str_partition.trim();
					}
					
					submit_cmd = str_partition;
					write_str = write_str + "<textarea rows=1 cols=10>"
							+ str_partition + "</textarea>";
				}
				else
				{

					stoken1 = new StringTokenizer( parsing, "-" );
					str_partition = stoken1.nextToken();
					if(parsing != str_partition){			//-이 있다는 것
				
						int space_index=str_partition.indexOf(' ');
						if(space_index != -1)
						{
							str_partition = str_partition.trim();
						}	
						write_str = write_str + "<text><br></text><text>"
								+ str_partition + "<br></text>" +
								"<form method=\"post\">" +
								"<select name=\"" + submit_cmd + "\">";
					}
					
					else
					{									//command

						int length = str_partition.length();
						int index = str_partition.indexOf('.');
						int space_index=str_partition.indexOf(' ');
											
						String Cmd = str_partition.substring(index+1, length);
						
						if(space_index != -1)
						{
							Cmd = Cmd.trim();
						}
						
						write_str = write_str + "<option value=\"" + Cmd + "\"" + "selected>"+ 
								Cmd +"</option>";
					}
				}
			}	
			
		}

	 private static Hashtable theKeyBoard = new Hashtable();
	    static
	    {
			StringTokenizer mSt = new StringTokenizer(
				
			   "KEYCODE_UNKNOWN           0 "+
			   "KEYCODE_SOFT_LEFT         1 "+
			   "KEYCODE_SOFT_RIGHT        2 "+
			   "KEYCODE_HOME              3 "+
			   "KEYCODE_BACK              4 "+
			   "KEYCODE_CALL              5 "+
			   "KEYCODE_ENDCALL           6 "+
			   "KEYCODE_0                 7 "+
			   "KEYCODE_1                 8 "+
			   "KEYCODE_2                 9 "+
			   "KEYCODE_3                 10 "+
			   "KEYCODE_4                 11 "+
			   "KEYCODE_5                 12 "+
			   "KEYCODE_6                 13 "+
			   "KEYCODE_7                 14 "+
			   "KEYCODE_8                 15 "+
			   "KEYCODE_9                 16 "+
			   "KEYCODE_STAR              17 "+
			   "KEYCODE_POUND             18 "+
			   "KEYCODE_DPAD_UP           19 "+
			   "KEYCODE_DPAD_DOWN         20 "+
			   "KEYCODE_DPAD_LEFT         21 "+
			   "KEYCODE_DPAD_RIGHT        22 "+
			   "KEYCODE_DPAD_CENTER       23 "+
			   "KEYCODE_VOLUME_UP         24 "+
			   "KEYCODE_VOLUME_DOWN       25 "+
			   "KEYCODE_POWER             26 "+
			   "KEYCODE_CAMERA            27 "+
			   "KEYCODE_CLEAR             28 "+
			   "KEYCODE_A                 29 "+
			   "KEYCODE_B                 30 "+
			   "KEYCODE_C                 31 "+
			   "KEYCODE_D                 32 "+
			   "KEYCODE_E                 33 "+
			   "KEYCODE_F                 34 "+
			   "KEYCODE_G                 35 "+
			   "KEYCODE_H                 36 "+
			   "KEYCODE_I                 37 "+
			   "KEYCODE_J                 38 "+
			   "KEYCODE_K                 39 "+
			   "KEYCODE_L                 40 "+
			   "KEYCODE_M                 41 "+
			   "KEYCODE_N                 42 "+
			   "KEYCODE_O                 43 "+
			   "KEYCODE_P                 44 "+
			   "KEYCODE_Q                 45 "+
			   "KEYCODE_R                 46 "+
			   "KEYCODE_S                 47 "+
			   "KEYCODE_T                 48 "+
			   "KEYCODE_U                 49 "+
			   "KEYCODE_V                 50 "+
			   "KEYCODE_W                 51 "+
			   "KEYCODE_X                 52 "+
			   "KEYCODE_Y                 53 "+
			   "KEYCODE_Z                 54 "+
			   "KEYCODE_COMMA             55 "+
			   "KEYCODE_PERIOD            56 "+
			   "KEYCODE_ALT_LEFT          57 "+
			   "KEYCODE_ALT_RIGHT         58 "+
			   "KEYCODE_SHIFT_LEFT        59 "+
			   "KEYCODE_SHIFT_RIGHT       60 "+
			   "KEYCODE_TAB               61 "+
			   "KEYCODE_SPACE             62 "+
			   "KEYCODE_SYM               63 "+
			   "KEYCODE_EXPLORER          64 "+
			   "KEYCODE_ENVELOPE          65 "+
			   "KEYCODE_ENTER             66 "+
			   "KEYCODE_DEL               67 "+
			   "KEYCODE_GRAVE             68 "+
			   "KEYCODE_MINUS             69 "+
			   "KEYCODE_EQUALS            70 "+
			   "KEYCODE_LEFT_BRACKET      71 "+
			   "KEYCODE_RIGHT_BRACKET     72 "+
			   "KEYCODE_BACKSLASH         73 "+
			   "KEYCODE_SEMICOLON         74 "+
			   "KEYCODE_APOSTROPHE        75 "+
			   "KEYCODE_SLASH             76 "+
			   "KEYCODE_AT                77 "+
			   "KEYCODE_NUM               78 "+
			   "KEYCODE_HEADSETHOOK       79 "+
			   "KEYCODE_FOCUS             80 "+   
			   "KEYCODE_PLUS              81 "+
			   "KEYCODE_MENU              82 "+
			   "KEYCODE_NOTIFICATION      83 "+
			   "KEYCODE_SEARCH            84 "+
			   "KEYCODE_MEDIA_PLAY_PAUSE  85 "+
			   "KEYCODE_MEDIA_STOP        86 "+
			   "KEYCODE_MEDIA_NEXT        87 "+
			   "KEYCODE_MEDIA_PREVIOUS    88 "+
			   "KEYCODE_MEDIA_REWIND      89 "+
			   "KEYCODE_MEDIA_FAST_FORWARD   90 "+
			   "KEYCODE_MUTE              91 "+
			   "KEYCODE_PAGE_UP           92 "+
			   "KEYCODE_PAGE_DOWN         93 "+
			   "KEYCODE_PICTSYMBOLS       94 "+   
			   "KEYCODE_SWITCH_CHARSET    95 "+   
			   "KEYCODE_BUTTON_A          96 "+
			   "KEYCODE_BUTTON_B          97 "+
			   "KEYCODE_BUTTON_C          98 "+
			   "KEYCODE_BUTTON_X          99 "+
			   "KEYCODE_BUTTON_Y          100 "+
			   "KEYCODE_BUTTON_Z          101 "+
			   "KEYCODE_BUTTON_L1         102 "+
			   "KEYCODE_BUTTON_R1         103 "+
			   "KEYCODE_BUTTON_L2         104 "+
			   "KEYCODE_BUTTON_R2         105 "+
			   "KEYCODE_BUTTON_THUMBL     106 "+
			   "KEYCODE_BUTTON_THUMBR     107 "+
			   "KEYCODE_BUTTON_START      108 "+
			   "KEYCODE_BUTTON_SELECT     109 "+
			   "KEYCODE_BUTTON_MODE       110 "+
			   "KEYCODE_ESCAPE            111 "+
			   "KEYCODE_FORWARD_DEL       112 "+
			   "KEYCODE_CTRL_LEFT         113 "+
			   "KEYCODE_CTRL_RIGHT        114 "+
			   "KEYCODE_CAPS_LOCK         115 "+
			   "KEYCODE_SCROLL_LOCK       116 "+
			   "KEYCODE_META_LEFT         117 "+
			   "KEYCODE_META_RIGHT        118 "+
			   "KEYCODE_FUNCTION          119 "+
			   "KEYCODE_SYSRQ             120 "+
			   "KEYCODE_BREAK             121 "+
			   "KEYCODE_MOVE_HOME         122 "+
			   "KEYCODE_MOVE_END          123 "+
			   "KEYCODE_INSERT            124 "+
			   "KEYCODE_FORWARD           125 "+
			   "KEYCODE_MEDIA_PLAY        126 "+
			   "KEYCODE_MEDIA_PAUSE       127 "+
			   "KEYCODE_MEDIA_CLOSE       128 "+
			   "KEYCODE_MEDIA_EJECT       129 "+
			   "KEYCODE_MEDIA_RECORD      130 "+
			   "KEYCODE_F1                131 "+
			   "KEYCODE_F2                132 "+
			   "KEYCODE_F3                133 "+
			   "KEYCODE_F4                134 "+
			   "KEYCODE_F5                135 "+
			   "KEYCODE_F6                136 "+
			   "KEYCODE_F7                137 "+
			   "KEYCODE_F8                138 "+
			   "KEYCODE_F9                139 "+
			   "KEYCODE_F10               140 "+
			   "KEYCODE_F11               141 "+
			   "KEYCODE_F12               142 "+
			   "KEYCODE_NUM_LOCK          143 "+
			   "KEYCODE_NUMPAD_0          144 "+
			   "KEYCODE_NUMPAD_1          145 "+
			   "KEYCODE_NUMPAD_2          146 "+
			   "KEYCODE_NUMPAD_3          147 "+
			   "KEYCODE_NUMPAD_4          148 "+
			   "KEYCODE_NUMPAD_5          149 "+
			   "KEYCODE_NUMPAD_6          150 "+
			   "KEYCODE_NUMPAD_7          151 "+
			   "KEYCODE_NUMPAD_8          152 "+
			   "KEYCODE_NUMPAD_9          153 "+
			   "KEYCODE_NUMPAD_DIVIDE     154 "+
			   "KEYCODE_NUMPAD_MULTIPLY   155 "+
			   "KEYCODE_NUMPAD_SUBTRACT   156 "+
			   "KEYCODE_NUMPAD_ADD        157 "+
			   "KEYCODE_NUMPAD_DOT        158 "+
			   "KEYCODE_NUMPAD_COMMA      159 "+
			   "KEYCODE_NUMPAD_ENTER      160 "+
			   "KEYCODE_NUMPAD_EQUALS     161 "+
			   "KEYCODE_NUMPAD_LEFT_PAREN   162 "+
			   "KEYCODE_NUMPAD_RIGHT_PAREN   163 "+
			   "KEYCODE_VOLUME_MUTE       164 "+
			   "KEYCODE_INFO              165 "+
			   "KEYCODE_CHANNEL_UP        166 "+
			   "KEYCODE_CHANNEL_DOWN      167 "+
			   "KEYCODE_ZOOM_IN           168 "+
			   "KEYCODE_ZOOM_OUT          169 "+
			   "KEYCODE_TV                170 "+
			   "KEYCODE_WINDOW            171 "+
			   "KEYCODE_GUIDE             172 "+
			   "KEYCODE_DVR               173 "+
			   "KEYCODE_BOOKMARK          174 "+
			   "KEYCODE_CAPTIONS          175 "+
			   "KEYCODE_SETTINGS          176 "+
			   "KEYCODE_TV_POWER          177 "+
			   "KEYCODE_TV_INPUT          178 "+
			   "KEYCODE_STB_POWER         179 "+
			   "KEYCODE_STB_INPUT         180 "+
			   "KEYCODE_AVR_POWER         181 "+
			   "KEYCODE_AVR_INPUT         182 "+
			   "KEYCODE_PROG_RED          183 "+
			   "KEYCODE_PROG_GREEN        184 "+
			   "KEYCODE_PROG_YELLOW       185 "+
			   "KEYCODE_PROG_BLUE         186 "+
			   "KEYCODE_APP_SWITCH        187 "+
			   "KEYCODE_BUTTON_1          188 "+
			   "KEYCODE_BUTTON_2          189 "+
			   "KEYCODE_BUTTON_3          190 "+
			   "KEYCODE_BUTTON_4          191 "+
			   "KEYCODE_BUTTON_5          192 "+
			   "KEYCODE_BUTTON_6          193 "+
			   "KEYCODE_BUTTON_7          194 "+
			   "KEYCODE_BUTTON_8          195 "+
			   "KEYCODE_BUTTON_9          196 "+
			   "KEYCODE_BUTTON_10         197 "+
			   "KEYCODE_BUTTON_11         198 "+
			   "KEYCODE_BUTTON_12         199 "+
			   "KEYCODE_BUTTON_13         200 "+
			   "KEYCODE_BUTTON_14         201 "+
			   "KEYCODE_BUTTON_15         202 "+
			   "KEYCODE_BUTTON_16         203 "+
			   "KEYCODE_LANGUAGE_SWITCH   204 "+
			   "KEYCODE_MANNER_MODE       205 "+
			   "KEYCODE_3D_MODE           206 "+
			   "KEYCODE_CONTACTS          207 "+
			   "KEYCODE_CALENDAR          208 "+
			   "KEYCODE_MUSIC             209 "+
			   "KEYCODE_CALCULATOR        210 "+
			   "KEYCODE_ZENKAKU_HANKAKU   211 "+
			   "KEYCODE_EISU              212 "+
			   "KEYCODE_MUHENKAN          213 "+
			   "KEYCODE_HENKAN            214 "+
			   "KEYCODE_KATAKANA_HIRAGANA 215 "+
			   "KEYCODE_YEN               216 "+
			   "KEYCODE_RO                217 "+
			   "KEYCODE_KANA              218 "+
			   "KEYCODE_ASSIST            219 "+
			   "KEYCODE_BRIGHTNESS_DOWN   220 "+
			   "KEYCODE_BRIGHTNESS_UP     221 "+
			   "KEYCODE_MEDIA_AUDIO_TRACK   222 "+
			   "LAST_KEYCODE             KEYCODE_MEDIA_AUDIO_TRACK ");
					while ( mSt.hasMoreTokens())
				theKeyBoard.put( mSt.nextToken(), mSt.nextToken());
					
					
		}
		public static void main(String[] args) {
	
		}
}
