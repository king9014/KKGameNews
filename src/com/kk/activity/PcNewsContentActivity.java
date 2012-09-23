package com.kk.activity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import net.youmi.android.AdManager;
import net.youmi.android.AdView;
import net.youmi.android.appoffers.YoumiOffersManager;
import net.youmi.android.appoffers.YoumiPointsManager;

import cn.dreamfield.parser.DreamFieldReader;
import cn.dreamfield.parser.PageShow;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PcNewsContentActivity extends Activity implements OnClickListener {
	private LinearLayout contentlinear;
	private ImageView backimg;
	private TextView pagetext;
	private ImageView nextimg;
	
	private FrameLayout toplayout;
	private RelativeLayout bottomlayout;
	
	private WebView webview;
	private WebSettings webSettings;
	
	private static String HTML_ROOT = "http://dreamfield.cn/lib/html/";
	private static String HTML_REQUEST_NEXT = "http://dreamfield.cn/lib/nextpage.php?id=";
	private static String HTML_REQUEST_BACK = "http://dreamfield.cn/lib/prepage.php?pid=";
	private static int UPDATE_CONTENT = 1;
	private static int ASKFOR_NOAD = 2;
	private String url;
	private int id;
	private int pid;
	private int total;
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId() == UPDATE_CONTENT) {
			webview.reload();
		} else if(item.getItemId() == ASKFOR_NOAD) {
			askForNoAD();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, UPDATE_CONTENT, 1, "刷新一下");
    	menu.add(0, ASKFOR_NOAD, 2, "去除广告");
		return super.onCreateOptionsMenu(menu);
	}
	
	protected void onPause() {
		super.onPause();
		webview.pauseTimers();
		if (isFinishing())
		{
			webview.loadUrl("about:blank");
			setContentView(new FrameLayout(this));
		}
		callHiddenWebViewMethod("onPause"); 
	}
	protected void onResume() {
		super.onResume();
		webSettings.setPluginsEnabled(true);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pcnews_content);
		contentlinear = (LinearLayout)findViewById(R.id.gamecontent_group);
		backimg = (ImageView)findViewById(R.id.gamecontent_previous);
		pagetext = (TextView)findViewById(R.id.gamecontent_current);
		nextimg = (ImageView)findViewById(R.id.gamecontent_next);
		toplayout = (FrameLayout)findViewById(R.id.top_linearcontent);
		bottomlayout = (RelativeLayout)findViewById(R.id.bottom_linearcontent);
		
		FileUtils fileutils = new FileUtils();
        if("noadbyglk".equals(fileutils.readLock2(this))) {
        	LinearLayout ad = (LinearLayout)findViewById(R.id.adViewLayout);
        	ad.setVisibility(View.GONE);
        } else {
        	AdManager.init(this,"adbb1a4eeca5454a", "12e035e70ab831db", 30, false);//有米广告条初始化 
        	LinearLayout adViewLayout = (LinearLayout) findViewById(R.id.adViewLayout); 
        	adViewLayout.addView(new AdView(this), new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 
        			LinearLayout.LayoutParams.WRAP_CONTENT)); //有米广告加载部分 代码
        }
		
		Intent it = getIntent();
		url = it.getCharSequenceExtra("url").toString();
		id = it.getIntExtra("id", 0);
		total = it.getIntExtra("total", 1);
		
		webview = new WebView(this);
		webview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		webSettings = webview.getSettings();    
		webSettings.setSavePassword(false); 
		webSettings.setSaveFormData(false); 
		webSettings.setJavaScriptEnabled(true); 
		webSettings.setSupportZoom(false); 
		webSettings.setPluginsEnabled(true);  
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webSettings.setDefaultTextEncodingName("GBK");
		webview.setWebViewClient(new WebViewClient(){

			public void onPageFinished(WebView view, String url) {
				toplayout.setVisibility(View.VISIBLE);
				bottomlayout.setVisibility(View.VISIBLE);
				super.onPageFinished(view, url);
			}
	    });

//	    if(Spider.HasFlash) {
		toplayout.setVisibility(View.GONE);
		bottomlayout.setVisibility(View.GONE);
//	    }
	    webview.loadUrl(HTML_ROOT+url);
	   
	    contentlinear.addView(webview);
	    backimg.setOnClickListener(this);
	    nextimg.setOnClickListener(this);
	    pagetext.setText("1/"+total);
	}
	
	private Boolean canPage = true;
	private int page = 1;
	public void onClick(View v) {
		if(v == backimg && canPage) {
			canPage = false;
			if(page > 1) {
				PageShow ps = DreamFieldReader.getPageFromJSON(HTML_REQUEST_BACK + pid);
				if(null == ps) {
					canPage = true;
					Toast.makeText(PcNewsContentActivity.this, "网络异常，请等下再试", Toast.LENGTH_LONG).show();
				} else {
					this.id = ps.getId();
					this.pid = ps.getPid();
					this.page = ps.getPageCurrent();
					webview.loadUrl(HTML_ROOT + ps.getHtmlUrl());
					pagetext.setText(page+"/"+total);
					canPage = true;
				}
			} else {
				Toast.makeText(PcNewsContentActivity.this, "前面木有啦", Toast.LENGTH_LONG).show();
				canPage = true;
			}
			//String result = getRsFromDreamField(HTML_REQUEST_BACK + );
		} else if(v == nextimg && canPage) {
			canPage = false;
			if(page < total) {
				PageShow ps = DreamFieldReader.getPageFromJSON(HTML_REQUEST_NEXT + id);
				if(null == ps) {
					canPage = true;
					Toast.makeText(PcNewsContentActivity.this, "网络异常，请等下再试", Toast.LENGTH_LONG).show();
				} else {
					this.id = ps.getId();
					this.pid = ps.getPid();
					this.page = ps.getPageCurrent();
					System.out.println("id--->"+id);
					System.out.println("pid--->"+pid);
					System.out.println("page--->"+page);
					
					webview.loadUrl(HTML_ROOT + ps.getHtmlUrl());
					pagetext.setText(page+"/"+total);
					canPage = true;
				}
			} else {
				Toast.makeText(PcNewsContentActivity.this, "已经到末页啦", Toast.LENGTH_LONG).show();
				canPage = true;
			}
		}
	} 
	
	private void callHiddenWebViewMethod(String name)
	{
		if (webview != null)
		{
		try {
				Method method = WebView.class.getMethod(name);
				method.invoke(webview);
			}
			catch (NoSuchMethodException e)
			{
				Log.i("No such method: " + name, e.toString());
			}
			catch (IllegalAccessException e)
			{
				Log.i("Illegal Access: " + name, e.toString());
			}
			catch (InvocationTargetException e)
			{
				Log.d("Invocation Target Exception: " + name, e.toString());
			}
		}
	}
	
	protected void AdDialog2() {
		AlertDialog.Builder builder = new Builder(PcNewsContentActivity.this);
		builder.setMessage("您的积分为"+YoumiPointsManager.queryPoints(this)+"，30积分可永久去广告，去免费赚积分？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				YoumiOffersManager.showOffers(PcNewsContentActivity.this, 0); //有米广告墙
			}
		});
		builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	protected void SpendDialog2() {
		AlertDialog.Builder builder = new Builder(PcNewsContentActivity.this);
		builder.setMessage("您的积分为"+YoumiPointsManager.queryPoints(this)+"，此操作要消耗30积分，继续吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				FileUtils fileutils = new FileUtils();
				YoumiPointsManager.spendPoints(PcNewsContentActivity.this, 30); //消耗有米积分
				fileutils.writeLock2("noadbyglk", PcNewsContentActivity.this);
				LinearLayout ad = (LinearLayout)PcNewsContentActivity.this.findViewById(R.id.adViewLayout);
	        	ad.setVisibility(View.GONE);
			}
		});
		builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	public void askForNoAD() {
		if(YoumiPointsManager.queryPoints(this) >= 30) {//查询有米积分 是否大于30
			SpendDialog2();
		} else {
			AdDialog2();
		}
	}
}
