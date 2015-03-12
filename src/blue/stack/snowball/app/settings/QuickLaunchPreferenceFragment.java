package blue.stack.snowball.app.settings;

import java.util.List;

import blue.stack.snowball.app.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
//import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import blue.stack.snowball.app.apps.App;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.core.GuiceModule;

import com.google.inject.Inject;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class QuickLaunchPreferenceFragment extends ListFragment {
	ArrayAdapter<App> adapter;
	@Inject
	AppManager appManager;
	private List<App> array;
	public boolean dragEnabled;
	public int dragStartMode;
	private DragSortController mController;
	private DragSortListView mDslv;
	private DropListener onDrop;
	private RemoveListener onRemove;
	public boolean removeEnabled;
	public int removeMode;
	@Inject
	Settings settings;
	public boolean sortEnabled;

	private static class AppAdapter extends ArrayAdapter<App> {
		Context context;
		List<App> data;
		int layoutResourceId;

		static class Holder {
			ImageView imgIcon;
			TextView txtTitle;

			Holder() {
			}
		}

		public AppAdapter(Context context, int layoutResourceId, List<App> data) {
			super(context, layoutResourceId, data);
			this.data = null;
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.data = data;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder;
			View row = convertView;
			if (row == null) {
				row = ((Activity) this.context).getLayoutInflater().inflate(this.layoutResourceId, parent, false);
				holder = new Holder();
				holder.imgIcon = (ImageView) row.findViewById(R.id.image);
				holder.txtTitle = (TextView) row.findViewById(R.id.text);
				row.setTag(holder);
			} else {
				holder = (Holder) row.getTag();
			}
			App app = this.data.get(position);
			holder.txtTitle.setText(app.getAppName());
			holder.imgIcon.setImageDrawable(app.getAppIcon());
			return row;
		}
	}

	public QuickLaunchPreferenceFragment() {
		this.onDrop = new DropListener() {
			@Override
			public void drop(int from, int to) {
				if (from != to) {
					App item = QuickLaunchPreferenceFragment.this.adapter.getItem(from);
					QuickLaunchPreferenceFragment.this.adapter.remove(item);
					QuickLaunchPreferenceFragment.this.adapter.insert(item, to);
					QuickLaunchPreferenceFragment.this.appManager
							.setQuickLaunchApps(QuickLaunchPreferenceFragment.this.array);
				}
			}
		};
		this.onRemove = new RemoveListener() {
			@Override
			public void remove(int which) {
				QuickLaunchPreferenceFragment.this.adapter.remove(QuickLaunchPreferenceFragment.this.adapter
						.getItem(which));
			}
		};
		this.dragStartMode = 0;
		this.removeEnabled = false;
		this.removeMode = 1;
		this.sortEnabled = true;
		this.dragEnabled = true;
		GuiceModule.get().injectMembers(this);
	}

	protected int getLayout() {
		return R.layout.dslv_fragment_main;
	}

	protected int getItemLayout() {
		return R.layout.quicklaunch_list_item;
	}

	public DragSortController getController() {
		return this.mController;
	}

	public void setListAdapter() {
		this.array = this.appManager.getQuickLaunchApps();
		this.adapter = new AppAdapter(getActivity(), getItemLayout(), this.array);
		setListAdapter(this.adapter);
	}

	public DragSortController buildController(DragSortListView dslv) {
		DragSortController controller = new DragSortController(dslv);
		controller.setDragHandleId(R.id.drag_handle);
		controller.setRemoveEnabled(this.removeEnabled);
		controller.setSortEnabled(this.sortEnabled);
		controller.setDragInitMode(this.dragStartMode);
		controller.setRemoveMode(this.removeMode);
		return controller;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.mDslv = (DragSortListView) inflater.inflate(getLayout(), container, false);
		addHeader(getActivity(), this.mDslv);
		this.mController = buildController(this.mDslv);
		this.mDslv.setFloatViewManager(this.mController);
		this.mDslv.setOnTouchListener(this.mController);
		this.mDslv.setDragEnabled(this.dragEnabled);
		this.mDslv.setDividerHeight(2);
		return this.mDslv;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.mDslv = (DragSortListView) getListView();
		this.mDslv.setDropListener(this.onDrop);
		this.mDslv.setRemoveListener(this.onRemove);
		Bundle args = getArguments();
		setListAdapter();
	}

	public static void addHeader(Activity activity, DragSortListView dslv) {
		LayoutInflater inflater = activity.getLayoutInflater();
		int count = dslv.getHeaderViewsCount();
		dslv.addHeaderView(inflater.inflate(R.layout.quicklaunch_header, null), null, false);
	}
}
