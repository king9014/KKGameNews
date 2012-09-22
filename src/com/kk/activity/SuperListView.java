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
	 * ����list���߳�
	 */
	HandlerThread ListThread;
	/**
	 * ����list�̵߳���
	 */
	public Object listLock = new Object();
	/**
	 * ����Image�̵߳���
	 */
	public Object imageLock = new Object();
	/**
	 * ����ͼƬ���߳�
	 */
	HandlerThread ImageThread;

	/**
	 * ������
	 */
	List<ProgressDialog> progressDialoglist = new ArrayList<ProgressDialog>();

	/**
	 * ����list��handler
	 */
	Handler listHandler;
	/**
	 * ����image��handler
	 */
	Handler imageHandler;
	/**
	 * ���̵߳�handler
	 */
	Handler mHandler;

	/**
	 * ������
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
	 * ��ʾ��������һ���ַ������Ǵ���Լ��ֶ���SuperListView�ϸİɣ��Ͳ����ӿ���
	 */
	private void showProgress() {

		progressDialog = new ProgressDialog(getContext());
		progressDialog.setMessage("���ڼ���...");
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
	 * ͨ���˷�����List�ӵ�ListView ��ȥ
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
	 * ͨ���˷���������ListView��Item��Ŀǰ��֧��ͨ��java������new View
	 * 
	 * @param id
	 */
	public void setView(int id) {
		layoutId = id;
	}

	/**
	 * �˷�������helper�Ӷ����Դﵽʵ���첽��Ŀ�ģ����û�е��ô˷�������ô���е��첽����������ȡ��
	 * 
	 * @param adapterHelper
	 */
	public void setAdapterHelper(SuperListViewAdapterHelper adapterHelper) {
		SuperListAdapter<K> superListAdapter = (SuperListAdapter) getAdapter();
		superListAdapter.setAdapterHelper(adapterHelper);

	}

	/**
	 * 
	 * �벻Ҫ���ô˷���
	 */
	public void setAdapter(ListAdapter adapter) {
		// super.setAdapter(adapter);
		throw new RuntimeException("�����Լ�����Adapter");
	}

	/**
	 * ��ʼ����������
	 */
	private void init() {
		// ΪlistView ע��Adapter,ע�û�����ΪsuperListViewע��Adapter��������ʧЧ
		if (getAdapter() == null) {
			SuperListAdapter<K> superListAdapter = new SuperListAdapter<K>(
					getContext());
			super.setAdapter(superListAdapter);
		}
		// ���������࣬�� ����Ҫ�Ǽ����û��Ĵ������ƣ������������Ϣ������Э���ж��Ƿ񻬵���ף����������ף�����ʾ�ø���������
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
		 * Э������image�õ�
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
		 * ��>0ʱ����ʾ�����»�������Э���Ƿ���Ҫdownload����
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
		 * �˷����Ǳ� adapter���getView�����ã���ΪgetView�Ѿ���д��ܶ������루��Ҫ�Ǹ���ͼƬ���õ��ģ�
		 * �÷�����Ŀ�ľ���ͨ��List��position,����ui,getView����ȫ��֪��������View��ʲô�ṹ�������ڴ˷����
		 * ����������getView������һ��дһЩ����listView Item ��Ui
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
		 * �˷�������������list���ݵģ������ݱ�������ĩ�ˣ��û������»���ʱ�򣬴˷����ᱻ���ã�ͨ���÷�������Ҫ����һ��List��
		 * Ҳ�����¼��ؽ�����List����
		 * ����������֮�����ʾ��������ʾ���ڼ������ݣ�����imageLock�ᴦ��wait״̬������downLoad��������������һ���߳�������
		 * ,Ҳ���Ǵ˷������β�lock,ֻҪ����������ɾͿ��Ե���lock.notify��֪ͨListView����ui.
		 * 
		 * @param oldList
		 *            �˲�����Ϊ������������ʱ��Ϊ����ʲô�����ṩ����
		 * @param lock
		 *            ͨ��������֪ͨ���߳������Ѿ�������ɣ����Ը���ui��
		 * @return
		 */
		List<K> updateList(List<K> oldList, Object lock);

		/**
		 * �÷�����Ҫ�������߳���ִ�У�ͬ���ṩһ����lock���û��������֮�����lock.notify,��������õĻ���Ui�Ǳ߻�һֱ�����£�
		 * ��������صĺ��
		 * 
		 * @param k
		 *            modelʵ�����
		 * @param lock
		 */
		void updateImageViewResouce(K k, Object lock);

		/**
		 * ����֪ͼƬ�Ѿ�������ɣ����̵߳��ô˷���֪ͨ�û�����ͼƬ���˷����Ǳ��첽����ִ�е�
		 * 
		 * @param k
		 * @param contentView
		 */
		void refreshImageView(K k, View contentView);

	}

}
