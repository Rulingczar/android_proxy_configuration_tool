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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import android.database.Cursor;

public class Toggler extends BroadcastReceiver
{
	private Context context;
	private SharedPreferences preferences;
	
	private String mCarrier;
	

	private Boolean mUseMMSU2NL;
	private Boolean mUseU2NL;
	
	private String mHostname;
	private String mProxy;
	private String mPort;
	
	private String mMMS;
	private String mMMSPort;

	private String mInterface;
	private SqlHelper DB;
	private int sdkVersion;
	
	private MeidHelper meidHelper;
	
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
		}else if (action.equals(Configuration.ACTION_SETUP_CARRIER_APN)){
			try{
				if( isAPNActive() ){
					Log.d(Configuration.TAG,"Restoring APN Settings");
					restoreAPN();
				} else {
					Log.d(Configuration.TAG,"Backing Up and Installing APN Settings");
					backupAPN();
					enableAPN();
				}
				
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Pre-load default variables and settings
	 */
	private void loadPreferences()
	{

		mCarrier = preferences.getString(Configuration.PREF_CARRIER_SELECTION, Configuration.PREF_CARRIER_SELECTION_DEFAULT);	
		mUseMMSU2NL = preferences.getBoolean(Configuration.PREF_USE_MMS_U2NL, Configuration.PREF_USE_MMS_U2NL_DEFAULT);
		mUseU2NL = preferences.getBoolean(Configuration.PREF_USE_U2NL, Configuration.PREF_USE_U2NL_DEFAULT);
		
		try{
			DB = new SqlHelper(context); 
		}catch(Exception e){
			Log.e(Configuration.TAG,"Could Not Establish a Database Connection");
			e.printStackTrace();
		}
		
		Cursor carrierConfig =  DB.getCarrierConfig(mCarrier);

		if(carrierConfig.getCount() > 0){
			carrierConfig.moveToFirst();
			mProxy = carrierConfig.getString(carrierConfig.getColumnIndex("proxy"));
			mPort = carrierConfig.getString(carrierConfig.getColumnIndex("proxy_port"));
			mMMS = carrierConfig.getString(carrierConfig.getColumnIndex("mms_proxy"));
			mMMSPort = carrierConfig.getString(carrierConfig.getColumnIndex("mms_proxy_port"));
		} else {
			Log.e(Configuration.TAG,"Could Not Load Carrier Configuration");
		}
        
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
		carrierConfig.close();
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
					"iptables -t nat -A OUTPUT -o " + mInterface + " -p 6 ! -d "+mProxy+" -j REDIRECT --to-port 1025 \n"+
					"u2nl " + mProxy + " " + mPort + " 127.0.0.1 1025 >/dev/null 2>&1 &"
					);
			} else {
				Log.d("t",ShellInterface.getProcessOutput(
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
				));
			}
		}
		context.sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
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
				"busybox killall u2nl"+"\n"+
				"iptables -P INPUT ACCEPT"+"\n"+
				"iptables -P OUTPUT ACCEPT"+"\n"+
				"iptables -P FORWARD ACCEPT"+"\n"+
				"iptables -F"+"\n"+
				"iptables -t nat -F"+"\n"+
				"iptables -X"
			);
		}
		context.sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
	}
	
	/*
	 * Check the APN Status
	 */
	private boolean isAPNActive()
	{
		if(ShellInterface.isSuAvailable()){
			if(ShellInterface.getProcessOutput("ls /data/data/com.android.providers.telephony/databases/ | busybox grep telephony.db.bak").length() > 0 ){
				return true;
			}
		}
		return false;
	}
	/*
	 * Backup Existing APN
	 */
	private void backupAPN()
	{
		Log.d(Configuration.TAG, "Backing Up Current APN Settings");
		if(ShellInterface.isSuAvailable()){
			ShellInterface.runCommand(
				"busybox cp /data/data/com.android.providers.telephony/databases/telephony.db /data/data/com.android.providers.telephony/databases/telephony.db.bak \n"+
				"chown radio /data/data/com.android.providers.telephony/databases/telephony.db.bak \n"+
				"chmod 0755 /data/data/com.android.providers.telephony/databases/telephony.db.bak"
    		);
		}
		
	}
	/*
	 * Restore Backed Up APN
	 */
	private void restoreAPN()
	{
		if(ShellInterface.isSuAvailable()){
        	if(ShellInterface.getProcessOutput("ls /data/data/com.android.providers.telephony/databases/ | busybox grep telephony.db.bak").length() > 0)
        	{
        		Log.d(Configuration.TAG, "Restoring Backed Up APN Settings");
    			ShellInterface.runCommand(
    				"busybox mv /data/data/com.android.providers.telephony/databases/telephony.db.bak /data/data/com.android.providers.telephony/databases/telephony.db \n"+
    				"chown radio /data/data/com.android.providers.telephony/databases/telephony.db \n"+
    				"chmod 0755 /data/data/com.android.providers.telephony/databases/telephony.db"
    	    	);
        	} else {
        		Log.d(Configuration.TAG, "No Backup Found");
        	}
		}
		context.sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
	}
	
	private void enableAPN(){
		
		Cursor apnConfig = DB.getCarrierAndDeviceApnConfig(mCarrier, android.os.Build.DEVICE);
		if( apnConfig.getCount() == 0){
			apnConfig = DB.getCarrierAndDeviceApnConfig(mCarrier, "default");
		}
		
		if( apnConfig.getCount() > 0){
			apnConfig.moveToFirst();
			if(ShellInterface.isSuAvailable()){
				Log.d(Configuration.TAG,"Removing Existing APN Settings");
				ShellInterface.runCommand("sqlite3 /data/data/com.android.providers.telephony/databases/telephony.db "+ "\"DELETE FROM carriers;\"");
			}
			Log.d(Configuration.TAG,"Installing New APN Settings");
			do{
				StringBuilder cols = new StringBuilder();
				StringBuilder vals = new StringBuilder();
				
				int i = 1;
				for( String c : apnConfig.getColumnNames()){
					boolean override = false;
					String override_val = null;
					if( c.equals("apn_user") && apnConfig.getString(apnConfig.getColumnIndex("apn_user")).contains("replaceMDN")){
						String[] user = apnConfig.getString(apnConfig.getColumnIndex("apn_user")).split("@");
		            	override=true;
		            	override_val = getDeviceMDN()+"@"+user[1];
					} else if( c.equals("apn_password") && apnConfig.getString(apnConfig.getColumnIndex("apn_password")).equals("ReplaceSPC")){
		        		try{
		            		String deviceSerial = getDeviceSerial();
		            		if(deviceSerial == null){
		            			Toast.makeText(context, "Device Not CDMA", Toast.LENGTH_LONG);
		            			return;
		            		}
		            		meidHelper = new MeidHelper(deviceSerial);
		            	}catch(Exception e){
		            		Log.e(Configuration.TAG, "Error Getting Device Serial");
		            	}
		            	override=true;
		            	override_val = meidHelper.getMetroSpc();
					}	
					
					if( c.contains("apn_")){
						String[] c2 = c.split("apn_");
						cols.append(c2[1]);
					} else {
						cols.append(c);
					}

					vals.append("'"+(override ? override_val : apnConfig.getString(apnConfig.getColumnIndex(c)))+"'");
					
					if(i!=apnConfig.getColumnNames().length){
						cols.append(",");
						vals.append(",");
					}
					i++;
				}
				
				ShellInterface.runCommand(
					"sqlite3 /data/data/com.android.providers.telephony/databases/telephony.db " + "\"INSERT INTO carriers (_id,"+cols+")VALUES(NULL,"+vals+");\"\n" +
					"chown radio /data/data/com.android.providers.telephony/databases/telephony.db \n"+
    				"chmod 0755 /data/data/com.android.providers.telephony/databases/telephony.db"					
				);
			
			} while(apnConfig.moveToNext());
			
		} else {
			Toast.makeText(context, context.getString(R.string.db_apn_error), Toast.LENGTH_LONG).show();
			Log.e(Configuration.TAG,context.getString(R.string.db_apn_error));
		}
		apnConfig.close();
		context.sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
	}
	
    /*
     * Get the Device Serial 
     */
    private String getDeviceMDN(){
    	TelephonyManager telephone = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    	return telephone.getLine1Number();
    }
    /*
     * Get the Device MDN
     */
    private String getDeviceSerial(){
    	TelephonyManager telephone = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    	if( telephone.getNetworkType() == TelephonyManager.PHONE_TYPE_GSM){ // exit for GSM 
    		Log.e(Configuration.TAG, "Phone is not CDMA");
    		return null;
    	}
    	return telephone.getDeviceId();
    }
}
