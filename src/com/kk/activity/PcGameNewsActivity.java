package com.kk.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.dreamfield.parser.DreamFieldReader;
import cn.dreamfield.parser.NewsRes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PcGameNewsActivity extends Activity implements OnClickListener, OnScrollListener, OnItemClickListener {
    /** Called when the activity is first created. */
	private TextView maintext1;
	private TextView maintext2;
	private TextView maintext3;
	private TextView maintext4;
	private TextView maintext5;
	private TextView maintext_top;
	private ImageView main_refresh;
	private LinearLayout mainlinear_middle;
	private ListView firstselectlistview;
	private ArrayList<NewsRes> newsRess = new ArrayList<NewsRes>(); 
	private ExecutorService mainThreadPool = Executors.newFixedThreadPool(5);
	AsyncImageLoader asyncimageloader = new AsyncImageLoader(mainThreadPool);
	
	public static int NEWS_TOTAL = 0;
	private int offset = 0;
	//private int page = 0;
	private int rows = 8;
	private String category = "pcnews";
	
	private static int UPDATE_LIST = 1;
	private static int ABOUT = 2;
	private static int EXIT = 3;
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId() == UPDATE_LIST) {
			showProgress();
			getNews();
		} else if(item.getItemId() == ABOUT) {
			Toast.makeText(PcGameNewsActivity.this, "梦野小组出品  v1.4 QQ:328184052", Toast.LENGTH_LONG).show();
		} else if(item.getItemId() == EXIT) {
			PcGameNewsActivity.this.finish();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, UPDATE_LIST, 1, "刷新");
    	menu.add(0, ABOUT, 2, "关于");
    	menu.add(0, EXIT, 3, "退出");
		return super.onCreateOptionsMenu(menu);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pcgamenews);
        
        maintext1 = (TextView)findViewById(R.id.maintext1);
        maintext2 = (TextView)findViewById(R.id.maintext2);
        maintext3 = (TextView)findViewById(R.id.maintext3);
        maintext4 = (TextView)findViewById(R.id.maintext4);
        maintext5 = (TextView)findViewById(R.id.maintext5);
        main_refresh = (ImageView)findViewById(R.id.mainrefresh_top);
        maintext_top = (TextView)findViewById(R.id.maintext_top);
        mainlinear_middle = (LinearLayout)findViewById(R.id.main_linear_middle);   
        
        maintext1.setOnClickListener(this);
        maintext2.setOnClickListener(this);
        maintext3.setOnClickListener(this);
        maintext4.setOnClickListener(this);
        maintext5.setOnClickListener(this);
        main_refresh.setOnClickListener(this);
        showProgress();
        getNews();
    }
    
    public void updateNews() {
    	//page ++;
    	offset = offset + rows;
    	if(offset >= NEWS_TOTAL) {
    		more.setText("木有更多了哦！");
    	} else {
    		showProgress();
    		Thread t = new Thread(new ImageDownloadThread2());
    		t.start();
    	}
    }
    
    private FirstSelectAdapter adapter;
    private LinearLayout listBottom;
    private TextView more;
    public void getNews() {
    	offset = 0;
    	//page = 0;
    	rows = 8;
    	mainlinear_middle.removeAllViews();
    	newsRess.clear();
    	LinearLayout linearList = (LinearLayout)this.getLayoutInflater().inflate(R.layout.firstselect_listview, null);
    	linearList.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    	listBottom = (LinearLayout)this.getLayoutInflater().inflate(R.layout.list_bottom, null);
    	more = (TextView)listBottom.findViewById(R.id.more_list);
    	more.setText("查看更多");
    	mainlinear_middle.addView(linearList);
    	firstselectlistview = (ListView)findViewById(R.id.cartoonnews_list);
    	firstselectlistview.addFooterView(listBottom);
    	firstselectlistview.setOnScrollListener(this);
    	firstselectlistview.setOnItemClickListener(this);
    	Thread t = new Thread(new ImageDownloadThread());
    	t.start();
    }
    class ImageDownloadThread implements Runnable {
		public void run() {
			try {
				String url = "http://dreamfield.cn/lib/searchnews.php?category="+category+"&offset="+offset+"&rows="+rows;
				DreamFieldReader.getNewResFromJSON(url, newsRess);
	    	} catch (Exception e) {
	    		netErrorHandler.sendEmptyMessage(0);
	    	}
	    	firstselecthandler.sendEmptyMessage(0);
		}
    }
    class ImageDownloadThread2 implements Runnable {
		public void run() {
			try {
				String url = "http://dreamfield.cn/lib/searchnews.php?category="+category+"&offset="+offset+"&rows="+rows;
				DreamFieldReader.getNewResFromJSON(url, newsRess);
			} catch (Exception e) {
				netErrorHandler.sendEmptyMessage(0);
	    	}
	    	firstselecthandler2.sendEmptyMessage(0);
		}
    }
    Handler firstselecthandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			adapter = new FirstSelectAdapter(PcGameNewsActivity.this, asyncimageloader, firstselectlistview);
			adapter.setItems(newsRess);
			firstselectlistview.setAdapter(adapter);
	        hideProgress();
		}
    };
    Handler firstselecthandler2 = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			adapter.setItems(newsRess);
			adapter.notifyDataSetChanged();
	        hideProgress();
		}
    };
    Handler netErrorHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(PcGameNewsActivity.this, "网络异常...请稍后再试", Toast.LENGTH_LONG).show();
		}
    };
    
	public void onClick(View v) {
		showProgress();
		if(v == maintext1) {
			maintext1.setBackgroundResource(R.drawable.shine);
			maintext2.setBackgroundResource(0);
			maintext3.setBackgroundResource(0);
			maintext4.setBackgroundResource(0);
			maintext5.setBackgroundResource(0);	
			maintext_top.setText("单机游戏资讯");
			this.category = "pcnews";
			getNews();
		} else if(v == maintext2) {
			maintext2.setBackgroundResource(R.drawable.shine);
			maintext1.setBackgroundResource(0);
			maintext3.setBackgroundResource(0);
			maintext4.setBackgroundResource(0);
			maintext5.setBackgroundResource(0);
			maintext_top.setText("电视游戏资讯");
			this.category = "tvnews";
			getNews();
		} else if(v == maintext3) {
			maintext3.setBackgroundResource(R.drawable.shine);
			maintext1.setBackgroundResource(0);
			maintext2.setBackgroundResource(0);
			maintext4.setBackgroundResource(0);
			maintext5.setBackgroundResource(0);
			maintext_top.setText("手机游戏资讯");
			this.category = "phonenews";
			getNews();
		} else if(v == maintext4) {
			maintext4.setBackgroundResource(R.drawable.shine);
			maintext1.setBackgroundResource(0);
			maintext2.setBackgroundResource(0);
			maintext3.setBackgroundResource(0);
			maintext5.setBackgroundResource(0);
			maintext_top.setText("游戏测评");
			this.category = "gametest";
			getNews();
		} else if(v == maintext5) {
			maintext5.setBackgroundResource(R.drawable.shine);
			maintext1.setBackgroundResource(0);
			maintext2.setBackgroundResource(0);
			maintext3.setBackgroundResource(0);
			maintext4.setBackgroundResource(0);
			maintext_top.setText("游戏前瞻");
			this.category = "gamefuture";
			getNews();
		} else if(v == main_refresh) {
			getNews();
		}
	}
	
	/**
	 * 进度条
	 */
	private ProgressDialog progressDialog;
	private List<ProgressDialog> progressDialoglist = new ArrayList<ProgressDialog>();
	
	/**
	 * 显示进度条这一块字符串还是大家自己手动在SuperListView上改吧，就不做接口了
	 */
	private void showProgress() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在加载...");
		progressDialog.show();
		progressDialoglist.add(progressDialog);
	}

	private void hideProgress() {
		if (progressDialoglist.size() > 0) {
			progressDialog = progressDialoglist.get(0);
			if (progressDialog != null) {
				progressDialog.cancel();

			}
			progressDialoglist.remove(0);
		}
	}
	private int lastItem;
	//private int firstItem;
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount - 1; 
		//firstItem = firstVisibleItem; 
		//System.out.println("firstItem=" + firstItem); 
		//System.out.println("lastItem=" + lastItem); 
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//System.out.println("scrollState=" + scrollState);
		if(lastItem >= NEWS_TOTAL || lastItem >= (offset + rows)) {
			if(scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_FLING) {
				updateNews();
			}
		}
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		ListView listView = (ListView)arg0;
		System.out.println("POSITION-->" + position);
		if(position == NEWS_TOTAL) {
			updateNews();
			return;
		}
		NewsRes ele = (NewsRes)listView.getItemAtPosition(position);
		String url = ele.getUrl();
		int id = Integer.parseInt(ele.getId());
		int total = ele.getPageTotal();
		//Toast.makeText(PcGameNewsActivity.this, "TEST....", Toast.LENGTH_LONG).show();
		Intent it = new Intent();
		it.setClass(PcGameNewsActivity.this, PcNewsContentActivity.class);
		it.putExtra("url", url);
		it.putExtra("id", id);
		it.putExtra("total", total);
		
		PcGameNewsActivity.this.startActivity(it);
	}
	
	protected void dialog() {
		AlertDialog.Builder builder = new Builder(PcGameNewsActivity.this);
		builder.setMessage("确认退出吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				PcGameNewsActivity.this.finish();
			}
		});
		builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {  
            dialog();  
            return true;  
        }  
        return super.onKeyDown(keyCode, event);
    }
}