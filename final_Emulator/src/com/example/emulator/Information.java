package com.example.emulator;


public class Information{
	public String wifi;
	public String bluetooth;
	public String screen;
	
	public Information()
	{
		wifi = "OFF";
		bluetooth = "OFF";
		screen = "OFF";
	}
	
	public void setValue_wifi(String setwifi, String setbluetooth, String setscreen)
	{
		wifi = setwifi;
		bluetooth = setbluetooth;
		screen = setscreen;
	}
}

