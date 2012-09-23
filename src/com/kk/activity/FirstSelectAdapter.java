package com.kk.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.kk.activity.AsyncImageLoader.ImageCallback;

import cn.dreamfield.parser.NewsRes;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;



public class FirstSelectAdapter extends BaseAdapter{
	private static String URL_ROOT = "http://dreamfield.cn/lib/ico/";
	
	private LayoutInflater inflater;
	private ArrayList<NewsRes> items;
	private ListView listView;
	AsyncImageLoader asyncimageloader;
	
	public FirstSelectAdapter(Context context, AsyncImageLoader asyncimageloader, ListView listView) {
		inflater = LayoutInflater.from(context);
		this.asyncimageloader = asyncimageloader;
		this.listView = listView;
	}
	
	public void setItems(ArrayList<NewsRes> items){		
		this.items = items;
	}

	public int getCount() {
		return items.size();
	}

	public Object getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	class ViewHolder {
		private ImageView icon;
		private TextView title;
		private TextView lookp;
		private TextView date;
		private View baseView;
		public ViewHolder(View baseView) {  
			this.baseView = baseView;  
	    }  
		public ImageView getImageView() {  
            if (icon == null) {  
            	icon = (ImageView) baseView.findViewById(R.id.firstselect_listicon);  
            }  
            return icon;  
        }
		
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if(convertView == null){
			convertView = inflater.inflate(R.layout.firstselect_listitem, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
			holder.title= (TextView) convertView.findViewById(R.id.firstselect_listtitle);
			holder.lookp = (TextView) convertView.findViewById(R.id.firstselect_listlookp);
			holder.date = (TextView) convertView.findViewById(R.id.firstselect_listdate);
			convertView.setTag(holder);
		}else{
			holder =(ViewHolder)convertView.getTag();
		}
		ImageView imageView = holder.getImageView();  
		if(null != items.get(position).getImgUrl()) {
	        imageView.setTag(URL_ROOT+items.get(position).getImgUrl()); 
			
			Drawable cachedImage = asyncimageloader.loadDrawable(URL_ROOT+items.get(position).getImgUrl(), imageView,  
		            new ImageCallback() {  
		                public void imageLoaded(Drawable imageDrawable, ImageView imageView, String imageUrl) {
		                	ImageView imageViewByTag = (ImageView) listView.findViewWithTag(imageUrl);  
		                    if (imageViewByTag != null) {  
		                        imageViewByTag.setBackgroundDrawable(imageDrawable);  
		                    } 
		                    //imageView.setBackgroundDrawable(imageDrawable);  
		                }  
		            });
			if(cachedImage == null) {
				imageView.setBackgroundResource(R.drawable.app_thumb_default_80_60);
			} else {
				imageView.setBackgroundDrawable(cachedImage);
			}
		} else {
			imageView.setBackgroundResource(R.drawable.app_thumb_default_80_60);
			//LinearLayout l = (LinearLayout)convertView.findViewById(R.id.firstselect_listitem);
			//l.setVisibility(View.GONE);
		}
		holder.title.setText(items.get(position).getTitle());
		holder.lookp.setText(items.get(position).getIntro());
		holder.date.setText("Ê±¼ä:"+items.get(position).getDate());
		return convertView;
	}
	
	public static Drawable loadImageFromUrl(String url) {
        URL m;
        InputStream i = null;
        try {
            m = new URL(url);
            i = (InputStream) m.getContent();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Drawable d = Drawable.createFromStream(i, "src");
        return d;
    }

}