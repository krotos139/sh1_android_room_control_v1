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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.Log;


public class BoardZ1Room {
	private final static String TAG = "BoardZ1Room";
	
	public static enum FILE {
		DEVICE_ID,	// 0
		STATE,		// 1
		
		RELAY1,		// 2
		RELAY2,		// 3
		RELAY3,		// 4 
		RELAY4,		// 5
		RELAY5,		// 6
		RELAY6,		// 7
		RELAY7,		// 8
		RELAY8,		// 9
		
		LED_R,		// 10
		LED_G,		// 11
		LED_B,		// 12
		
		TEMPERATURE,		// 13
		TEMPERATURE_FRAC,	// 14
		HUMIDITY,			// 15
		HUMIDITY_FRAC,		// 16
		PIR,				// 17
		MQ2,				// 18
		CURRENT,			// 19

		IR_MODE,			// 20
		IR_DH,				// 21
		IR_DL,				// 22
		IR_SIZE				// 23
		
	}
	
	List<BoardListener> list_write_callbacks = new ArrayList<BoardListener>();
	
	/**
	 * Value holdings registers
	 */
	public int db [];
	
	List<FILE> db_l_update;
	
	public BoardZ1Room(Context c) {
		Log.d(TAG, "BoardZ1Room db.len="+FILE.values().length+"");
		// init db
		db = new int[FILE.values().length];
		for (int i=0;i<db.length;i++) db[i] = 0;
		// init db_l_update
		db_l_update = new ArrayList<BoardZ1Room.FILE>();
		db_l_update.add(FILE.TEMPERATURE);
		db_l_update.add(FILE.TEMPERATURE_FRAC);
		db_l_update.add(FILE.HUMIDITY);
		db_l_update.add(FILE.HUMIDITY_FRAC);
		db_l_update.add(FILE.PIR);
		db_l_update.add(FILE.MQ2);
		db_l_update.add(FILE.CURRENT);
		
		
	}

	public int read(FILE f) {
		return read(f.ordinal());
	}
	public int read(int f) {
		Log.d(TAG, "read f="+f+"");
		return db[f];
	}

	public void write(FILE f, int value) {
		write(f.ordinal(), value);			
	}
	public void write(int f, int value) {
		Log.d(TAG, "write f="+f+" v="+value);		
		
		db[f] = value;
		
		Iterator<BoardListener> e = list_write_callbacks.iterator();
		while (e.hasNext()) {
			e.next().write(f, value);
		}
	}
	



	
}
