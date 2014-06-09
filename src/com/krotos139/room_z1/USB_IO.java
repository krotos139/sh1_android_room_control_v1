package com.krotos139.room_z1;

import java.util.HashMap;
import java.util.Iterator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;

public class USB_IO {
	private final static String TAG = "USB_IO";
	
    private static final int ARDUINO_USB_VENDOR_ID = 0x2341;
    private static final int ARDUINO_UNO_USB_PRODUCT_ID = 0x01;
    private static final int ARDUINO_MEGA_2560_USB_PRODUCT_ID = 0x10;
    private static final int ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID = 0x42;
    private static final int ARDUINO_UNO_R3_USB_PRODUCT_ID = 0x43;
    private static final int ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID = 0x44;
    private static final int ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID = 0x3F;
    private static final int ARDUINO_LEONARDO_ADK_USB_PRODUCT_ID = 0x8036;
    
    private volatile UsbDevice mUsbDevice = null;
    private volatile UsbDeviceConnection mUsbConnection = null;
    private volatile UsbManager mUsbManager = null;
    private volatile UsbEndpoint mInUsbEndpoint = null;
    private volatile UsbEndpoint mOutUsbEndpoint = null;
	
	private Context context;
	protected DataResiver sender;
	
	USB_IO (DataResiver sender, Context c) {
		this.context = c;
		this.sender = sender; 
	}

	void findDevice() {
		mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> usbDeviceList = mUsbManager.getDeviceList();
        Log.d(TAG, "Count devices: " + usbDeviceList.size());
        
     
        Iterator<UsbDevice> deviceIterator = usbDeviceList.values().iterator();
        if (deviceIterator.hasNext()) {
            UsbDevice tempUsbDevice = deviceIterator.next();
            
            // Print device information. If you think your device should be able
            // to communicate with this app, add it to accepted products below.
            Log.d(TAG, "VendorId: " + tempUsbDevice.getVendorId());
            Log.d(TAG, "ProductId: " + tempUsbDevice.getProductId());
            Log.d(TAG, "DeviceName: " + tempUsbDevice.getDeviceName());
            Log.d(TAG, "DeviceId: " + tempUsbDevice.getDeviceId());
            Log.d(TAG, "DeviceClass: " + tempUsbDevice.getDeviceClass());
            Log.d(TAG, "DeviceSubclass: " + tempUsbDevice.getDeviceSubclass());
            Log.d(TAG, "InterfaceCount: " + tempUsbDevice.getInterfaceCount());
            Log.d(TAG, "DeviceProtocol: " + tempUsbDevice.getDeviceProtocol());

            if (tempUsbDevice.getVendorId() == ARDUINO_USB_VENDOR_ID) {
                Log.i(TAG, "Arduino device found!");

                switch (tempUsbDevice.getProductId()) {
                case ARDUINO_UNO_USB_PRODUCT_ID:
                    Toast.makeText(context, "Arduino Uno found", Toast.LENGTH_SHORT).show();
                    mUsbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_USB_PRODUCT_ID:
                    Toast.makeText(context, "Arduino Mega 2560 found", Toast.LENGTH_SHORT).show();
                    mUsbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_R3_USB_PRODUCT_ID:
                    Toast.makeText(context, "Arduino Mega 2560 R3 found", Toast.LENGTH_SHORT).show();
                    mUsbDevice = tempUsbDevice;
                    break;
                case ARDUINO_UNO_R3_USB_PRODUCT_ID:
                    Toast.makeText(context, "Arduino Uno R3 found", Toast.LENGTH_SHORT).show();
                    mUsbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_ADK_R3_USB_PRODUCT_ID:
                    Toast.makeText(context, "Arduino Mega 2560 ADK R3 found", Toast.LENGTH_SHORT).show();
                    mUsbDevice = tempUsbDevice;
                    break;
                case ARDUINO_MEGA_2560_ADK_USB_PRODUCT_ID:
                    Toast.makeText(context, "Arduino Mega 2560 ADK found", Toast.LENGTH_SHORT).show();
                    mUsbDevice = tempUsbDevice;
                    break;
                case ARDUINO_LEONARDO_ADK_USB_PRODUCT_ID:
                    Toast.makeText(context, "Arduino Leonardo ADK found", Toast.LENGTH_SHORT).show();
                    mUsbDevice = tempUsbDevice;
                    break;
                    
                }
            }
        }

        if (mUsbDevice == null) {
            Log.i(TAG, "No device found!");
            Toast.makeText(context, "Device not found", Toast.LENGTH_LONG).show();
        } else {
            Log.i(TAG, "Device found!");
            
            initDevice(mUsbDevice, mUsbManager);
            
         //   Intent startIntent = new Intent(context, ArduinoCommunicatorService.class);
         //   PendingIntent pendingIntent = PendingIntent.getService(context, 0, startIntent, 0);
         //   usbManager.requestPermission(usbDevice, pendingIntent);
        }
    }
	
	void initDevice(UsbDevice usbDevice, UsbManager usbManager) {
		
		
		mUsbConnection = usbManager.openDevice(usbDevice);
		if (mUsbConnection == null) {
            Log.e(TAG, "Opening USB device failed!");
            Toast.makeText(context, "Opening USB device failed!", Toast.LENGTH_LONG).show();
            return;
        }
		
		UsbInterface usbInterface = mUsbDevice.getInterface(1);
        if (!mUsbConnection.claimInterface(usbInterface, true)) {
            Log.e(TAG, "Claiming interface failed!");
            Toast.makeText(context, "Claiming interface failed!", Toast.LENGTH_LONG).show();
            mUsbConnection.close();
            return;
        }

        // Arduino USB serial converter setup
        // Set control line state
        mUsbConnection.controlTransfer(0x21, 0x22, 0, 0, null, 0, 0);
        // Set line encoding.
        mUsbConnection.controlTransfer(0x21, 0x20, 0, 0, getLineEncoding(9600), 7, 0);

        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            if (usbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                    mInUsbEndpoint = usbInterface.getEndpoint(i);
                } else if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_OUT) {
                    mOutUsbEndpoint = usbInterface.getEndpoint(i);
                }
            }
        }
        
        if (mInUsbEndpoint == null) {
            Log.e(TAG, "No in endpoint found!");
            Toast.makeText(context, "No in endpoint found!", Toast.LENGTH_LONG).show();
            mUsbConnection.close();
            return;
        }

        if (mOutUsbEndpoint == null) {
            Log.e(TAG, "No out endpoint found!");
            Toast.makeText(context, "No out endpoint found!", Toast.LENGTH_LONG).show();
            mUsbConnection.close();
            return;
        }
		
		//startSenderThread();
		startReceiverThread();
	}
	
	public boolean Transmit(int data) { 
		byte[] buf_data = new byte[4];
		
		buf_data[0] = (byte) (data & 0xFF);
		buf_data[1] = (byte) ((data >> 8) & 0xFF);
		buf_data[2] = (byte) ((data >> 16) & 0xFF);
		buf_data[3] = (byte) ((data >> 24) & 0xFF);
		
		return Transmit(buf_data);
	}
	
	public boolean Transmit(byte[] data) { 
		if (mUsbDevice == null) return false;
		
		final int len = mUsbConnection.bulkTransfer(mOutUsbEndpoint, data, data.length, 0);
    	Log.d(TAG, len + " of " + data.length + " sent.");
    	if (len != data.length) {
    		return false;
    	}
		
		return true;
	}
	
    private void startReceiverThread() {
    	ReceiverThread mReceiverThread;
        mReceiverThread = new ReceiverThread("arduino_receiver");
        mReceiverThread.start();
    }
    
    private class ReceiverThread extends Thread {
    	
    	public ReceiverThread(String string) {
            super(string);
        }
    	
    	public void run() {
            byte[] inBuffer = new byte[4];
            while(mUsbDevice != null ) {
                Log.d(TAG, "calling bulkTransfer() in");
                final int len = mUsbConnection.bulkTransfer(mInUsbEndpoint, inBuffer, inBuffer.length, 0);
                sender.resive(inBuffer, len);
            }

            Log.d(TAG, "receiver thread stopped.");
        }
    }
    

    
	/*
    private void startSenderThread() {
    	SenderThread mSenderThread;
        mSenderThread = new SenderThread("arduino_sender");
        mSenderThread.start();
    }

    private class SenderThread extends Thread {
        public Handler mHandler;

        public SenderThread(String string) {
            super(string);
        }

        public void run() {
        	byte[] dataToSend = new byte[4];

        	dataToSend[0] = 1;
        	dataToSend[1] = 2;
        	dataToSend[2] = 4;
        	dataToSend[3] = 8;
        	
            while(true) {
            	final int len = mUsbConnection.bulkTransfer(mOutUsbEndpoint, dataToSend, dataToSend.length, 0);
            	Log.d(TAG, len + " of " + dataToSend.length + " sent.");
            	try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }

            //Log.i(TAG, "sender thread stopped");
        }
    }
    */
    
    private byte[] getLineEncoding(int baudRate) {
        final byte[] lineEncodingRequest = { (byte) 0x80, 0x25, 0x00, 0x00, 0x00, 0x00, 0x08 };
        switch (baudRate) {
        case 14400:
            lineEncodingRequest[0] = 0x40;
            lineEncodingRequest[1] = 0x38;
            break;

        case 19200:
            lineEncodingRequest[0] = 0x00;
            lineEncodingRequest[1] = 0x4B;
            break;
        }

        return lineEncodingRequest;
    }
	
}
