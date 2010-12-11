package net.geekherd.proxyswitcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;
import android.content.res.AssetManager;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Environment;
import android.util.Log;


public class InstallBinaries extends PreferenceActivity{
	
	public final static String INSTALL_U2NL = "install_u2nl";
	public final static String INSTALL_SQLITE3 = "install_sqlite3";
	
	public boolean sdAvailable = false;
	boolean sdWritable = false;
	
	private final static String TAG = "InstallBinaries";
	
	private static final String[] MTDBLOCKS = new String[]{
	    "mtdblock0",
	    "mtdblock1",
	    "mtdblock2",
	    "mtdblock3",
	    "mtdblock4",
	    "mtdblock5"
	};
	
	private String systemMount;
	
	private PreferenceScreen install_u2nl,install_sqlite3;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	super.onCreate(savedInstanceState);
    	setProgressBarIndeterminateVisibility(true);
        addPreferencesFromResource(R.xml.configuration_binaries);
        
        install_u2nl = (PreferenceScreen)findPreference("install_u2nl");
        install_sqlite3 = (PreferenceScreen)findPreference("install_sqlite3");
        
        refreshStatus();
        
        systemMount = findSystemMount();
        
    }
    
    /*
     * This checks if a PreferenceScreen has been clicked on and acts accordingly.
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) 
    {
    	
    	String key = preference.getKey();
    	
    	if (INSTALL_U2NL.equals(key))
    	{	
    		if(u2nlExists()){
    			uninstallU2nl();
    		} else {
    			installU2nl();
    		}
    		
    		refreshStatus();
    	} else if(INSTALL_SQLITE3.equals(key)){
    		if(sqlite3Exists()){
    			uninstallSqlite3();
    		} else {
    			installSqlite3();
    		}
    		
    		refreshStatus();
    	}
    	
    	
    	return true;
    }
    /*
     * Check to see if u2nl binary already exists in /system/bin 
     */
    protected boolean u2nlExists(){
        if(ShellInterface.isSuAvailable()){
        	if(ShellInterface.getProcessOutput("ls /system/bin | grep u2nl").length() > 0)
        	{
        		return true;
        	}
        }
        return false;
    }
    
    /*
     * Install the u2nl binary
     */
    protected void installU2nl(){
    	Log.d(TAG, "Moving U2NL to SDCard");
    	assetToSd("u2nl");
    	Log.d(TAG, "Installing U2NL");
    	if(ShellInterface.isSuAvailable()) { 
    		ShellInterface.runCommand(
    			"mount -o rw,remount -t yaffs2 /dev/block/mtdblock"+systemMount+" /system \n" +
    			"cp /sdcard/u2nl /system/bin/u2nl \n" +
    			"chmod 0755 /system/bin/u2nl"
    		);
    	}
    }
    
    /*
     * Remove the u2nl binary
     */
    protected void uninstallU2nl(){
    	Log.d(TAG, "Uninstalling U2NL");
    	if(ShellInterface.isSuAvailable()) { 
    		ShellInterface.runCommand(
        		"mount -o rw,remount -t yaffs2 /dev/block/mtdblock"+systemMount+" /system \n" +
        		"rm /system/bin/u2nl"
        	);
    	}
    }
    
    /*
     * Install the sqlite3 binary
     */
    protected void installSqlite3(){
    	Log.d(TAG, "Moving SQLITE3 to SDCard");
    	assetToSd("u2nl");
    	Log.d(TAG, "Installing SQLITE3");
    	if(ShellInterface.isSuAvailable()) { 
    		ShellInterface.runCommand(
    			"mount -o rw,remount -t yaffs2 /dev/block/mtdblock"+systemMount+" /system \n" +
    			"cp /sdcard/sqlite3 /system/xbin/sqlite3 \n" +
    			"chmod 0755 /system/xbin/sqlite3"
    		);
    	}
    }
    
    /*
     * Remove the sqlite3 binary
     */
    protected void uninstallSqlite3(){
    	Log.d(TAG, "Uninstalling SQLITE3");
    	if(ShellInterface.isSuAvailable()) { 
    		ShellInterface.runCommand(
        		"mount -o rw,remount -t yaffs2 /dev/block/mtdblock"+systemMount+" /system \n" +
        		"rm /system/xbin/sqlite3"
        	);
    	}
    }
    
    /*
     * Check to see if sqlite3 binary already exists in /system/xbin 
     */
    protected boolean sqlite3Exists(){
        if(ShellInterface.isSuAvailable()){
        	if(ShellInterface.getProcessOutput("ls /system/xbin | grep sqlite3").length() > 0)
        	{
        		return true;
        	}
        }
        return false;
    }
    
    
    /*
     * Refreshes the status of the binaries in the Preference List
     */
    protected void refreshStatus(){
        
        if(u2nlExists()){
        	install_u2nl.setTitle("Uninstall u2nl");
        	install_u2nl.setSummary("Uninstall the u2nl binary");
        } else {
        	install_u2nl.setTitle("Install u2nl");
        	install_u2nl.setSummary("Install the u2nl binary");
        }
        if(sqlite3Exists()){
        	install_sqlite3.setTitle("Uninstall sqlite3");
        	install_sqlite3.setSummary("Uninstall the sqlite3 binary");
        } else {
        	install_sqlite3.setTitle("Install sqlite3");
        	install_sqlite3.setSummary("Install the sqlite3 binary");
        }

    }
    /*
     * Locate the System mount to be able to write to it
     * Probably a better way to do this
     */
    protected String findSystemMount(){
    	if(systemMount!=null){
    		return systemMount;
    	}
    	Log.d(TAG, "Locating System Mount");
    	if(ShellInterface.isSuAvailable()) { 
    		int b=0;
    		for(String block : MTDBLOCKS){
            	if(ShellInterface.getProcessOutput("mount | grep '/dev/block/mtdblock"+b+" on /system'").length() > 0)
            	{
            		return MTDBLOCKS[b];
            	}
            	b++;
    		}
    	}
    	return null;
    }
    
    /*
     * Move a file from the asset/ folder to the root of the sdCard
     * @param String file the filename of the file to move
     */
	protected void assetToSd(String file){
		Log.d(TAG, "Moving file to sdcard: "+file);
		
		String sdState = Environment.getExternalStorageState();


		if (Environment.MEDIA_MOUNTED.equals(sdState)) {
		    sdAvailable = sdWritable  = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdState)) {
			sdAvailable = true;
			sdWritable = false;
		} else {
			sdAvailable = sdWritable = false;
		}
		
		
		if( sdAvailable && sdWritable){	
			File sd = Environment.getExternalStorageDirectory();
			try{
				InputStream asset = getApplicationContext().getAssets().open(file);
				
				File destinationFile = new File("/mnt/sdcard/" + file);    

				BufferedOutputStream buffer = new BufferedOutputStream(new FileOutputStream(destinationFile)); 
				byte byt[] = new byte[1024]; 
				int i; 

				for (long l = 0L; (i = asset.read(byt)) != -1; l += i ) {
				    buffer.write(byt, 0, i); 
				}

				asset.close();               
				buffer.close();

			} catch(IOException io){
				Log.e(TAG, "File Not Found"+io);
				io.printStackTrace();
			}
		
		} else {
			Toast.makeText(getApplicationContext(), "SD Card Needs to be Inserted and Writable", Toast.LENGTH_LONG).show();
		}
	}
    
    
}
