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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;

import com.krotos139.room_z1.BoardZ1Room.register;

import android.content.Context;
import android.util.Log;

public class TCPModbusSlave implements Runnable {
	private final static String TAG = "TCPModbusSlave";
	
	public static final int SERVERPORT = 8502;
	
	public BoardZ1Room board;
	private ServerSocket serverSocket;
	
	
	public TCPModbusSlave(Context c, BoardZ1Room board) {
		this.board = board; 
	}
	
	public void run() {
		Socket socket = null;
		try {
			serverSocket = new ServerSocket(SERVERPORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "Server start on port "+SERVERPORT);
		while (!Thread.currentThread().isInterrupted()) {

			try {

				socket = serverSocket.accept();
				Log.d(TAG, "accept");

				TCPModbusSlaveCommunicationThread commThread = new TCPModbusSlaveCommunicationThread(this, socket);
				new Thread(commThread).start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}


class TCPModbusSlaveCommunicationThread implements Runnable {
	private final static String TAG = "CommunicationThread";

	TCPModbusSlave pclass;
	
	private Socket clientSocket;

//	private BufferedReader input;
	
	private InputStreamReader input;
	private OutputStream output; 
	
	private byte pack[];
	private int pack_length;
	


	public TCPModbusSlaveCommunicationThread(TCPModbusSlave parrent, Socket clientSocket) {

		this.pclass = parrent;
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

				Modbus mb = new Modbus();
				
				buf_length = input.read(buf);
				
				if (buf_length == -1) {
					this.clientSocket.close();
					return;
				}
				Log.d("TCPModbus", "resive "+buf_length+" bytes");
				
				for (int i=0; i<buf_length ;i++) pack[i] = (byte) buf[i]; 
				pack_length=buf_length;

				pdu = mb.TPDU_unpack(pack,pack_length);
				
				if (pdu == null) {
					Log.w("TCPModbus", "TPDU decode fail");
					continue;
				}
				
				mb.PDU_decode_request(pdu);
				
				Log.d("TCPModbus", "cmd="+mb.cmd);
				switch (mb.cmd) {
				case Modbus.cmd_read_holding:
					mb.data_array = new int[mb.count];
					for (int i=0;i<mb.count;i++)  mb.data_array[i] = pclass.board.read(mb.reg+i);
					pdu = mb.PDU_encode_answer();
					pack = mb.TPDU_pack(pdu);
					output.write(pack);
					break;
				case Modbus.cmd_write_holding:
					pclass.board.write( mb.reg, mb.data);
					pdu = mb.PDU_encode_answer();
					pack = mb.TPDU_pack(pdu);
					output.write(pack);
					break;
				case Modbus.cmd_write_holdings:
					for (int i=0;i<mb.count;i++) pclass.board.write(mb.reg+i, mb.data_array[i]);
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

