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

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {

    private CharSequence mTitle;
    
    static BoardZ1Room z1room;
    
    private WebServer server;
    private Thread TCPModbusThread;
    private RTUModbusMaster RTUModbus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        this.z1room = new BoardZ1Room(getApplicationContext());
        
        server = new WebServer(getApplicationContext(), this.z1room);
        try {
            server.start();
        } catch(IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");

        
        this.TCPModbusThread = new Thread(new TCPModbusSlave(getApplicationContext(), this.z1room));
        this.TCPModbusThread.start();
        
        this.RTUModbus = new RTUModbusMaster(getApplicationContext(), this.z1room);
        

        mTitle = getTitle();

      FragmentManager fragmentManager = getFragmentManager();
      fragmentManager.beginTransaction()
              .replace(R.id.container, PlaceholderFragment.newInstance(1))
              .commit();
    }

//    @Override
//    public void onNavigationDrawerItemSelected(int position) {
//        // update the main content by replacing fragments
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
//                .commit();
//    }

//    public void onSectionAttached(int number) {
//    	WebView myWebView = (WebView) this.findViewById(R.id.webView1);
//    	if (myWebView == null) return;
//        switch (number) {
//            case 1:
//                mTitle = getString(R.string.title_control);
//                myWebView.loadUrl("http://localhost:8080/control.xml");
//                break;
//            case 2:
//                mTitle = getString(R.string.title_sensors);
//                myWebView.loadUrl("http://localhost:8080/sensors.xml");
//                break;
//            case 3:
//                mTitle = getString(R.string.title_weather);
//                myWebView.loadUrl("http://192.168.0.20/");
//                break;
//        }
//    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
