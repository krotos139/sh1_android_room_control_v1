package com.krotos139.room_z1;

import android.util.Log;

public class Modbus {
	private final static String TAG = "Arinc429";

	static int label_time		= 1;
	static int label_id			= 2;
	static int label_do1		= 3;
	static int label_do2		= 4;
	static int label_di1		= 5;
	static int label_di2		= 6;
	static int label_temp		= 7;
	static int label_hydro		= 8;
	static int label_ir_cmd		= 9;
	static int label_ir_interface	= 10;
		
	int slave_addr;
	
	Modbus(int addr) {
		super();
		this.slave_addr = addr;
	}
	
	Modbus(byte pack[]) {
		super();
		
		this.slave_addr = pack[0];
		
		
//		p = 0;
//		for ( int i=0 ; i<31 ; i++ ) {
//			p ^= (pack >> i) & 1;
//		}
//		
//		this.label	= pack & 0x0F;
//		this.data	= (pack >> 10) & 0x3FF;
//		this.sdi	= (pack >> 8) & 3;
//		
//		
//		if (((pack >> 31) & 1) != p ) {
//			this.ssm	= ssm_nсd;
//		}
//		this.ssm	= (pack >> 29) & 3;
	}
	
//	Arinc429(byte pack[]) {
//		this(pack[0] + (pack[1] << 8) + (pack[2] << 16) + (pack[3] << 24));
//	}



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
