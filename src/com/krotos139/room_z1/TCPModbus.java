package com.krotos139.room_z1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;

import com.krotos139.room_z1.BoardZ1Room.FILE;

import android.util.Log;

public class TCPModbus implements Runnable {
	
	public static final int SERVERPORT = 8502;
	
	private ServerSocket serverSocket;
	
	public void run() {
		Socket socket = null;
		try {
			serverSocket = new ServerSocket(SERVERPORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (!Thread.currentThread().isInterrupted()) {

			try {

				socket = serverSocket.accept();
				Log.d("TCPModbus", "accept");

				CommunicationThread commThread = new CommunicationThread(socket);
				new Thread(commThread).start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}


class CommunicationThread implements Runnable {
	private final static String TAG = "CommunicationThread";

	private Socket clientSocket;

//	private BufferedReader input;
	
	private InputStreamReader input;
	private OutputStream output; 
	
	private byte pack[];
	private int pack_length;
	


	public CommunicationThread(Socket clientSocket) {

		this.clientSocket = clientSocket;

		try {

			this.input = new InputStreamReader(this.clientSocket.getInputStream());
			this.output = this.clientSocket.getOutputStream();
			pack = new byte[32];
			pack_length = 0;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		char buf[] = new char[128];
		int buf_length = 0;
		byte pdu[] = new byte[128];

		while (!Thread.currentThread().isInterrupted()) {

			try {

				Modbus mb = new Modbus(1);
				
				buf_length = input.read(buf);
				Log.d("TCPModbus", "resive "+buf_length+" bytes");
				if (buf_length == -1) {
					this.clientSocket.close();
					return;
				}
				if (buf_length <=0) continue;
				for (int i=0; i<buf_length ;i++) pack[i] = (byte) buf[i]; 
				pack_length=buf_length;
				
				Log.d("TCPModbus", "TPDU_unpack");
				Log.d("TCPModbus", "pack = "
						+ pack[0] + ","
						+ pack[1] + ","
						+ pack[2] + ","
						+ pack[3] + ","
						+ pack[4] + ","
						+ pack[5] + ","
						+ pack[6] + ","
						+ pack[7] + ","
						+ pack[8] + ","
						+ pack[9] + ","
						+ pack[10] + ","
						+ pack[11] + ","
						);
				pdu = mb.TPDU_unpack(pack,pack_length);
				
				if (pdu == null) {
					Log.w("TCPModbus", "TPDU decode fail");
					continue;
				}
				
				Log.d("TCPModbus", "PDU_decode_request");
				mb.PDU_decode_request(pdu);
				
				Log.d("TCPModbus", "cmd="+mb.cmd);
				switch (mb.cmd) {
				case Modbus.cmd_read_holding:
					Log.d("TCPModbus", "reg="+mb.reg);
					Log.d("TCPModbus", "count="+mb.count);
					mb.data_array = new int[mb.count];
					for (int i=0;i<mb.count;i++)  mb.data_array[i] = WebServer.z1room.read(mb.reg+i);
					Log.d("TCPModbus", "PDU_encode_answer");
					pdu = mb.PDU_encode_answer();
					Log.d("TCPModbus", "TPDU_pack");
					pack = mb.TPDU_pack(pdu);
					Log.d("TCPModbus", "write "+pack.length+" bytes");
					output.write(pack);
					break;
				case Modbus.cmd_write_holding:
					WebServer.z1room.write( mb.reg, mb.data);
					pdu = mb.PDU_encode_answer();
					pack = mb.TPDU_pack(pdu);
					output.write(pack);
					break;
				default:
					Log.e(TAG, "Unsuppotred command "+mb.cmd);
					continue;
				}
				
				

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}

