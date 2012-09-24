package com.kk.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class AsyncImageLoader {
    // SoftReference�������ã���Ϊ�˸��õ�Ϊ��ϵͳ���ձ���
    private HashMap<String, SoftReference<Drawable>> imageCache;
    private ExecutorService mainThreadPool;
 
    public AsyncImageLoader(ExecutorService mainThreadPool) {
        imageCache = new HashMap<String, SoftReference<Drawable>>();
        this.mainThreadPool = mainThreadPool;
    }
 
    public Drawable loadDrawable(final String imageUrl, final ImageView imageView,
            final ImageCallback imageCallback) {
        if (imageCache.containsKey(imageUrl)) {
            // �ӻ����л�ȡ
            SoftReference<Drawable> softReference = imageCache.get(imageUrl);
            Drawable drawable = softReference.get();
            if (drawable != null) {
                return drawable;
            }
        }
        String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
		final String path = SDCardRoot + "DreamField" + File.separator + "imgCache" + File.separator;
        // �ӱ����ļ��ж�ȡ
		Drawable drawable = loadImageFromFile(path, imageUrl);
        if (drawable != null) {
            return drawable;
        }
        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                imageCallback.imageLoaded((Drawable) message.obj, imageView, imageUrl);
            }
        };
        // ����һ���µ��߳�����ͼƬ
        Thread t = new Thread() {
            @Override
            public void run() {
                Drawable drawable = loadImageFromUrl(imageUrl);
                if(null != drawable) {
	                imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
	                Message message = handler.obtainMessage(0, drawable);
	                handler.sendMessage(message);
	                exportImage2File(imageUrl, drawable);
                }
            }
        };
        mainThreadPool.execute(t);
        return null;
    }
    
    /**
     * ��Drawableд�뵽�ļ�
     * @param url
     * @param drawable
     */
    public void exportImage2File(String url, Drawable drawable) {
    	FileUtils fileutils = new FileUtils();
    	fileutils.creatSDDir("DreamField" + File.separator + "imgCache");
    	Bitmap bitmap = drawableToBitmap(drawable);
    	String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    	File file = new File(SDCardRoot + "DreamField" + File.separator + "imgCache" + File.separator + MD5Encode(url) + ".png");
    	FileOutputStream fos = null;
    	try {
			fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);// ��ͼƬд�뵽�ļ�
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }

    /**
     * ��Drawableת��ΪBitmap
     * @param drawable
     * @return
     */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(
			drawable.getIntrinsicWidth(),
			drawable.getIntrinsicHeight(),
			drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
			: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}
    /**
     * ��ȡ�ļ��е�pngͼƬ
     * @param dir
     * @param url
     * @return
     */
    public Drawable loadImageFromFile(String dir, String url) {
    	String path = dir + MD5Encode(url) + ".png";
    	File mfile = new File(path);
        if (mfile.exists()) { // �����ļ�����
	        Bitmap bm = BitmapFactory.decodeFile(path);
	        return new BitmapDrawable(bm);
        }
    	return null;
    }
    /**
     * 16λMD5����
     * @param plainText
     * @return
     */
    public String MD5Encode(String plainText) {
    	MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
    	md.update(plainText.getBytes());
	    byte b[] = md.digest();
	    int i;
	    StringBuffer buf = new StringBuffer("");
	    for (int offset = 0; offset < b.length; offset++) {
	    	i = b[offset];
	    	if (i < 0)
	    		i += 256;
	    	if (i < 16)
	    		buf.append("0");
	    	buf.append(Integer.toHexString(i));
	    }
	    //System.out.println("result: " + buf.toString().substring(8, 24));// 16λ�ļ���
    	return buf.toString().substring(8, 24);
    }
 
    public static Drawable loadImageFromUrl(String url) {
        URL m;
        InputStream i = null;
        try {
            m = new URL(url);
            i = (InputStream) m.getContent();
        } catch (MalformedURLException e1) {
            System.out.println(e1);
        } catch (IOException e) {
        	System.out.println(e);
        }
        Drawable d = Drawable.createFromStream(i, "src");
        return d;
    }
 
    // �ص��ӿ�
    public interface ImageCallback {
        public void imageLoaded(Drawable imageDrawable, ImageView imageView, String imageUrl);
    }
}
