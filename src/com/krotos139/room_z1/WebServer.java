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
	private Thread TCPModbusThread;
	static BoardZ1Room z1room;
	
    public WebServer(Context context)
    {
        super(8080);
        this.c = context;
        this.res = context.getResources();
        z1room = new BoardZ1Room(c);
        
        this.TCPModbusThread = new Thread(new TCPModbus());
        this.TCPModbusThread.start();
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
