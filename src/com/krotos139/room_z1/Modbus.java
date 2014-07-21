/*
    Author: Iuri Iakovlev <krotos139@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

  (Это свободная программа: вы можете перераспространять ее и/или изменять
   ее на условиях Стандартной общественной лицензии GNU в том виде, в каком
   она была опубликована Фондом свободного программного обеспечения; либо
   версии 3 лицензии, либо (по вашему выбору) любой более поздней версии.

   Эта программа распространяется в надежде, что она будет полезной,
   но БЕЗО ВСЯКИХ ГАРАНТИЙ; даже без неявной гарантии ТОВАРНОГО ВИДА
   или ПРИГОДНОСТИ ДЛЯ ОПРЕДЕЛЕННЫХ ЦЕЛЕЙ. Подробнее см. в Стандартной
   общественной лицензии GNU.

   Вы должны были получить копию Стандартной общественной лицензии GNU
   вместе с этой программой. Если это не так, см.
   <http://www.gnu.org/licenses/>.)
*/

package com.krotos139.room_z1;

import java.lang.reflect.Array;

import android.util.Log;

public class Modbus {
	private final static String TAG = "Modbus";

	// PDU
	int reg;
	int count;
	int cmd;
	int data;
	int data_array[];
	
	// APU
	int slave_addr;
	
	// TPDU
	int TransactionID;
	int ProtocolID;
	int Length;
	
	final static int cmd_read_coil		= 0x01;
	final static int cmd_read_discrete	= 0x02;
	final static int cmd_read_holding	= 0x03;
	final static int cmd_read_input		= 0x04;
	final static int cmd_write_coil		= 0x05;
	final static int cmd_write_holding	= 0x06;
	final static int cmd_write_coils	= 0x0F;
	final static int cmd_write_holdings	= 0x10;
	
	Modbus() {
		super();
		this.TransactionID = 0;
		this.ProtocolID = 0;
		this.Length = 0;
	}

	byte[] ReadHoldingRegisters(int in_reg) {
		byte pack[];
		byte[] crc;

		reg = in_reg;
		
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
		
		return pack;
	}
	
	byte[] PresetMultipleRegisters(int in_reg, int in_data) {
		byte pack[];
		byte[] crc;
		
		reg = in_reg;
		data = in_data;

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
	
	byte[] RTU_pack(byte pack_in[]) {
		byte pack[];
		byte[] crc;
		int i;
		
		pack = new byte[pack_in.length+2];
		crc = CRCMODBUS.calc(pack_in);
		
		for (i=0; i<pack_in.length ;i++) {
			pack[i] = pack_in[i];
		}
		
		pack[pack_in.length+0] = (byte) crc[0];
		pack[pack_in.length+1] = (byte) crc[1];
		
		return pack;
	}
	
	byte[] RTU_unpack(byte pack_in[]) {
		return RTU_unpack(pack_in, pack_in.length);
	}
	
	byte[] RTU_unpack(byte pack_in[], int length) {
		byte pack[];
		byte[] crc;
		int i;
		
		pack = new byte[length-2];
		crc = CRCMODBUS.calc(pack_in, length-2);
		
		if ( (pack_in[length-2] != (byte) crc[0]) || (pack_in[length-1] != (byte) crc[1]) ) {
			Log.e(TAG, "Error modbus CRC");
			return null;
		}
		
		for (i=0; i<pack.length ;i++) {
			pack[i] = pack_in[i];
		}
		
		return pack;
	}

	byte[] TPDU_pack(byte pack_in[]) {
		byte pack[];
		int i;
		
		Length = pack_in.length;
		
		pack = new byte[Length+6];
		pack[0] = (byte) ((TransactionID >> 8) & 0xFF);
		pack[1] = (byte) (TransactionID & 0xFF);
		pack[2] = (byte) ((ProtocolID >> 8) & 0xFF);
		pack[3] = (byte) (ProtocolID & 0xFF);
		pack[4] = (byte) ((Length >> 8) & 0xFF);
		pack[5] = (byte) (Length & 0xFF);

		for (i=0; i<pack_in.length ;i++) {
			pack[i+6] = pack_in[i];
		}
		
		return pack;
	}
	
	byte[] TPDU_unpack(byte pack_in[]) {
		return TPDU_unpack(pack_in, pack_in.length);
	}
	
	byte[] TPDU_unpack(byte pack_in[], int length) {
		byte pack[];
		int i;
		
		if (length<=6) return null;
		
		this.TransactionID	=	pack_in[0] << 8;
		this.TransactionID	+=	pack_in[1];
		this.ProtocolID		=	pack_in[2] << 8;
		this.ProtocolID		+=	pack_in[3];
		this.Length			=	pack_in[4] << 8;
		this.Length			+=	pack_in[5];
		
		if (length != this.Length+6) return null;
		
		pack = new byte[this.Length];
		
		for (i=0; i<pack.length ;i++) {
			pack[i] = pack_in[i+6];
		}
		
		return pack;
	}
	

	byte[] PDU_encode_request() {
		byte pack[];
		int i;
		
		switch (this.cmd) {
			case cmd_read_coil:
			case cmd_read_discrete:
			case cmd_read_holding:
			case cmd_read_input:
			case cmd_write_coil:
			case cmd_write_holding:
				pack = new byte[6];
				break;
			case cmd_write_coils:
			case cmd_write_holdings:
				pack = new byte[7+this.count*2];
				break;
			default:
				return null;
		}
		
		pack[0] = (byte) this.slave_addr;
		pack[1] = (byte) this.cmd;
		switch (this.cmd) {
			case cmd_read_coil:
			case cmd_read_discrete:
			case cmd_read_holding:
			case cmd_read_input:
				pack[2] = (byte) ((this.reg >> 8) & 0xFF);
				pack[3] = (byte) (this.reg & 0xFF);
				pack[4] = (byte) ((this.count >> 8) & 0xFF);
				pack[5] = (byte) (this.count & 0xFF);
				break;
		case cmd_write_coil:
		case cmd_write_holding:
				pack[2] = (byte) ((this.reg >> 8) & 0xFF);
				pack[3] = (byte) (this.reg & 0xFF);
				pack[4] = (byte) ((this.data >> 8) & 0xFF);
				pack[5] = (byte) (this.data & 0xFF);
				break;
		case cmd_write_coils:
		case cmd_write_holdings:
			pack[2] = (byte) ((this.reg >> 8) & 0xFF);
			pack[3] = (byte) (this.reg & 0xFF);
			pack[4] = (byte) ((this.count >> 8) & 0xFF);
			pack[5] = (byte) (this.count & 0xFF);
			pack[6] = (byte) (this.count*2);
			for (i=0;i<this.count;i++) {
				pack[7+i*2] = (byte) ((this.data_array[i] >> 8) & 0xFF);
				pack[8+i*2] = (byte) (this.data_array[i] & 0xFF);
			}
				break;
		}
		return pack;
	}
	
	byte[] PDU_encode_answer() {
		byte pack[];
		int i;
		
		switch (this.cmd) {
			case cmd_read_coil:
			case cmd_read_discrete:
			case cmd_read_holding:
			case cmd_read_input:
				pack = new byte[3+this.count*2];
				break;
			case cmd_write_coil:
			case cmd_write_holding:
			case cmd_write_coils:
			case cmd_write_holdings:
				pack = new byte[6];
				break;
			default:
				return null;
		}
		
		pack[0] = (byte) this.slave_addr;
		pack[1] = (byte) this.cmd;
		switch (this.cmd) {
			case cmd_read_coil:
			case cmd_read_discrete:
			case cmd_read_holding:
			case cmd_read_input:
				pack[2] = (byte) (this.count*2);
				for (i=0;i<this.count;i++) {
					pack[3+i*2] = (byte) ((this.data_array[i] >> 8) & 0xFF);
					pack[4+i*2] = (byte) (this.data_array[i] & 0xFF);
				}
				break;
		case cmd_write_coil:
		case cmd_write_holding:
				pack[2] = (byte) ((this.reg >> 8) & 0xFF);
				pack[3] = (byte) (this.reg & 0xFF);
				pack[4] = (byte) ((this.data >> 8) & 0xFF);
				pack[5] = (byte) (this.data & 0xFF);
				break;
		case cmd_write_coils:
		case cmd_write_holdings:
				pack[2] = (byte) ((this.reg >> 8) & 0xFF);
				pack[3] = (byte) (this.reg & 0xFF);
				pack[4] = (byte) ((this.count >> 8) & 0xFF);
				pack[5] = (byte) (this.count & 0xFF);
				break;
		}
		return pack;
	}

	
	void PDU_decode_request(byte pack[]) {
		int i;
		
		this.slave_addr = pack[0];
		this.cmd = pack[1];
		switch (this.cmd) {
			case cmd_read_coil:
			case cmd_read_discrete:
			case cmd_read_holding:
			case cmd_read_input:
				this.reg	=	pack[2] << 8;
				this.reg	+=	pack[3];
				this.count	=	pack[4] << 8;
				this.count	+=	pack[5];
				break;
			case cmd_write_coil:
			case cmd_write_holding:
				this.reg	=	pack[2] << 8;
				this.reg	+=	pack[3];
				this.data	=	pack[4] << 8;
				this.data	+=	pack[5];
				break;
			case cmd_write_coils:
			case cmd_write_holdings:
				this.reg	=	pack[2] << 8;
				this.reg	+=	pack[3];
				this.count	=	pack[4] << 8;
				this.count	+=	pack[5];
				this.data_array = new int[this.count];
				for (i=0;i<count;i++) {
					this.data_array[i]	=	pack[6+i*2] << 8;
					this.data_array[i]	+=	pack[7+i*2];
				}
				break;
		}
		
	}
	
	void PDU_decode_answer(byte pack[]) {
		int i;
		
		this.slave_addr = pack[0];
		this.cmd = pack[1];
		switch (this.cmd) {
			case cmd_read_coil:
			case cmd_read_discrete:
			case cmd_read_holding:
			case cmd_read_input:
				this.count	=	pack[2] / 2;
				this.data_array = new int[this.count];
				for (i=0;i<count;i++) {
					this.data_array[i]	=	pack[3+i*2] << 8;
					this.data_array[i]	+=	pack[4+i*2];
				}
				break;
			case cmd_write_coil:
			case cmd_write_holding:
				this.reg	=	pack[2] << 8;
				this.reg	+=	pack[3];
				this.data	=	pack[4] << 8;
				this.data	+=	pack[5];
				break;
			case cmd_write_coils:
			case cmd_write_holdings:
				this.reg	=	pack[2] << 8;
				this.reg	+=	pack[3];
				this.count	=	pack[4] << 8;
				this.count	+=	pack[5];
				break;
		}
		
	}
	
}
