package com.kk.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 
 * @author sun.shine
 * 
 * @param <K>
 */
@SuppressWarnings(value = "unchecked")
public class SuperListView<K> extends ListView {
	int layoutId;

	/**
	 * 更新list的线程
	 */
	HandlerThread ListThread;
	/**
	 * 更新list线程的锁
	 */
	public Object listLock = new Object();
	/**
	 * 更新Image线程的锁
	 */
	public Object imageLock = new Object();
	/**
	 * 更新图片的线程
	 */
	HandlerThread ImageThread;

	/**
	 * 进度条
	 */
	List<ProgressDialog> progressDialoglist = new ArrayList<ProgressDialog>();

	/**
	 * 处理list的handler
	 */
	Handler listHandler;
	/**
	 * 处理image的handler
	 */
	Handler imageHandler;
	/**
	 * 主线程的handler
	 */
	Handler mHandler;

	/**
	 * 进度条
	 */
	ProgressDialog progressDialog;

	public SuperListView(Context context) {
		super(context);
	}

	public SuperListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		ListThread = new HandlerThread("list-update");
		ImageThread = new HandlerThread("image-update");
		ListThread.start();
		ImageThread.start();
		listHandler = new Handler(ListThread.getLooper());
		imageHandler = new Handler(ImageThread.getLooper());
		mHandler = new Handler();
		init();
	}

	/**
	 * 显示进度条这一块字符串还是大家自己手动在SuperListView上改吧，就不做接口了
	 */
	private void showProgress() {

		progressDialog = new ProgressDialog(getContext());
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

	/**
	 * 通过此方法把List加到ListView 中去
	 * 
	 * @param list
	 */
	public void setList(List<K> list) {

		if (getAdapter() == null) {
			SuperListAdapter<K> superListAdapter = new SuperListAdapter<K>(
					getContext());
			this.setAdapter(superListAdapter);
		}

		SuperListAdapter<K> superListAdapter = (SuperListAdapter<K>) getAdapter();
		superListAdapter.setList(list);

	}

	/**
	 * 通过此方法来设置ListView的Item，目前不支持通过java代码来new View
	 * 
	 * @param id
	 */
	public void setView(int id) {
		layoutId = id;
	}

	/**
	 * 此方法设置helper从而可以达到实现异步的目的，如果没有调用此方法，那么所有的异步操作将都被取消
	 * 
	 * @param adapterHelper
	 */
	public void setAdapterHelper(SuperListViewAdapterHelper adapterHelper) {
		SuperListAdapter<K> superListAdapter = (SuperListAdapter) getAdapter();
		superListAdapter.setAdapterHelper(adapterHelper);

	}

	/**
	 * 
	 * 请不要调用此方法
	 */
	public void setAdapter(ListAdapter adapter) {
		// super.setAdapter(adapter);
		throw new RuntimeException("不能自己设置Adapter");
	}

	/**
	 * 初始化所有数据
	 */
	private void init() {
		// 为listView 注入Adapter,注用户不能为superListView注册Adapter。这样会失效
		if (getAdapter() == null) {
			SuperListAdapter<K> superListAdapter = new SuperListAdapter<K>(
					getContext());
			super.setAdapter(superListAdapter);
		}
		// 引入手势类，该 类主要是监听用户的触屏手势，来获得手势信息，用来协助判断是否滑到最底，如果滑到最底，就提示该更新数据了
		final SuperListOnGestureListener superListOnGestureListener = new SuperListOnGestureListener();
		final GestureDetector gestureDetector = new GestureDetector(
				superListOnGestureListener);

		this.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return gestureDetector.onTouchEvent(event);
			}
		});

		this.setOnScrollListener(new OnScrollListener() {

			/**
			 * 
			 * 0: scroll end,1: start 2:ing
			 */
			int scrollState;

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

				this.scrollState = scrollState;
				// LogManager.log_cycle("ononScrollStateChanged" + scrollState);
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				// System.out.println("totalItemCount=" + totalItemCount);
				if (totalItemCount == 0) {
					return;
				}
				if (superListOnGestureListener.distanceY > 0
						&& scrollState == 1
						&& firstVisibleItem + visibleItemCount >= totalItemCount) {
					scrollState = 0;

					final SuperListAdapter<K> adapter = (SuperListAdapter<K>) getAdapter();
					if (adapter.getAdapterHelper() != null) {

						final List<K> oldList = adapter.getList();
						showProgress();
						System.out.println("showProgresss--------");
						listHandler.post(new Runnable() {

							public void run() {
								// TODO Auto-generated method stub
								List<K> newList = adapter.getAdapterHelper()
										.updateList(oldList, listLock);
								synchronized (listLock) {

									try {
										System.out
												.println("listLock wait......");
										listLock.wait();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									System.out.println("listLock awake......");
									oldList.addAll(newList);
									if (newList != null) {

										mHandler.post(new Runnable() {

											public void run() {
												// TODO Auto-generated method
												// stub
												// adapter.setList(newList);
												adapter.notifyDataSetChanged();
												System.out.println(adapter
														.getCount());
												System.out.println(adapter
														.getList().size());
												hideProgress();
											}
										});
									}
								}
							}
						});

					}
					System.out.println("need update data");

				}
			}
		});

	}

	public class SuperListAdapter<E> extends BaseAdapter {

		/**
		 * 协助更新image用的
		 */
		Map<View, Integer> currentViewMap = new HashMap<View, Integer>();
		List<E> list;
		Context context;
		SuperListViewAdapterHelper<E> adapterHelper;

		private List<E> getList() {
			return list;
		}

		private SuperListViewAdapterHelper<E> getAdapterHelper() {
			return adapterHelper;
		}

		private void setList(List<E> list) {
			this.list = list;
		}

		private void setAdapterHelper(
				SuperListViewAdapterHelper<E> adapterHelper) {
			this.adapterHelper = adapterHelper;
		}

		private SuperListAdapter(Context context) {

			this.context = context;
			System.out.println("this.context" + this.context);
		}

		public int getCount() {
			// TODO Auto-generated method stub

			if (list == null) {
				return 0;
			}

			return list.size();
		}

		public E getItem(int position) {
			// TODO Auto-generated method stub
			if (list == null) {
				return null;
			}
			return list.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(final int position, View convertView,
				final ViewGroup parent) {

			Log.i("cmcc", "getView" + position + convertView + parent);
			if (convertView == null) {
				System.out.println("convertView=null new a view");
				LayoutInflater layoutInflater = LayoutInflater
						.from(this.context);

				View layout = layoutInflater.inflate(layoutId, null);
				convertView = layout;
				// RelativeLayout relativeLayout=
			} else {

			}
			currentViewMap.put(convertView, position);
			final View view = convertView;
			if (this.adapterHelper != null) {

				boolean needUpdate = this.adapterHelper.updateViews(list,
						position, convertView, parent);
				if (needUpdate) {

					imageHandler.postDelayed(new Runnable() {

						public void run() {
							// TODO Auto-generated method stub

							System.out.println("wait image..........");

							if (currentViewMap.get(view) != position) {
								return;
							}
							adapterHelper.updateImageViewResouce(
									list.get(position), imageLock);

							synchronized (imageLock) {
								try {

									imageLock.wait();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();

								}
							}
							int t = currentViewMap.get(view);
							if (t != position) {
								return;
							}
							System.out.println("awake.................");
							mHandler.post(new Runnable() {

								public void run() {
									// TODO Auto-generated method stub
									System.out.println("download Image is set");

									adapterHelper.refreshImageView(
											list.get(position), view);
								}
							});

						}
					}, 50);
				}
			}

			return convertView;

		}
	}

	class SuperListOnGestureListener implements OnGestureListener {

		/**
		 * 当>0时，表示在往下滑，用来协助是否需要download数据
		 */
		public float distanceY;

		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

		public void onShowPress(MotionEvent e) {

		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			this.distanceY = distanceY;
			return false;
		}

		public void onLongPress(MotionEvent e) {

		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}

		public boolean onDown(MotionEvent e) {
			return false;
		}
	}

	public interface SuperListViewAdapterHelper<K> {
		/**
		 * 此方法是被 adapter里的getView所调用，因为getView已经被写入很多代理代码（主要是更新图片）用到的，
		 * 该方法的目的就是通过List的position,更新ui,getView里完全不知道您给的View是什么结构，所以在此方法里，
		 * 您可以像在getView方法里一样写一些更新listView Item 的Ui
		 * 
		 * @see BaseAdapter.getView
		 * @param list
		 * @param position
		 * @param convertView
		 * @param parent
		 * @return
		 * @see android.widget.BaseAdapter#getView
		 */
		boolean updateViews(List<K> list, int position, View convertView,
				ViewGroup parent);

		/**
		 * 此方法是用来更新list数据的，当数据被滚到最末端，用户再往下滑的时候，此方法会被调用，通过该方法会需要生成一个List，
		 * 也就是新加载进来的List，该
		 * 方法被调用之后会显示进度条提示正在加载数据，并且imageLock会处于wait状态，此是downLoad数据您可以再另一个线程里下载
		 * ,也就是此方法的形参lock,只要数据下载完成就可以调用lock.notify，通知ListView更新ui.
		 * 
		 * @param oldList
		 *            此参数是为了联网下数据时，为该下什么数据提供依据
		 * @param lock
		 *            通过此锁来通知主线程数据已经下载完成，可以更新ui了
		 * @return
		 */
		List<K> updateList(List<K> oldList, Object lock);

		/**
		 * 该方法需要在其它线程里执行，同样提供一个锁lock让用户下载完成之后调用lock.notify,如果不调用的话，Ui那边会一直不更新，
		 * 会带来严重的后果
		 * 
		 * @param k
		 *            model实体对象
		 * @param lock
		 */
		void updateImageViewResouce(K k, Object lock);

		/**
		 * 当得知图片已经下载完成，主线程调用此方法通知用户更新图片，此方法是被异步调用执行的
		 * 
		 * @param k
		 * @param contentView
		 */
		void refreshImageView(K k, View contentView);

	}

}
