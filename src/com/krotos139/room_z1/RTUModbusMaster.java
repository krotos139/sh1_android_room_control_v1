package com.krotos139.room_z1;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.krotos139.room_z1.BoardZ1Room.register;

public class RTUModbusMaster implements BoardListener {
	private final static String TAG = "RTUModbusMaster";
	
	private static final String ACTION_USB_PERMISSION = "com.krotos139.room_z1";
    private static final int ARDUINO_USB_VENDOR_ID = 0x2341;
    private static final int ARDUINO_UNO_USB_PRODUCT_ID = 0x01;
    private static final int ARDUINO_MEGA_2560_USB_PRODUCT_ID = 0x10;
    private static final int ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID = 0x42;
    private static final int ARDUINO_UNO_R3_USB_PRODUCT_ID = 0x43;
    private static final int ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID = 0x44;
    private static final int ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID = 0x3F;
    private static final int ARDUINO_LEONARDO_ADK_USB_PRODUCT_ID = 0x8036;
	
	//private USB_IO usb;
	private UsbSerialPort port;
	private BoardZ1Room board;
	
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
        	Log.d(TAG, "read_all");
        	read_all();

            timerHandler.postDelayed(this, 1000);
        }
    };
    

	
	public RTUModbusMaster(Context c, BoardZ1Room board) {
		this.board = board; 
		// Register callback
		this.board.list_write_callbacks.add(this);
		
		ProbeTable customTable = new ProbeTable();
		customTable.addProduct(ARDUINO_USB_VENDOR_ID, ARDUINO_LEONARDO_ADK_USB_PRODUCT_ID, CdcAcmSerialDriver.class);
		UsbSerialProber prober = new UsbSerialProber(customTable);
		
		// Find all available drivers from attached devices.
		UsbManager manager = (UsbManager) c.getSystemService(Context.USB_SERVICE);
		List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);
		if (availableDrivers.isEmpty()) {
		  return;
		}

		// Open a connection to the first available driver.
		UsbSerialDriver driver = availableDrivers.get(0);
		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(c, 0, new Intent(ACTION_USB_PERMISSION), 0);
		manager.requestPermission(driver.getDevice(), mPermissionIntent);
		
		UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
		if (connection == null) {

		  return;
		}
		List<UsbSerialPort> ports = driver.getPorts();
		Log.d(TAG, "Port0:"+ports.get(0).toString());
		
		// Read some data! Most have just one port (port 0).
		port = ports.get(0);
		
		try {
		  port.open(connection);
		  port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
		  port.setDTR(true);
//		  port.setRTS(false);
		  byte pack[] = new byte[128];
		  int pack_length = port.read(pack, 1000);
		} catch (IOException e) {
		  // Deal with error.
			e.printStackTrace();
		  return;
		} 
        
        Log.d(TAG, "Run service");
        timerHandler.postDelayed(timerRunnable, 0);
	}
	
	
	public void read(int f) {
		byte pdu[];
		byte rtu[]; 
		Modbus mb = new Modbus();	
		
		mb.slave_addr = 1;
		mb.cmd = Modbus.cmd_read_holding;
		mb.reg = f;
		mb.count = 1;
		
		pdu = mb.PDU_encode_request();
		rtu = mb.RTU_pack(pdu);

		try {
			port.write(rtu, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		processing(mb.reg);
	}
	public void read_all() {
		Iterator<register> db_l_update_iterator;
		
		db_l_update_iterator = this.board.db_l_update.iterator();
		while (db_l_update_iterator.hasNext()) {
			register f = db_l_update_iterator.next();
			read(f.ordinal());
		}
	}
	
	public void write(int f, int value) {
		byte pdu[];
		byte rtu[]; 
		Modbus mb = new Modbus();	
		
		Log.d(TAG, "write f="+f+" v="+value);
		
		mb.slave_addr = 1;
		mb.cmd = Modbus.cmd_write_holdings;
		mb.reg = f;
		mb.count = 1;
		mb.data_array = new int[mb.count];
		mb.data_array[0] = value;
		
		pdu = mb.PDU_encode_request();
		rtu = mb.RTU_pack(pdu);
		
		try {
			port.write(rtu, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		processing(mb.reg);
	}
	
	private void processing(int query_read_reg) {
		int i_read_c, result;
    	byte pack[] = new byte[128];
    	int pack_length = 0;
    	try {
    		for (i_read_c=0;i_read_c<10;i_read_c++) {
    			byte m[] = new byte[128];
				int m_length = port.read(m, 500);
				
				for (int i=0;i<m_length;i++) pack[pack_length+i] = m[i];
				pack_length += m_length;
				
				result = resive(pack, pack_length, query_read_reg);
				if (result == 0) {
					return;
				}
    		}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return;
	}
	
	public int resive(byte[] data, int len, int query_read_reg) {
		byte pdu[];
		if (len<=3) {
			return 2;
		}
		Modbus mb = new Modbus();
		
		pdu = mb.RTU_unpack(data, len);
		if (pdu == null) {
			Log.w(TAG, "RTU decode fail");
			return 1;
		}
		for (int i=0;i<len;i++) Log.d(TAG, "data["+i+"]="+data[i]);
		mb.PDU_decode_answer(pdu);
		mb.reg = query_read_reg;
		
		switch (mb.cmd) {
		case Modbus.cmd_read_coil:
		case Modbus.cmd_read_discrete:
		case Modbus.cmd_read_holding:
		case Modbus.cmd_read_input:
			for (int i=0;i<mb.count;i++)  board.db[mb.reg+i] =  mb.data_array[i];
			break;
		case Modbus.cmd_write_coil:
		case Modbus.cmd_write_holding:
			break;
		}
		return 0;
	}

	

}
