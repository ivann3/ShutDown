package com.miki.mikishutdown;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private TextView tv,numbertv;
	private  int counter;
	private Button shutdown;
	public SharedPreferences shutdonwNumber,ShutdownSP,shutdownTimeSP,LCDIDRedFailSP,LCDIDSP;
	private EditText time,shutdownNumberET;
	private long editTime;
	private int editNumber;
	private int readSD;
	private File file;
	private TextView sdShow;
	private String tabBnStr;
	private int lcdInt;
	private String lcdIDStr,lcdIDEtStr;
	private EditText etLCD;
	private TextView lcdshow;
	
	private boolean judgeSD = false;
	private boolean flagMc = false;             //记录mc的状态，是否处于启动状态,false为未启动
	MyCount mc;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        lcdIDStr = "/sys/bus/platform/drivers/factoryapi/lcd";
        Log.d("-----lcdInt", "----------------"+lcdInt);
        
        tv = (TextView) findViewById(R.id.tv);
        sdShow = (TextView) findViewById(R.id.sdShow);
        numbertv = (TextView) findViewById(R.id.numbertv);
        shutdown = (Button) findViewById(R.id.shutdown);
        time = (EditText) findViewById(R.id.time);
        etLCD = (EditText) findViewById(R.id.etLCD);
        shutdownNumberET = (EditText) findViewById(R.id.shutdownNumberet);
        lcdshow = (TextView) findViewById(R.id.lcdshow);
        
        lcdshow.setText(getSensorId(lcdIDStr));
        
        booleanSDTest();
        MySharedPreferenceses();
        //tabWriteSD();
        judge();
    }
    
    /**
     * LCD ID核对方法
     */
    /**************************************************************/
    private void lcdIDJude(){
    	/*counter = shutdonwNumber.getInt("counter",0);
		editNumber = ShutdownSP.getInt("editNumber", 200);
	    editTime = shutdownTimeSP.getLong("editTime", 10*1000);*/
    	lcdInt = LCDIDRedFailSP.getInt("lcdInt", 0);
    	if(!(etLCD.getText().toString().trim()).equals(getSensorId(lcdIDStr))){
    		++lcdInt;
    		Editor mEditor = LCDIDRedFailSP.edit();
    		mEditor.putInt("lcdInt", lcdInt);
    		mEditor.commit();
    	}
    	
    	Log.i("MiKi", "lcdInt = " + lcdInt);
    	try {
			OutputStream outputLCD = new FileOutputStream("/storage/sdcard0/MiKiLCDID.txt",false);
			//BufferedOutputStream bos = new BufferedOutputStream(outputLCD);
			Writer writer = new OutputStreamWriter(outputLCD);
			try {
				writer.write("Set Shutdown : "+editNumber+"\r\n"+
			"Set Shutdown Countdown : "+editTime/1000+"\r\n"+"Has been shut down : "+counter
			+"\r\n"+"LCD ID read failure :"+lcdInt);
				writer.close();
				outputLCD.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public String getSensorId(String path) {
		String LCDResult = "unknow";
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			String text = br.readLine();
			if(text != null)
				LCDResult = text.trim();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return LCDResult;
    }
    /****************************************************************/
    /**
     * SD卡判断方法
     */  
    private void booleanSDTest(){
    	if(ExistSDCard()){
    		sdShow.setTextColor(Color.GREEN);
    		sdShow.setText("SD卡可用");
    		tabBnStr = "true";
		}else{
			sdShow.setTextColor(Color.RED);
			sdShow.setText("SD卡不可用!!!");
			tabBnStr = "false";
		}
    }
    /**
     * SD卡判断方法
     */   
    private boolean ExistSDCard(){
		StringBuffer sb = new StringBuffer("");
		boolean result = false;
		file = new File("storage");
		File fileList[] = file.listFiles();
		for(File f : fileList){
			if(f.isDirectory() && f.canWrite() && f.canRead()){
				if(f.getPath().trim().equals("storage/sdcard0") ||
						f.getPath().trim().equals("storage/usbotg")){
					continue;
				} else {
					result = true;
					break;
				}
			}
		}
		return result;
	}
    /**
     * 记录外置SD卡读取状态
     */
    private void tabWriteSD(){
    	counter = shutdonwNumber.getInt("counter",0);
    	Log.i("MiKi", "counter = " + counter);
    	try {
			OutputStream outputStream = new FileOutputStream("/storage/sdcard0/MiKiSD.txt",true);
			Writer writer = new OutputStreamWriter(outputStream);
			try {
				writer.write(counter+" : "+tabBnStr+"\r\n");
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    /**
     * SharedPreferences方法集合
     * shutdonwNumber:存储记录已关机次数
     * ShutdownSP：存储设置关机的次数
     * shutdownTimeSP：存储设置关机倒计时的时间
     */
    public void MySharedPreferenceses(){
    	//记录关机次数-开始
        shutdonwNumber  = getSharedPreferences("shutDownNumber", MODE_PRIVATE);
        ShutdownSP = getSharedPreferences("ShutdownSP", MODE_PRIVATE);
        shutdownTimeSP = getSharedPreferences("shutdownTimeSP", MODE_PRIVATE);
        LCDIDRedFailSP = getSharedPreferences("LCDIDRedFailSP", MODE_PRIVATE);
        LCDIDSP = getSharedPreferences("LCDIDSP", MODE_PRIVATE);
        
        counter = shutdonwNumber.getInt("counter",0);
        editNumber = ShutdownSP.getInt("editNumber", 200);
        editTime = shutdownTimeSP.getLong("editTime", 10*1000);
        lcdInt = LCDIDRedFailSP.getInt("lcdInt", 0);
        lcdIDEtStr = LCDIDSP.getString("lcdIDEtStr", null);
        
        numbertv.setText("已关机："+counter+"  次");
        Editor mEditor = shutdonwNumber.edit();
        mEditor.putInt("counter", counter);
        
        
        mEditor.commit();
      //记录关机次数-结束
    }
    
     /**
      * 判断是否是第一次执行
      * counter:记录关机的次数
      */
    private void judge(){
    	
    	if(counter > 0){                             //不是第一次关机，执行自动关机
    		time.setText((String.valueOf(editTime/1000)));
    	    shutdownNumberET.setText(String.valueOf(editNumber));
    	    etLCD.setText(lcdIDEtStr);
    		tv.setVisibility(View.VISIBLE);         //倒计时TextView可见
        	mc = new MyCount(editTime, 1000);
        	flagMc = true;
            mc.start();
    	}
    }
    /**
     * 关机Button的点击
     * @param v
     */
    public void shutdown(View v){
    	Toast.makeText(MainActivity.this, "正在关机", 0).show();
    	if(!(time.getText().toString().equals(""))){
			editTime = Long.parseLong(time.getText().toString()) * 1000;
			Editor timeEditor = shutdownTimeSP.edit();
			timeEditor.putLong("editTime", editTime);
			timeEditor.commit();
		}
    	
    	if(!(shutdownNumberET.getText().toString().equals(""))){
        	editNumber = Integer.parseInt(shutdownNumberET.getText().toString());
    		Editor shutEditor = ShutdownSP.edit();
    		shutEditor.putInt("editNumber", editNumber);
    		shutEditor.commit();
        }
    	
    	if(!etLCD.getText().toString().equals("")){
    		lcdIDEtStr = etLCD.getText().toString().trim();
    		Editor lcdEditor = LCDIDSP.edit();
    		lcdEditor.putString("lcdIDEtStr", lcdIDEtStr);
    		lcdEditor.commit();
    	}
    	
    	time.setText((String.valueOf(editTime/1000)));
	    shutdownNumberET.setText(String.valueOf(editNumber));
	    etLCD.setText(lcdIDEtStr);
	    
    	tv.setVisibility(View.VISIBLE);         //倒计时TextView可见
    	mc = new MyCount(editTime, 1000);
        flagMc = true;
        mc.start();
    }
    /**
     * 取消关机的Button点击
     * tv:显示倒计时秒数的TextView
     * @param v
     */
    public void cancle(View v){
    	tv.setVisibility(View.INVISIBLE);
    	counter--;
    	if(counter < 0){
    		counter = 0;
    	}
    	Editor mEditor = shutdonwNumber.edit();
        mEditor.putInt("counter", counter);
        mEditor.commit();
        if(flagMc){
	        flagMc = false;
	        mc.cancel();
	        Toast.makeText(MainActivity.this, "已取消关机", 0).show();
        }
    }
    
    /**
     * 重置点击
     */
    public void resetting(View v){
    	shutdown.setEnabled(true);
    	shutdown.setBackgroundResource(R.drawable.button_style);
    	if(counter == editNumber){
			editNumber = 200;
	    	editTime = 2*1000;
	    	counter = 0;
	    	lcdInt = 0;
	    	lcdInt = 0;
	    	
	    	time.setText(null);
	    	shutdownNumberET.setText(null);
	    	etLCD.setText(null);
	    	sharedPreferencesesWrite();
	    	Toast.makeText(MainActivity.this, "重置成功", 0).show();
    	}else{
    		counter = 0;
    		sharedPreferencesesWrite();
			tv.setVisibility(View.INVISIBLE);
			if(flagMc){
				flagMc = false;
				mc.cancel();
			}
			Toast.makeText(MainActivity.this, "重置成功", 0).show();
			
    	}
    	File file = new File("/storage/sdcard0/MiKiSD.txt");
    	if(file != null && file.exists()){
    		file.delete();
    	}
    	File fileLCD = new File("/storage/sdcard0/MiKiLCDID.txt");
    	if(fileLCD != null && fileLCD.exists()){
    		fileLCD.delete();
    	}
    }
    /**
     * 已关机次数、设置的关机倒计时、设置的关机次数的写入
     * numbertv：显示已关机次数的TextView
     */
    public void sharedPreferencesesWrite(){
    	numbertv.setText("已关机："+counter+"  次");
        Editor mEditor = shutdonwNumber.edit();
        mEditor.putInt("counter", counter);
        mEditor.commit();
    	
    	Editor shutEditor = ShutdownSP.edit();
		shutEditor.putInt("editNumber", editNumber);
		shutEditor.commit();
		
		Editor timeEditor = shutdownTimeSP.edit();
		timeEditor.putLong("editTime", editTime);
		timeEditor.commit();
		
		Editor lcdEditor = LCDIDRedFailSP.edit();
		lcdEditor.putInt("lcdInt", lcdInt);
		lcdEditor.commit();
    }
    /**
     * 倒计时内部类
     * @author Administrator
     *
     */
    class MyCount extends CountDownTimer{
		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
		}
		@Override
		/**
		 * 倒计时结束时执行的方法
		 */
		public void onFinish() {
			// TODO Auto-generated method stub
			counter = shutdonwNumber.getInt("counter",0);
	        editNumber = ShutdownSP.getInt("editNumber", 200);
	        editTime = shutdownTimeSP.getLong("editTime", 2*1000);
			
        	numbertv.setText("已关机："+counter+"  次");
            Editor mEditor = shutdonwNumber.edit();
            mEditor.putInt("counter", ++counter);
            mEditor.commit();
            
            tabWriteSD();
            lcdIDJude();
	        
        	Intent i = new Intent(Intent.ACTION_REBOOT);
			i.putExtra("nowait", 1);
			i.putExtra("interval", 1);
			i.putExtra("window", 0);
			sendBroadcast(i);
			tv.setText("finish");
		}
		
		/**
		 * 实现倒计时
		 */
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			 if(counter >= editNumber){
				    flagMc = false;
		        	mc.cancel();
		        	
		        	tv.setVisibility(View.INVISIBLE);
		        	shutdown.setEnabled(false);
		        	shutdown.setBackgroundColor(Color.GRAY);
		        }
			tv.setTextColor(Color.RED);
			tv.setText("关机倒计时"+editTime/1000
					+"秒(" + millisUntilFinished / 1000 + ")");
		}
    }
}
