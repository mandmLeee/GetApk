package com.mandmlee.getapk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnItemClickListener,
		MultiChoiceModeListener {

	private static final String TAG = "MainActivity";
	private GridView mAppGridView;
	private ArrayList<ResolveInfo> mApps;
	private PackageManager pm;
	private MyAdapter adapter;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pm = getPackageManager();
		mAppGridView = (GridView) findViewById(R.id.appGridView);
		initApp();
		adapter = new MyAdapter(this, mApps);
		mAppGridView.setAdapter(adapter);
		mAppGridView.setOnItemClickListener(this);
		// 上下文菜单
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// 如果API>=11选择操作模式
			mAppGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
			mAppGridView.setMultiChoiceModeListener(this);
		}

	}

	/**
	 * 初始化app列表
	 */
	private void initApp() {
		// 获取android设备的应用列表
		Intent intent = new Intent(Intent.ACTION_MAIN); // 动作匹配
		intent.addCategory(Intent.CATEGORY_LAUNCHER); // 类别匹配
		mApps = (ArrayList<ResolveInfo>) pm.queryIntentActivities(intent, 0);
		// 排序
		Collections.sort(mApps, new Comparator<ResolveInfo>() {

			@Override
			public int compare(ResolveInfo a, ResolveInfo b) {
				// 排序规则
				PackageManager pm = getPackageManager();
				return String.CASE_INSENSITIVE_ORDER.compare(a.loadLabel(pm)
						.toString(), b.loadLabel(pm).toString()); // 忽略大小写
			}
		});

	}

	private class MyAdapter extends ArrayAdapter<ResolveInfo> {

		public MyAdapter(Context context, ArrayList<ResolveInfo> apps) {
			super(context, 0, apps);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {

				convertView = getLayoutInflater().inflate(
						R.layout.item_gridview, null);
				holder = new ViewHolder();
				holder.appImageView = (ImageView) convertView
						.findViewById(R.id.appImageView);
				holder.appNameTextView = (TextView) convertView
						.findViewById(R.id.appNameTextView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			ResolveInfo app = mApps.get(position);
			CharSequence appName = app.loadLabel(pm);
			holder.appNameTextView.setText(appName);
			Drawable appIcon = app.loadIcon(pm);
			holder.appImageView.setImageDrawable(appIcon);
			return convertView;
		}

		private class ViewHolder {
			public ImageView appImageView;
			public TextView appNameTextView;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ResolveInfo app = mApps.get(position);
		String appDir = null;
		try {
			// 指定包名的程序源文件路径
			appDir = getPackageManager().getApplicationInfo(
					app.activityInfo.packageName, 0).sourceDir;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		appDir = "file://" + appDir;
		Uri uri = Uri.parse(appDir);
		// 发送
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.setType("*/*");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(Intent.createChooser(intent, "发送"));
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.list_item_context, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_send:
			ArrayList<Uri> uris = new ArrayList<Uri>(); // uri列表
			for (int i = adapter.getCount() - 1; i >= 0; i--) {
				if (mAppGridView.isItemChecked(i)) {
					ResolveInfo app = mApps.get(i);
					String appDir = null;
					try {
						// 指定包名的程序源文件路径
						appDir = getPackageManager().getApplicationInfo(
								app.activityInfo.packageName, 0).sourceDir;
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
					appDir = "file://" + appDir;
					Uri uri = Uri.parse(appDir);
					uris.add(uri);
				}
			}

			boolean multiple = uris.size() > 1;
			Intent intent = new Intent(multiple ? Intent.ACTION_SEND_MULTIPLE
					: Intent.ACTION_SEND);
			intent.setType("*/*");
			if (multiple) {
				intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			} else {
				intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
			}
			startActivity(Intent.createChooser(intent, "发送"));
			mode.finish();
			return true;

		default:
			return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {

	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {

	}
}
