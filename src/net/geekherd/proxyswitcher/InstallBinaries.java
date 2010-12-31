package net.geekherd.proxyswitcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;
import java.io.*;

import android.os.Environment;



public class InstallBinaries extends PreferenceActivity{
	
	public final static String INSTALL_U2NL = "install_u2nl";
	public final static String INSTALL_SQLITE3 = "install_sqlite3";

	public boolean sdAvailable = false;
	boolean sdWritable = false;
	
	private final static String TAG = "InstallBinaries";
		
	private String systemMount;
	
	private PreferenceScreen install_u2nl,install_sqlite3;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	super.onCreate(savedInstanceState);
    	setProgressBarIndeterminateVisibility(true);
        addPreferencesFromResource(R.xml.install_binaries);
        
        install_u2nl = (PreferenceScreen)findPreference("install_u2nl");
        install_sqlite3 = (PreferenceScreen)findPreference("install_sqlite3");
        
        systemMount = findSystemMount();
                
    }
    
    private BroadcastReceiver mBinaryChangeActionReceiver = new BroadcastReceiver()
	{
    	@Override
		public void onReceive(Context context, Intent intent) 
		{
    		new checkStatus().execute();
		}
	};
    
    /*
     * As soon as the activity resumes, check for the state of the binaries.
     */
    @Override
	protected void onResume()
	{
		super.onResume();
		
		new checkStatus().execute();
		
		this.registerReceiver(this.mBinaryChangeActionReceiver, new IntentFilter());
	}
    
    @Override
	protected void onStop()
	{
		super.onStop();
		
		this.unregisterReceiver(this.mBinaryChangeActionReceiver);
		
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
    	} else if(INSTALL_SQLITE3.equals(key)){
    		if(sqlite3Exists()){
    			uninstallSqlite3();
    		} else {
    			installSqlite3();
    		}
    	}
    	
    	new checkStatus().execute();
    	
    	return true;
    }
    /*
     * Check to see if u2nl binary already exists in /system/bin 
     */
    protected boolean u2nlExists(){
        if(ShellInterface.isSuAvailable()){
        	if(ShellInterface.getProcessOutput("ls /system/bin | busybox grep u2nl").length() > 0)
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
        		"killall u2nl \n"+
        		"rm /system/bin/u2nl"
        	);
    	}
    }
    
    /*
     * Install the sqlite3 binary
     */
    protected void installSqlite3(){
    	Log.d(TAG, "Moving SQLITE3 to SDCard");
    	assetToSd("sqlite3");
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
        	if(ShellInterface.getProcessOutput("ls /system/xbin | busybox grep sqlite3").length() > 0)
        	{
        		return true;
        	}
        }
        return false;
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
    		for(b=0;b<=6;b++){
            	if(ShellInterface.getProcessOutput("mount | busybox grep '/dev/block/mtdblock"+b+" on /system'").length() > 0){
            		return Integer.toString(b);
            	}
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
			try{
				InputStream asset = getApplicationContext().getAssets().open(file);
				
				File destinationFile = new File("/sdcard/" + file);    

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
	
	protected void disableToggles()
    {
    	install_u2nl.setEnabled(false);
    	install_sqlite3.setEnabled(false);

    }
	
	protected void enableToggles()
    {
    	install_u2nl.setEnabled(true);
    	install_sqlite3.setEnabled(true);

    }
	
    private class checkStatus extends AsyncTask<Void, Void, Boolean> 
	{
    	boolean u2nlStatus = false;
    	boolean sqlite3Status = false;

    	
    	protected void onPreExecute()
    	{
    		Log.d(TAG, "checking binary status");
    		setProgressBarIndeterminateVisibility(true);
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

    		u2nlStatus = u2nlExists();
    		sqlite3Status = sqlite3Exists();

    		
			return true;
		}
    	
    	protected void onPostExecute(Boolean state)
    	{
    		setProgressBarIndeterminateVisibility(false); 
        	install_u2nl.setTitle((u2nlStatus ? "Uninstall" : "Install")+" u2nl");
        	install_u2nl.setSummary((u2nlStatus ? "Uninstall" : "Install")+" the u2nl binary");
        	
        	
        	install_sqlite3.setTitle((sqlite3Status ? "Uninstall" : "Install")+" sqlite3");
        	install_sqlite3.setSummary((sqlite3Status ? "Uninstall" : "Install")+" the sqlite3 binary");
        	
        	enableToggles();
    	}
	}
    
    
    
}
