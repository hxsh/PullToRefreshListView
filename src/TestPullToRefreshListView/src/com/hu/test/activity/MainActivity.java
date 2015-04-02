package com.hu.test.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hu.pulltorefresh.view.PullToRefreshListView;
import com.hu.pulltorefresh.view.PullToRefreshListView.OnRefreshListener;
import com.hu.test.R;

public class MainActivity extends Activity implements OnRefreshListener, OnItemClickListener {
	
	private PullToRefreshListView mListView;
	private MyAdapter mAdapter;
	private int mItemCount = 50;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mListView = (PullToRefreshListView) findViewById(R.id.list);
		
		mListView.setOnRefreshListener(this);
		mListView.setOnItemClickListener(this);
		
		mListView.setMode(PullToRefreshListView.MODE_REFRESH);
		mAdapter = new MyAdapter();
		mListView.setAdapter(mAdapter);
		
	}
	
	final class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mItemCount;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_main, null, false);
				holder = new ViewHolder();
				holder.mTextView = (TextView) convertView.findViewById(R.id.tv);
				holder.mBtn = (Button) convertView.findViewById(R.id.btn);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.mTextView.setText(getString(R.string.text_position, position));
			holder.mBtn.setOnClickListener(new ClickListener(position));
			return convertView;
		}

	}
	
	final class ClickListener implements OnClickListener{
		private int mPosition;
		
		public ClickListener(int position){
			mPosition = position;
		}

		@Override
		public void onClick(View v) {
			Toast.makeText(MainActivity.this, "click position : " + mPosition, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	final class ViewHolder{
		TextView mTextView;
		Button mBtn;
	}

	@Override
	public void onRefresh() {
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mListView.onRefreshComplete();
				mItemCount = mItemCount + 10;
				mAdapter.notifyDataSetChanged();
				mListView.setSelection(10);
			}
		}, 2000l);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Toast.makeText(this, getString(R.string.click_position, position), Toast.LENGTH_SHORT).show();
	}

}
