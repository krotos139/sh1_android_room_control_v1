package com.krotos139.room_z1;

import android.util.Log;

public class Modbus {
	private final static String TAG = "Modbus";

		
	int slave_addr;
	
	Modbus(int addr) {
		super();
		this.slave_addr = addr;
	}

	byte[] ReadHoldingRegisters(int reg) {
		byte pack[];
		byte[] crc;

		pack = new byte[8];
		pack[0] = (byte) this.slave_addr;
		pack[1] = 3;
		pack[2] = 0;
		pack[3] = (byte) reg;
		pack[4] = 0;
		pack[5] = (byte) reg;
		crc = CRCMODBUS.calc(pack, 6);
		pack[6] = (byte) crc[0];
		pack[7] = (byte) crc[1];
		
//		Log.d(TAG, "Pack="+pack+"");
		
		return pack;
	}
	
	byte[] PresetMultipleRegisters(int reg, int data) {
		byte pack[];
		byte[] crc;

		pack = new byte[11];
		pack[0] = (byte) this.slave_addr;
		pack[1] = 16;
		pack[2] = (byte) ((reg >> 8) & 0xFF);
		pack[3] = (byte) (reg & 0xFF);
		pack[4] = 0;
		pack[5] = 1;
		pack[6] = 2;
		pack[7] = (byte) ((data >> 8) & 0xFF);
		pack[8] = (byte) (data & 0xFF);
		crc = CRCMODBUS.calc(pack, 9);
		pack[9] = (byte) crc[0];
		pack[10] = (byte) crc[1];
		
//		Log.d(TAG, "Pack="+pack+"");
		
		return pack;
	}
}
