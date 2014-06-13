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
	
	private int db [];
	
	private USB_IO usb;
	private Modbus bus;
	
	public BoardZ1Room(Context c) {
		Log.d(TAG, "BoardZ1Room db.len="+FILE.values().length+"");
		db = new int[FILE.values().length];
		
        usb = new USB_IO(this, c);
        usb.findDevice();
        
		bus = new Modbus(1);
	}

	public int read(FILE f) {
		Log.d(TAG, "read f="+f.ordinal()+"");
		return db[f.ordinal()];
		//return 0;
		
	}
	
	public void write(FILE f, int value) {
		byte msg[];
		Log.d(TAG, "write f="+f.ordinal()+" v="+value);		
		
		db[f.ordinal()] = value;
		
		msg = bus.PresetMultipleRegisters(f.ordinal(), value);
		usb.Transmit(msg);
		
	}

	@Override
	public void resive(byte[] data, int len) {
		// TODO Auto-generated method stub
		
	}
	
}
