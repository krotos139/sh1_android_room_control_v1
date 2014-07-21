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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.krotos139.room_z1.BoardZ1Room.FILE;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class WebServer extends NanoHTTPD {
	Resources res;
	Context c;
	BoardZ1Room z1room;
	
    public WebServer(Context context, BoardZ1Room board)
    {
        super(8080);
        this.c = context;
        this.res = context.getResources();
        z1room = board;
    }

    @Override public Response serve(IHTTPSession session) {
    	String msg;
    	
        Method method = session.getMethod();
        String uri = session.getUri();
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }
        System.out.println(method + " '" + uri + "' ");
        
        Map<String, String> parms = session.getParms();
        
        if (uri.equals("/")) {
        	try {
            	InputStream is = res.openRawResource(R.raw.index);
            	msg = convertStreamToString(is);
				is.close();
			} catch (IOException e) {
				msg = "500 Internal error";
			}
        	return new NanoHTTPD.Response(msg);
        } else if (uri.equals("/style.css")) {
        	try {
            	InputStream is = res.openRawResource(R.raw.style);
            	msg = convertStreamToString(is);
				is.close();
			} catch (IOException e) {
				msg = "500 Internal error";
			}
        	return new NanoHTTPD.Response(Status.OK, "text/css",msg);
        } else if (uri.equals("/control.xml")) {
        	try {
            	InputStream is = res.openRawResource(R.raw.control_template);
            	msg = convertStreamToString(is);
				is.close();
				String cmd = parms.get("cmd");
				if (cmd != null) {
					int v = Integer.parseInt(parms.get("v"));
					int id = Integer.parseInt(parms.get("id"));
					switch (id) {
					case 1:
						z1room.write(FILE.RELAY1, v);
						break;
					case 2:
						z1room.write(FILE.RELAY2, v);
						break;
					case 3:
						z1room.write(FILE.RELAY3, v);
						break;
					case 4:
						z1room.write(FILE.RELAY4, v);
						break;
					case 5:
						z1room.write(FILE.RELAY5, v);
						break;
					case 6:
						z1room.write(FILE.RELAY6, v);
						break;
					case 7:
						z1room.write(FILE.RELAY7, v);
						break;
					case 8:
						z1room.write(FILE.RELAY8, v);
						break;
					default:
						break;
					}
				}
            	msg = msg.replace("%RELAY1%", String.valueOf(z1room.read(FILE.RELAY1)) );
            	msg = msg.replace("%RELAY2%", String.valueOf(z1room.read(FILE.RELAY2)) );
            	msg = msg.replace("%RELAY3%", String.valueOf(z1room.read(FILE.RELAY3)) );
            	msg = msg.replace("%RELAY4%", String.valueOf(z1room.read(FILE.RELAY4)) );
            	msg = msg.replace("%RELAY5%", String.valueOf(z1room.read(FILE.RELAY5)) );
            	msg = msg.replace("%RELAY6%", String.valueOf(z1room.read(FILE.RELAY6)) );
            	msg = msg.replace("%RELAY7%", String.valueOf(z1room.read(FILE.RELAY7)) );
            	msg = msg.replace("%RELAY8%", String.valueOf(z1room.read(FILE.RELAY8)) );
			} catch (IOException e) {
				msg = "500 Internal error";
			}
        	return new NanoHTTPD.Response(Status.OK, "text/xml",msg);
        } else if (uri.equals("/sensors.xml")) {
        	try {
            	InputStream is = res.openRawResource(R.raw.sensors_template);
            	msg = convertStreamToString(is);
				is.close();
            	msg = msg.replace("%TEMPTERATURE_CELSIUS%", String.valueOf(z1room.read(FILE.TEMPERATURE)) );
            	msg = msg.replace("%HUMIDITY_PERC%", String.valueOf(z1room.read(FILE.HUMIDITY)) );
            	msg = msg.replace("%CURRENT_AMPER%", String.valueOf(z1room.read(FILE.CURRENT)) );
            	msg = msg.replace("%MQ2_PERC%", String.valueOf(z1room.read(FILE.MQ2)) );
            	msg = msg.replace("%PIR_PERC%", String.valueOf(z1room.read(FILE.PIR)) );
			} catch (IOException e) {
				msg = "500 Internal error";
			}
        	return new NanoHTTPD.Response(Status.OK, "text/xml",msg);
        } else if (uri.equals("/control.xsl")) {
        	try {
            	InputStream is = res.openRawResource(R.raw.control);
            	msg = convertStreamToString(is);
				is.close();
			} catch (IOException e) {
				msg = "500 Internal error";
			}
        	return new NanoHTTPD.Response(Status.OK, "text/xsl",msg);
        } else if (uri.equals("/sensors.xsl")) {
        	try {
            	InputStream is = res.openRawResource(R.raw.sensors);
            	msg = convertStreamToString(is);
				is.close();
			} catch (IOException e) {
				msg = "500 Internal error";
			}
        	return new NanoHTTPD.Response(Status.OK, "text/xsl",msg);
        } else {
        	return new NanoHTTPD.Response(Status.NOT_FOUND, MIME_HTML,"404 Error");
        }

    }

	private String convertStreamToString(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = is.read();
		while (i != -1) {
			baos.write(i);
			i = is.read();
		}
		return baos.toString();
	}
}
