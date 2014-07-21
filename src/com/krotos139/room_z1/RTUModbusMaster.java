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
import com.krotos139.room_z1.BoardZ1Room.FILE;

public class RTUModbusMaster implements Runnable, DataResiver, BoardListener {
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
	private Iterator<FILE> db_l_update_iterator;
	private int query_read_reg;
	
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
        	Log.d(TAG, "read_all");
        	read_all();
        	Log.d(TAG, "processing");
        	processing();

            timerHandler.postDelayed(this, 1000);
        }
    };
    

	
	public RTUModbusMaster(Context c, BoardZ1Room board) {
		this.board = board; 
		this.db_l_update_iterator = this.board.db_l_update.iterator();
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
		  // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
		  return;
		}
		List<UsbSerialPort> ports = driver.getPorts();
		Log.d(TAG, "Port0:"+ports.get(0).toString());
		
		// Read some data! Most have just one port (port 0).
		port = ports.get(0);
		
		try {
		  port.open(connection);
//		  port.setBaudRate(115200);
		  port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
		  port.setDTR(true);
//		  port.setRTS(false);
		  byte pack[] = new byte[128];
		  int pack_length = port.read(pack, 1000);
		  Log.d(TAG,"read ( "+pack_length+" bytes) data:"+pack);
		} catch (IOException e) {
		  // Deal with error.
			e.printStackTrace();
		  return;
		}
        
        
        Log.d(TAG, "Run service");
        timerHandler.postDelayed(timerRunnable, 0);
	}
	
	private void processing() {
    	byte pack[] = new byte[128];
    	byte pdu[];
    	Modbus mb = new Modbus();
    	try {
			int pack_length = port.read(pack, 500);
			
			Log.d(TAG, "Read " + pack_length + " bytes.");
			for (int i=0;i<pack_length;i++)	Log.d(TAG, "PACK["+i+"]="+pack[i]);
			
			resive(pack, pack_length);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	timerHandler.postDelayed(this, 1000);
	}
	
	public void read(int f) {
		byte pdu[];
		byte rtu[]; 
		Modbus mb = new Modbus();
		
		Log.d(TAG, "write f="+f);		
		
		mb.slave_addr = 1;
		mb.cmd = mb.cmd_read_holding;
		mb.reg = f;
		mb.count = 1;
		
		query_read_reg = mb.reg;
		
		pdu = mb.PDU_encode_request();
		rtu = mb.RTU_pack(pdu);
//		usb.Transmit(rtu); TODO
	}
	public void read_all() {
		byte pdu[];
		byte rtu[]; 
		Modbus mb = new Modbus();
		
		if (!db_l_update_iterator.hasNext()) db_l_update_iterator = this.board.db_l_update.iterator();
		FILE f = db_l_update_iterator.next();
		
		mb.slave_addr = 1;
		mb.cmd = mb.cmd_read_holding;
		mb.reg = f.ordinal();
		mb.count = 1;
		
		query_read_reg = mb.reg;
		
		pdu = mb.PDU_encode_request();
		rtu = mb.RTU_pack(pdu);
		for (int i=0;i<rtu.length;i++)	Log.d(TAG, "PDU["+i+"]="+rtu[i]);
		try {
			port.write(rtu, 100);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		usb.Transmit(rtu); TODO
	}
	public void write(int f, int value) {
		byte pdu[];
		byte rtu[]; 
		Modbus mb = new Modbus();
		
		Log.d(TAG, "write f="+f+" v="+value);		
		
		mb.slave_addr = 1;
		mb.cmd = mb.cmd_write_holding;
		mb.reg = f;
		mb.count = 1;
		mb.data = value;
		
		pdu = mb.PDU_encode_request();
		rtu = mb.RTU_pack(pdu);
		
//		usb.Transmit(rtu); TODO
	}
	
	@Override
	public void resive(byte[] data, int len) {
		byte pdu[];
		Log.d(TAG, "Resive data, len="+len);
		if (len<=3) {
			Log.d(TAG, "Data does not resive");
			return;
		}
		Modbus mb = new Modbus();
		
		pdu = mb.RTU_unpack(data, len);
		if (pdu == null) {
			Log.w(TAG, "RTU decode fail");
			return;
		}
		mb.PDU_decode_answer(pdu);
		mb.reg = query_read_reg;
		
		switch (mb.cmd) {
		case Modbus.cmd_read_coil:
		case Modbus.cmd_read_discrete:
		case Modbus.cmd_read_holding:
		case Modbus.cmd_read_input:
			for (int i=0;i<mb.count;i++)  board.db[mb.reg+i] =  mb.data_array[i];
			for (int i=0;i<mb.count;i++)  Log.d(TAG,"reg =  " + (mb.reg+i) + "data = " + mb.data_array[i]);
			break;
		case Modbus.cmd_write_coil:
		case Modbus.cmd_write_holding:
			break;
		}
		return;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	

}
