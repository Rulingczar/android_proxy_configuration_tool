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


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Proxy;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

public class Configuration extends PreferenceActivity 
{
	/*
	 * 
	 * Main screen.
	 * We use this activity to allow the user to manually activate/deactivate proxy,
	 * as well as change default settings for the proxy.
	 * 
	 * Can be improved.
	 * 
	 */
	
	/** Default Settings **/
	public final static String TAG = "ProxySwitcher";
	/** Default Settings **/

	/** Network Interfaces **/
	public final static String DEFAULT_INTERFACE = "rmnet0"; //DEFAULT INTERFACE USED BY IPTABLES
	public final static String DEFAULT_INTERFACE_MOTO_SHOLES = "ppp0"; //INTERFACE USED ON MOTOROLA DROID 1 PRE-FROYO
	public final static String DEFAULT_INTERFACE_MOTO_SHOLES_FROYO = "ppp0"; //INTERFACE USED ON MOTOROLA DROID 1 ON FROYO /*TODO: THIS IS NOT RIGHT! */
	public final static String DEFAULT_INTERFACE_HTC = "rmnet0"; //INTERFACE USED ON MOST HTC DEVICES
	/** Network Interfaces **/
	
	public static String ACTION_INSTALL_BINARIES = "InstallBinaries";
	public static String ACTION_ACTIVATE_ALL = "ActivateAll";
	public static String ACTION_ACTIVATE_PROXY = "ActivateProxy";
	public static String ACTION_ACTIVATE_U2NL = "ActivateU2NL";
	public static String ACTION_DEACTIVATE_ALL = "DectivateAll";
	public static String ACTION_DEACTIVATE_PROXY = "DectivateProxy";
	public static String ACTION_DEACTIVATE_U2NL = "DeactivateU2NL";
	public static String ACTION_SETUP_CARRIER_APN = "SetupApn";
	
	/** Preference Screen Constants **/
	public final static String PROXY_STATUS = "proxy_status";
	
	public final static String INSTALL_BINARIES = "install_binaries";
	
	public final static String PREF_CREDITS = "prefs_credits";
	
	public final static String TOGGLE_ACTIVATE = "toggle_activate";
	public final static String TOGGLE_DEACTIVATE = "toggle_deactivate";
	public final static String TOGGLE_CARRIER_APN = "toggle_carrier_apn";
	/** Preference Screen Constants **/
	
	/** Preferences Constants **/
	public final static String CARRIER_METROPCS = "metropcs";
	public final static String CARRIER_CRICKET = "cricket";
	public final static String CARRIER_CUSTOM = "custom";
	
	public final static String PREF_CARRIER_SELECTION = "prefs_carrier_selection";
	public final static String PREF_CARRIER_SELECTION_DEFAULT = CARRIER_METROPCS;
	
	public final static String PREF_AUTO_SWITCH_ENABLED = "prefs_autoswitch_enabled";
	public final static Boolean PREF_AUTO_SWITCH_ENABLED_DEFAULT = true;
	
	public final static String PREF_USE_U2NL = "prefs_use_u2nl";
	public final static Boolean PREF_USE_U2NL_DEFAULT = true;
	
	public final static String PREF_PROXY = "prefs_custom_proxy";
	
	public final static String PREF_PROXY_PORT = "prefs_custom_proxy_port";
	
	public final static String PREF_USE_MMS_U2NL = "prefs_use_mms_u2nl";
	public final static Boolean PREF_USE_MMS_U2NL_DEFAULT = true;
	
	public final static String PREF_MMS = "prefs_custom_mms";
	
	public final static String PREF_MMS_PORT = "prefs_custom_mms_port";
	/** Preferences Constants **/
	
	private PreferenceScreen 
		proxy_status,
		toggle_activate,
		toggle_deactivate,
		toggle_carrier_apn,
		prefs_custom_settings,
		install_binaries;
	
	private ListPreference prefs_carrier_selection;
	
	private CheckBoxPreference prefs_autoswitch_enabled;
	private CheckBoxPreference prefs_use_u2nl;

	private EditTextPreference prefs_custom_proxy;
	private EditTextPreference prefs_custom_proxy_port;
	private EditTextPreference prefs_custom_mms;
	private EditTextPreference prefs_custom_mms_port;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	super.onCreate(savedInstanceState);
    	setProgressBarIndeterminateVisibility(true);
        addPreferencesFromResource(R.xml.configuration);
        
        proxy_status = (PreferenceScreen)findPreference("proxy_status");
        
        toggle_activate = (PreferenceScreen)findPreference("toggle_activate");
        toggle_deactivate = (PreferenceScreen)findPreference("toggle_deactivate");
        toggle_carrier_apn = (PreferenceScreen)findPreference("toggle_carrier_apn");
        install_binaries = (PreferenceScreen)findPreference("install_binaries");
        prefs_autoswitch_enabled = (CheckBoxPreference)findPreference("prefs_autoswitch_enabled");
        
        prefs_carrier_selection = (ListPreference)findPreference("prefs_carrier_selection");
        
        prefs_use_u2nl = (CheckBoxPreference)findPreference("prefs_use_u2nl");
        
        prefs_custom_settings = (PreferenceScreen)findPreference("prefs_custom_settings");
        

        prefs_custom_proxy = (EditTextPreference)findPreference("prefs_custom_proxy");
        prefs_custom_proxy_port = (EditTextPreference)findPreference("prefs_custom_proxy_port");

        prefs_custom_proxy.setOnPreferenceChangeListener(customProxyEditTextListener);
        prefs_custom_proxy_port.setOnPreferenceChangeListener(customProxyPortEditTextListener);
        
       
        prefs_custom_mms = (EditTextPreference)findPreference("prefs_custom_mms");
        prefs_custom_mms_port = (EditTextPreference)findPreference("prefs_custom_mms_port");
        
        prefs_custom_mms.setOnPreferenceChangeListener(customMMSEditTextListener);
        prefs_custom_mms_port.setOnPreferenceChangeListener(customMMSPortEditTextListener);
        
        prefs_carrier_selection.setOnPreferenceChangeListener(setCarrierSelection);
    }
    
    private BroadcastReceiver mProxyChangeActionReceiver = new BroadcastReceiver()
	{
    	@Override
		public void onReceive(Context context, Intent intent) 
		{
    		new checkStatus().execute();
		}
	};
    
    /*
     * As soon as the activity resumes, check for the state of the proxy/u2nl.
     */
    @Override
	protected void onResume()
	{
		super.onResume();
		
		updateCustomProxySummary(prefs_carrier_selection.getValue().toString().equals(CARRIER_CUSTOM), null, null);
		updateCustomMMSSummary(prefs_carrier_selection.getValue().toString().equals(CARRIER_CUSTOM), null, null);
		updateCarrierSelectionSummary(prefs_carrier_selection.getValue());
		
		new checkStatus().execute();
		
		this.registerReceiver(this.mProxyChangeActionReceiver, new IntentFilter(Proxy.PROXY_CHANGE_ACTION));
	}
    
    /*
     * Here we test the custom proxy/mms servers to see if they are valid.
     */
    @Override
	protected void onStop()
	{
		super.onStop();
		
		this.unregisterReceiver(this.mProxyChangeActionReceiver);
		
		if (prefs_carrier_selection.getValue().toString().equals(CARRIER_CUSTOM))
		{
			if (!testCustomProxy(null, null))
			{
				Log.d(TAG, "Invalid ip address and/or port number. Cannot use custom proxy.");
				Toast.makeText(this, getString(R.string.prefs_use_custom_proxy_error), Toast.LENGTH_LONG).show();
			}
		}
		
		if (prefs_carrier_selection.getValue().toString().equals(CARRIER_CUSTOM))
		{
			if (!testCustomMMSServer(null, null))
			{
				Log.d(TAG, "Invalid ip address and/or port number. Cannot use custom MMS server.");
				Toast.makeText(this, getString(R.string.prefs_use_custom_mms_error), Toast.LENGTH_LONG).show();
			}
		}
	}
    
    /*
     * This checks if a PreferenceScreen has been clicked on and acts accordingly.
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) 
    {
    	String key = preference.getKey();
    	
    	if (TOGGLE_ACTIVATE.equals(key))
    	{	
    		Intent sIntent = new Intent(this, Toggler.class);
    		sIntent.setAction(ACTION_ACTIVATE_ALL);
    		sendBroadcast(sIntent);
    	} 
    	else if (TOGGLE_DEACTIVATE.equals(key))
    	{
    		Intent sIntent = new Intent(this, Toggler.class);
    		sIntent.setAction(ACTION_DEACTIVATE_ALL);
    		sendBroadcast(sIntent);
    	}
    	else if (INSTALL_BINARIES.equals(key))
    	{
    		 setProgressBarIndeterminateVisibility(true);
	   		 Intent installer = new Intent(this, InstallBinaries.class);
	         startActivityForResult(installer, 0);
    	}
    	else if (TOGGLE_CARRIER_APN.equals(key))
    	{
    		Intent sIntent = new Intent(this, Toggler.class);
    		sIntent.setAction(ACTION_SETUP_CARRIER_APN);
    		sendBroadcast(sIntent);
    	}
    	else if (PREF_CREDITS.equals(key))
    	{
    		showCredits();
    	}
    	
    	return true;
    }
    
    /*
     * Used to temporarily disable some preferences while we
     * check for the state of the proxy/u2nl using the ASyncTask.
     */
    private void disableToggles()
    {
    	toggle_activate.setEnabled(false);
    	toggle_deactivate.setEnabled(false);
    	toggle_carrier_apn.setEnabled(false);
    	prefs_autoswitch_enabled.setEnabled(false);
    	prefs_carrier_selection.setEnabled(false);
    	prefs_use_u2nl.setEnabled(false);
    	prefs_custom_settings.setEnabled(false);
    	install_binaries.setEnabled(false);
    }
    
    /*
     * Re-enable preferences.
     */
    private void enableToggles()
    {
    	toggle_activate.setEnabled(true);
    	toggle_deactivate.setEnabled(true);
    	toggle_carrier_apn.setEnabled(true);
    	prefs_autoswitch_enabled.setEnabled(true);
    	prefs_carrier_selection.setEnabled(true);
    	prefs_use_u2nl.setEnabled(true);
    	install_binaries.setEnabled(true);
    	prefs_custom_settings.setEnabled(prefs_carrier_selection.getValue().toString().equals(CARRIER_CUSTOM));
    }
    
    /*
     * ASyncTask helps update the current state of the proxy 
     * and u2nl when a change is made. (User activating/deactivating proxy/u2nl)
     * or Wifi Changed BroadcastReceiver toggles it.
     */
    private class checkStatus extends AsyncTask<Void, Void, Boolean> 
	{
    	String proxyStatus = getString(R.string.status_inactive);
    	String u2nlStatus = getString(R.string.status_inactive);
    	String apnStatus = getString(R.string.status_inactive);
    	
    	protected void onPreExecute()
    	{
    		Log.d(TAG, "checking proxy/u2nl status");
    		setProgressBarIndeterminateVisibility(true);
    		proxy_status.setSummary(getString(R.string.status_checking));
    		
    		disableToggles();
    	}
    	
    	@Override
		protected Boolean doInBackground(final Void... params) 
		{
    		try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
    		if (isProxyActive())
    			proxyStatus = getString(R.string.status_active);
    		
    		if (isU2NLActive())
    			u2nlStatus = getString(R.string.status_active);
    		
    		if(isAPNActive())
    			apnStatus  = getString(R.string.status_active);
    		
			return true;
		}
    	
    	protected void onPostExecute(Boolean state)
    	{
    		setProgressBarIndeterminateVisibility(false);
    		proxy_status.setSummary(String.format(getString(R.string.proxy_status_sry), proxyStatus, u2nlStatus, apnStatus));
    		toggle_carrier_apn.setSummary((apnStatus.equals(getString(R.string.status_active)) ? R.string.toggle_carrier_apn_active_sry : R.string.toggle_carrier_apn_inactive_sry));
    		toggle_carrier_apn.setTitle((apnStatus.equals(getString(R.string.status_active)) ? R.string.toggle_carrier_apn_active : R.string.toggle_carrier_apn_inactive));
    		
    		enableToggles();
    	}
	}
    
    /*
     * Checks if a proxy server is currently set
     */
    private boolean isProxyActive()
    {
    	Boolean state = false;
    	
    	String currentProxy = Settings.Secure.
    			getString(getContentResolver(), Settings.Secure.HTTP_PROXY);

    	Log.d(TAG, "currentProxy: " + currentProxy);

    	if (currentProxy != null)
    		if (!currentProxy.equals(""))
    			state = true;
    	
    	return state;
    }
    
    /*
     * Checks if the u2nl program is running. 
     * 
     * There might be a better way for this.
     */
    private boolean isU2NLActive()
    {
    	Boolean state = false;
		if(ShellInterface.isSuAvailable()){
			if(ShellInterface.getProcessOutput("ps | busybox grep u2nl").length() > 0){
				state =  true;
			}
		}
    	return state;
    }

    
	/*
	 * Update the Preference summary with the used Proxy server/port
	 */
	private void updateCustomProxySummary(Object value, String proxy, String port)
	{
		
		String customProxy;
		String customPort;
		
		if (proxy != null)
			customProxy = proxy;
		else
			customProxy = prefs_custom_proxy.getText();
		
		if (port != null)
			customPort = port;
		else
			customPort = prefs_custom_proxy_port.getText();
		
		if (value.equals(true))
		{
			if (testCustomProxy(customProxy, customPort))
			{
					Log.d(TAG, "custom proxy is valid!");
				
	    			prefs_custom_proxy.setSummary(String.format(
	    					getString(R.string.prefs_custom_proxy_sryOn), 
	    					customProxy));
	    			prefs_custom_proxy_port.setSummary(String.format(
	    					getString(R.string.prefs_custom_proxy_port_sryOn), 
	    					customPort));
			} else {
				Log.d(TAG, "custom proxy is invalid!");
				
				prefs_custom_proxy.setSummary(getString(R.string.prefs_custom_proxy_invalid));
				prefs_custom_proxy_port.setSummary(getString(R.string.prefs_custom_proxy_invalid));
			}
	
		} else {
			prefs_custom_proxy.setSummary(String.format(getString(R.string.prefs_custom_proxy_sryOff), ""));
			prefs_custom_proxy_port.setSummary(String.format(getString(R.string.prefs_custom_proxy_port_sryOff), ""));
		}
	}
	
	private boolean testCustomProxy(String proxy, String port)
	{
		Boolean validProxy = true;
		
		String customProxy;
		String customPort;
		
		if (proxy !=null)
			customProxy = proxy;
		else
			customProxy = prefs_custom_proxy.getText();
		
		
		if (port != null)
			customPort = port;
		else
			customPort = prefs_custom_proxy_port.getText();
		
		if (!validateIP(customProxy))
		{
			Log.e(TAG, "Invalid ip address");
			validProxy = false;
		}
		
		try
		{
			int portInt = Integer.parseInt(customPort);
			
			if (portInt < 0 || portInt > 65535)
			{
				Log.e(TAG, "Invalid port number");
				validProxy = false;
			}
			
		} catch (NumberFormatException npe)
		{
			npe.printStackTrace();
			Log.e(TAG, "Invalid port number");
			validProxy = false;
		}
		
		return validProxy;
	}
	
	/*
	 * Update Carrier Selection Summary
	 */
	private void updateCarrierSelectionSummary(Object value){
		prefs_custom_settings.setEnabled(value.toString().equals(CARRIER_CUSTOM));
	}
	
	/*
	 * Update the Preference summary with the used MMS server/port
	 */
	private void updateCustomMMSSummary(Object value, String server, String port)
	{
		String customProxy;
		String customPort;
		
		if (server != null)
			customProxy = server;
		else
			customProxy = prefs_custom_mms.getText();
		
		if (port != null)
			customPort = port;
		else
			customPort = prefs_custom_mms_port.getText();
		
		if (value.equals(true))
		{
			if (testCustomProxy(customProxy, customPort))
			{
					Log.d(TAG, "custom MMS server is valid!");
				
					prefs_custom_mms.setSummary(String.format(
	    					getString(R.string.prefs_custom_mms_sryOn), 
	    					customProxy));
	    			prefs_custom_mms_port.setSummary(String.format(
	    					getString(R.string.prefs_custom_mms_port_sryOn), 
	    					customPort));
			} else {
				Log.d(TAG, "custom mms is invalid!");
				
				prefs_custom_mms.setSummary(getString(R.string.prefs_custom_mms_invalid));
				prefs_custom_mms_port.setSummary(getString(R.string.prefs_custom_mms_invalid));
			}
	
		} else {
			prefs_custom_mms.setSummary(String.format(getString(R.string.prefs_custom_mms_sryOff), ""));
			prefs_custom_mms_port.setSummary(String.format(getString(R.string.prefs_custom_mms_port_sryOff), ""));
		}
	}
	
	/*
	 * Method that helps validate the MMS server/port
	 */
	private boolean testCustomMMSServer(String server, String port)
	{
		Boolean validProxy = true;
		
		String customProxy;
		String customPort;
		
		if (server !=null)
			customProxy = server;
		else
			customProxy = prefs_custom_mms.getText();
		
		
		if (port != null)
			customPort = port;
		else
			customPort = prefs_custom_mms_port.getText();
		
		if (!validateIP(customProxy))
		{
			Log.e(TAG, "Invalid ip address");
			validProxy = false;
		}
		
		try
		{
			int portInt = Integer.parseInt(customPort);
			
			if (portInt < 0 || portInt > 65535)
			{
				Log.e(TAG, "Invalid port number");
				validProxy = false;
			}
			
		} catch (NumberFormatException npe)
		{
			npe.printStackTrace();
			Log.e(TAG, "Invalid port number");
			validProxy = false;
		}
		
		return validProxy;
	}
	

	
	private Preference.OnPreferenceChangeListener 
		customProxyEditTextListener = new Preference.OnPreferenceChangeListener() 
	{
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{
			updateCustomProxySummary(true, newValue.toString(), null);
			
			return true;
		}
	};
	
	private Preference.OnPreferenceChangeListener 
		customProxyPortEditTextListener = new Preference.OnPreferenceChangeListener() 
	{
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{
			updateCustomProxySummary(true, null, newValue.toString());
			
			return true;
		}
	};

	
	private Preference.OnPreferenceChangeListener 
		customMMSEditTextListener = new Preference.OnPreferenceChangeListener() 
	{
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{
			updateCustomMMSSummary(true, newValue.toString(), null);
			
			return true;
		}
	};
	
	private Preference.OnPreferenceChangeListener 
		customMMSPortEditTextListener = new Preference.OnPreferenceChangeListener() 
	{
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{
			updateCustomMMSSummary(true, null, newValue.toString());
			
			return true;
		}
	};
	
    
    /* 
     * Set the Selected Carrier on Change
     */
    private Preference.OnPreferenceChangeListener 
    setCarrierSelection = new Preference.OnPreferenceChangeListener() 
	{
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{

			updateCarrierSelectionSummary(newValue);
			return true;
		}
	};
    
    
	/*
	 * Method that helps validate IP addresses.
	 */
    private final static boolean validateIP(String ipAddress)
    {
        String[] parts = ipAddress.split( "\\." );
        
        if (parts.length != 4)
        {
            return false;
        }

        for ( String s : parts )
        {
            int i = Integer.parseInt( s );

            if ( (i < 0) || (i > 255) )
            {
                return false;
            }
        }

        return true;
    }
    
    /*
     * AlertDialog with special credits to special people :)
     */
    private void showCredits()
    {
    	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    	alertDialog.setTitle(this.getString(R.string.credits));
    	alertDialog.setMessage(this.getString(R.string.credits_txt));
    	alertDialog.setIcon(R.drawable.icon);
    	alertDialog.setButton(this.getString(R.string.credits_ok), new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {	
    			return;
    		}
    	});
    	alertDialog.show();
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
}




