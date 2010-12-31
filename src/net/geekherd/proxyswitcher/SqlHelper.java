package net.geekherd.proxyswitcher;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlHelper {

	private static final String DB_NAME = "proxyswitcher.db";
	private static final int DB_VER = 5;
	
	private Context context;
	private SQLiteDatabase db;
	
	OpenHelper openHelper = null;

	public String[] ApnColumnsDefault = {"apn_name","apn_numeric","apn_mcc","apn_mnc","apn_apn","apn_user","apn_server","apn_password","apn_proxy","apn_port","apn_mmsproxy","apn_mmsport","apn_mmsc","apn_authtype","apn_type","apn_current"};
	public String[] ApnColumnsDesirec = {"apn_name","apn_numeric","apn_mcc","apn_mnc","apn_apn","apn_user","apn_server","apn_password","apn_proxy","apn_port","apn_mmsproxy","apn_mmsport","apn_mmsprotocol","apn_mmsc","apn_authtype","apn_type","apn_insert_by","apn_operator"};

	public SqlHelper(Context context) {
		this.context = context;
		openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
	}
	
	/*
	 * Load a Carriers Configuration by Carrier Name
	 */
	public Cursor getCarrierConfig(String carrier)
	{
		return this.db.query("configuration", new String[] { "id", "carrier", "proxy", "proxy_port", "mms_proxy", "mms_proxy_port" }, "carrier = '"+carrier+"'", null, null, null, "id asc LIMIT 1");
	}
	/*
	 * Load a Carriers APN Configuration by Carrier Name and Device
	 */
	public Cursor getCarrierAndDeviceApnConfig(String carrier,String device)
	{
		return this.db.query("apns", android.os.Build.DEVICE.equals("deirec") ? ApnColumnsDesirec : ApnColumnsDefault, "carrier = '"+carrier+"' AND device = '"+device+"'", null, null, null, "ordering asc");
	}
	
	public void close() {
	    if (openHelper != null) {
	    	openHelper.close();
	    }
	}


	private static class OpenHelper extends SQLiteOpenHelper {
	      OpenHelper(Context context) {
	          super(context, DB_NAME, null, DB_VER);
	       }
	
	       @Override
	       public void onCreate(SQLiteDatabase db) {
	          db.execSQL("CREATE TABLE configuration (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `carrier` TEXT NULL, `proxy` TEXT NULL, `proxy_port` TEXT NULL, `mms_proxy` TEXT NULL, `mms_proxy_port` TEXT NULL)");
	          
	          //carrier configuration values
	          db.execSQL("INSERT INTO configuration VALUES(NULL,'metropcs','10.223.2.4','3128','65.91.116.37','3128')");
	          db.execSQL("INSERT INTO configuration VALUES(NULL,'cricket','10.132.25.254','8080','10.132.25.254','8080')");
	          db.execSQL("INSERT INTO configuration VALUES(NULL,'custom',NULL,NULL,NULL,NULL)");

	          
	          db.execSQL("CREATE TABLE device_configuration (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `device` TEXT NULL, `apiver` TEXT NULL, `interface` TEXT NULL)");
	          db.execSQL("INSERT INTO device_configuration VALUES(NULL,'default',NULL,'rmnet0')");
	          db.execSQL("INSERT INTO device_configuration VALUES(NULL,'hero',NULL,'rmnet0')");
	          db.execSQL("INSERT INTO device_configuration VALUES(NULL,'supersonic',NULL,'rmnet0')");
	          db.execSQL("INSERT INTO device_configuration VALUES(NULL,'desirec',NULL,'rmnet0')");
	          db.execSQL("INSERT INTO device_configuration VALUES(NULL,'inc',NULL,'rmnet0')");
	          db.execSQL("INSERT INTO device_configuration VALUES(NULL,'sholes','8','ppp0')");
	          db.execSQL("INSERT INTO device_configuration VALUES(NULL,'sholes',NULL,'ppp0')");
	          
			
	          db.execSQL("CREATE TABLE apns (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `carrier` TEXT NULL, `device` TEXT NULL, `apn_name` TEXT NULL, `apn_numeric` TEXT NULL,`apn_mcc` TEXT NULL, `apn_mnc` TEXT NULL, `apn_apn` TEXT NULL, `apn_user` TEXT NULL, `apn_server` TEXT NULL,`apn_password` TEXT NULL, `apn_proxy` TEXT NULL, `apn_port` TEXT NULL, `apn_mmsproxy` TEXT NULL, `apn_mmsport` TEXT NULL, `apn_mmsprotocol` TEXT NULL,`apn_mmsc` TEXT NULL, `apn_authtype` TEXT NULL, `apn_type` TEXT NULL, `apn_insert_by` TEXT NULL,`apn_operator` TEXT NULL, `apn_current` TEXT NULL, `ordering` TEXT NULL)");
	          
	          //device/carrier apn values
	          // works: sholes,inc
	          // TODO proper cricket settings
	          db.execSQL("INSERT INTO apns VALUES(NULL, 'metropcs', 'default', 'MetroPCS', '310004', '310', '004', 'internet', 'replaceMDN@mymetropcs.com', 'wap.metropcs.net', 'replaceSPC', 'wap.metropcs.net:3128', '3128', 'wap.metropcs.net', '3128', NULL, 'http://mms.metropcs.net:3128/mmsc', NULL, NULL, NULL, NULL, '1', '1')");
	          db.execSQL("INSERT INTO apns VALUES(NULL, 'cricket', 'default', 'Cricket', '310004', '310', '004', 'internet', 'replaceMDN@cricket.com', '', '', '', '8080', '', '8080', NULL, '', NULL, NULL, NULL, NULL, '1', '1')");
	          // TODO custom apn settings integration
	          db.execSQL("INSERT INTO apns VALUES(NULL, 'custom', 'default', 'Custom', '310004', '310', '004', '', '', '', '', '', '', '', '', NULL, '', NULL, NULL, NULL, NULL, '1', '1')");
	          
	          //desirec (ERIS)
	          db.execSQL("INSERT INTO apns VALUES(NULL, 'metropcs', 'desirec', 'CDMA', '310012', '000', '00', '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '-1', '*', 'internal', NULL, NULL, '1')");
	          db.execSQL("INSERT INTO apns VALUES(NULL, 'metropcs', 'desirec', 'metropcs', '310012', '310', '004', '1', 'replaceMDN@mymetropcs.com', 'wap.metropcs.net', 'replaceSPC', 'wap.metropcs.net:3128', '3128', 'wap.metropcs.net', '3128', '2.0', 'http://mms.metropcs.net:3128/mmsc', '-1', 'mms', 'internal', NULL, NULL, '2')");
	          db.execSQL("INSERT INTO apns VALUES(NULL, 'metropcs', 'desirec', 'Android', '310012', '310', '995', 'internet', '*', '*', '*', NULL, NULL, NULL, NULL, NULL, NULL, '-1', NULL, 'external', NULL, NULL, '3')");
	          // TODO proper cricket settings
	          db.execSQL("INSERT INTO apns VALUES(NULL, 'cricket', 'desirec', 'CDMA', '310012', '000', '00', '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '-1', '*', 'internal', NULL, NULL, '1')");
	          db.execSQL("INSERT INTO apns VALUES(NULL, 'cricket', 'desirec', 'cricket', '310012', '310', '004', '1', 'replaceMDN@cricket.com', '', '', '', '8080', 'wap.metropcs.net', '8080', '2.0', '', '-1', 'mms', 'internal', NULL, NULL, '2')");
	          db.execSQL("INSERT INTO apns VALUES(NULL, 'cricket', 'desirec', 'Android', '310012', '310', '995', 'internet', '*', '*', '*', NULL, NULL, NULL, NULL, NULL, NULL, '-1', NULL, 'external', NULL, NULL, '3')");
	        
	       }

	       @Override
	       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	          db.execSQL("DROP TABLE IF EXISTS configuration");
	          db.execSQL("DROP TABLE IF EXISTS device_configuration");
	          db.execSQL("DROP TABLE IF EXISTS apns");
	          onCreate(db);
	       }
	       
	}
}
