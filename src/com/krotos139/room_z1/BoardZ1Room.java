package com.krotos139.room_z1;

import android.content.Context;
import android.util.Log;


public class BoardZ1Room implements DataResiver {
	private final static String TAG = "BoardZ1Room";
	
	public static enum FILE {
		DEVICE_ID,
		STATE,
		
		RELAY1,
		RELAY2,
		RELAY3,
		RELAY4,
		RELAY5,
		RELAY6,
		RELAY7,
		RELAY8,
		
		LED_R,
		LED_G,
		LED_B,
		
		TEMPERATURE,
		HUMIDITY,
		PIR,
		MQ2,
		CURRENT,

		IR_MODE,
		IR_DH,
		IR_DL,
		IR_SIZE,
		
		COUNT_FILES
	}
	
	public int db [];
	
	private USB_IO usb;
	private Modbus bus;
	
	public BoardZ1Room(Context c) {
		Log.d(TAG, "BoardZ1Room db.len="+FILE.values().length+"");
		db = new int[FILE.values().length];
		
        usb = new USB_IO(this, c);
        usb.findDevice();
        
		bus = new Modbus(1);
	}

	public int read(int f) {
		Log.d(TAG, "read f="+f+"");
		return db[f];
		//return 0;
		
	}
	public int read(FILE f) {
		return read(f.ordinal());
	}
	
	public void write(int f, int value) {
		byte msg[];
		Log.d(TAG, "write f="+f+" v="+value);		
		
		db[f] = value;
		
		msg = bus.PresetMultipleRegisters(f, value);
		usb.Transmit(msg);
		
	}
	
	public void write(FILE f, int value) {
		write(f.ordinal(), value);			
	}

	@Override
	public void resive(byte[] data, int len) {
		// TODO Auto-generated method stub
		
	}
	
}
