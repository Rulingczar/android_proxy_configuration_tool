/*
 * Copyright (C) 2010 Daniel Velazco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.geekherd.proxyswitcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Proxy;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class Toggler extends BroadcastReceiver
{
	private Context context;
	private SharedPreferences preferences;
	
	private Boolean mUseCustomProxy;
	private Boolean mUseCustomMMS;
	private Boolean mUseMMSU2NL;
	private Boolean mUseU2NL;
	
	private String mCustomProxy;
	private String mCustomProxyPort;
	private String mCustomMMS;
	private String mCustomMMSPort;
	
	private String mHostname;
	private String mProxy;
	private String mPort;
	
	private String mMMS;
	private String mMMSPort;

	private String mInterface;
	
	private int sdkVersion;
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		loadPreferences();
		
		String action = intent.getAction();
		
		sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
			
		if (action.equals(Configuration.ACTION_ACTIVATE_ALL))
		{
			Log.d(Configuration.TAG, "ACTION_ACTIVATE_ALL action");
			
			try 
			{
				if( ! mUseU2NL ){ // only if we done use u2nl
					enableProxy();
				}
				enableU2NL();
			} catch (Exception e) {
				Log.e(Configuration.TAG, "", e);
				e.printStackTrace();
				Toast.makeText(context, context.getString(R.string.txt_root_error), Toast.LENGTH_LONG).show();
			}
		}
		else if (action.equals(Configuration.ACTION_DEACTIVATE_ALL))
		{
			Log.d(Configuration.TAG, "ACTION_DEACTIVATE_ALL action");
			
			try 
			{
				disableProxy();
				disableU2NL();
			} catch (Exception e) {
				Log.e(Configuration.TAG, "", e);
				e.printStackTrace();
				Toast.makeText(context, context.getString(R.string.txt_root_error), Toast.LENGTH_LONG).show();
			}
		}
		else if (action.equals(Configuration.ACTION_ACTIVATE_PROXY))
		{
			Log.d(Configuration.TAG, "ACTION_ACTIVATE_PROXY action");
			
			try 
			{
				enableProxy();
			} catch (Exception e) {
				Log.e(Configuration.TAG, "", e);
				e.printStackTrace();
				Toast.makeText(context, context.getString(R.string.txt_root_error), Toast.LENGTH_LONG).show();
			}
		}
		else if (action.equals(Configuration.ACTION_DEACTIVATE_PROXY))
		{
			Log.d(Configuration.TAG, "ACTION_DEACTIVATE_PROXY action");
			
			try 
			{
				disableProxy();
			} catch (Exception e) {
				Log.e(Configuration.TAG, "", e);
				e.printStackTrace();
				Toast.makeText(context, context.getString(R.string.txt_root_error), Toast.LENGTH_LONG).show();
			}
		}
		else if (action.equals(Configuration.ACTION_ACTIVATE_U2NL))
		{
			Log.d(Configuration.TAG, "ACTION_ACTIVATE_U2NL action");
			
			try 
			{
				enableU2NL();
			} catch (Exception e) {
				Log.e(Configuration.TAG, "", e);
				e.printStackTrace();
				Toast.makeText(context, context.getString(R.string.txt_root_error), Toast.LENGTH_LONG).show();
			}
		}
		else if (action.equals(Configuration.ACTION_DEACTIVATE_U2NL))
		{
			Log.d(Configuration.TAG, "ACTION_DEACTIVATE_U2NL action");
			
			try 
			{
				disableU2NL();
			} catch (Exception e) {
				Log.e(Configuration.TAG, "", e);
				e.printStackTrace();
				Toast.makeText(context, context.getString(R.string.txt_root_error), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	/*
	 * Pre-load default variables and settings
	 */
	private void loadPreferences()
	{
		mUseCustomProxy = preferences.
			getBoolean(Configuration.PREF_USE_CUSTOM_PROXY, Configuration.PREF_USE_CUSTOM_PROXY_DEFAULT);
		
		mCustomProxy = preferences.
			getString(Configuration.PREF_PROXY, Configuration.PREF_PROXY_DEFAULT);
		
		mCustomProxyPort = preferences.
			getString(Configuration.PREF_PROXY_PORT, Configuration.PREF_PROXY_PORT_DEFAULT);
		
		mUseCustomMMS = preferences.
			getBoolean(Configuration.PREF_USE_CUSTOM_MMS, Configuration.PREF_USE_CUSTOM_MMS_DEFAULT);
		
		mCustomMMS = preferences.
			getString(Configuration.PREF_MMS, Configuration.PREF_MMS_DEFAULT);
	
		mCustomMMSPort = preferences.
			getString(Configuration.PREF_MMS_PORT, Configuration.PREF_MMS_PORT_DEFAULT);
		
		mUseMMSU2NL = preferences.getBoolean(Configuration.PREF_USE_MMS_U2NL, 
				Configuration.PREF_USE_MMS_U2NL_DEFAULT);
		
		mUseU2NL = preferences.getBoolean(Configuration.PREF_USE_U2NL, 
				Configuration.PREF_USE_U2NL_DEFAULT);
		
		/* TODO: Add support for cricket */
		if (android.os.Build.DEVICE.equals("sholes"))
		{
			//The Motorola Droid's kernel was slightly changed between 
			//eclair and froyo and it sports a different network interface
			if (sdkVersion >= 8)
				mInterface = Configuration.DEFAULT_INTERFACE_MOTO_SHOLES_FROYO;
			else
				mInterface = Configuration.DEFAULT_INTERFACE_MOTO_SHOLES;
		}
		else if (android.os.Build.DEVICE.equals("inc"))
			mInterface = Configuration.DEFAULT_INTERFACE_HTC;
		else if (android.os.Build.DEVICE.equals("hero"))
			mInterface = Configuration.DEFAULT_INTERFACE_HTC;
		else if (android.os.Build.DEVICE.equals("supersonic"))
			mInterface = Configuration.DEFAULT_INTERFACE_HTC;
		else
			mInterface = Configuration.DEFAULT_INTERFACE;
		
		
		Log.d(Configuration.TAG, "Interface for " + android.os.Build.DEVICE + ": " + mInterface);
		
		if (mUseCustomProxy)
		{
			mHostname = mCustomProxy + ':' + mCustomProxyPort;
			mProxy = mCustomProxy;
			mPort = mCustomProxyPort;
		}
		else
		{
			mHostname = Configuration.DEFAULT_METRO_PROXY + ':' + Configuration.DEFAULT_METRO_PROXY_PORT;
			mProxy = Configuration.DEFAULT_METRO_PROXY;
			mPort = Configuration.DEFAULT_METRO_PROXY_PORT;
		}
		
		if (mUseCustomMMS)
		{
			mMMS = mCustomMMS;
			mMMSPort = mCustomMMSPort;
		}
		else
		{
			mMMS = Configuration.DEFAULT_METRO_MMS;
			mMMSPort = Configuration.DEFAULT_METRO_MMS_PORT;
		}
	}
	
	/*
	 * Put proxy settings on system secure settings. Will automatically activate proxy
	 */
	private void enableProxy()
	{
		
		if( !mUseU2NL && ShellInterface.isSuAvailable()){
			Log.d(Configuration.TAG, "Enabling proxy");
			ShellInterface.runCommand(
				"sqlite3 /data/data/com.android.providers.settings/databases/settings.db " + "\"INSERT OR IGNORE INTO secure (name, value) VALUES ('" + Settings.Secure.HTTP_PROXY + "', '" + mHostname + "');\"" + "\n"+
				"sqlite3 /data/data/com.android.providers.settings/databases/settings.db " + "\"UPDATE secure SET value = '" + mHostname + "' WHERE name = '" + Settings.Secure.HTTP_PROXY + "';\""
			);
			context.sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
		}
	}
	
	/*
	 * Remove proxy server from system. Will automatically disable proxy
	 */
	private void disableProxy()
	{
		Log.d(Configuration.TAG, "Disabling proxy");
		if( ShellInterface.isSuAvailable()){
			ShellInterface.runCommand(
				"sqlite3 /data/data/com.android.providers.settings/databases/settings.db " +"\"UPDATE secure SET value = '' WHERE name = '" + Settings.Secure.HTTP_PROXY + "';\""
			);
		}
		context.sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
	}
	
	/*
	 * Start up u2nl binary and tunnel all connections thru proxy.
	 */
	private void enableU2NL()
	{
		if (!mUseU2NL)
			return;
		
		Log.d(Configuration.TAG, "Enabling U2NL");
		
		if(ShellInterface.isSuAvailable()){
			if(android.os.Build.DEVICE.equals("desirec")){ // HTC ERIS
				ShellInterface.runCommand(
					"iptables -P INPUT ACCEPT \n"+
					"iptables -P OUTPUT ACCEPT \n"+
					"iptables -P FORWARD ACCEPT \n"+
					"iptables -F \n"+
					"iptables -t nat -F \n"+
					"iptables -X \n"+
					(mUseMMSU2NL ? ("iptables -t nat -A OUTPUT -o " + mInterface + " -p 6 -d " + mMMS + " --dport " + mMMSPort + " -j DNAT --to-destination " + mMMS + ":" + mMMSPort) : "")+"\n"+
					"iptables -t nat -A OUTPUT -o " + mInterface + " -p 6 ! -d 10.223.2.4 -j REDIRECT --to-port 1025 \n"+
					"u2nl " + mProxy + " " + mPort + " 127.0.0.1 1025 >/dev/null 2>&1 &"
					);
			} else {
				ShellInterface.runCommand(
					"iptables -P INPUT ACCEPT \n"+
					"iptables -P OUTPUT ACCEPT \n"+
					"iptables -P FORWARD ACCEPT \n"+
					"iptables -F \n"+
					"iptables -t nat -F \n"+
					"iptables -X \n"+
					(mUseMMSU2NL ? ("iptables -t nat -A OUTPUT -o " + mInterface + " -p 6 -d " + mMMS + " --dport " + mMMSPort + " -j DNAT --to-destination " + mMMS + ":" + mMMSPort) : "")+"\n"+
					"iptables -t nat -A OUTPUT -o " + mInterface + " -p 6 --dport 80 -j DNAT --to-destination " + mHostname + "\n"+
					"iptables -t nat -A OUTPUT -o " + mInterface + " -p 6 ! -d " + mProxy + " ! --dport " + mPort + " -j REDIRECT --to-port 1025\n"+
					"u2nl " + mProxy + " " + mPort + " 127.0.0.1 1025 >/dev/null 2>&1 &"
				);
			}
		}
	}
	
	/*
	 * Kill all instances of u2nl.
	 */
	private void disableU2NL()
	{
		if (!mUseU2NL)
			return;
		
		Log.d(Configuration.TAG, "Disabling U2NL");
		
		if(ShellInterface.isSuAvailable()){
			ShellInterface.runCommand(
				"killall u2nl"+"\n"+
				"iptables -P INPUT ACCEPT"+"\n"+
				"iptables -P OUTPUT ACCEPT"+"\n"+
				"iptables -P FORWARD ACCEPT"+"\n"+
				"iptables -F"+"\n"+
				"iptables -t nat -F"+"\n"+
				"iptables -X"
			);
		}
	}
	
}
