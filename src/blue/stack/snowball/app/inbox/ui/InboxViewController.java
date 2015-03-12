package blue.stack.snowball.app.inbox.ui;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.TextView;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.ui.ExpandableListView;

import com.google.inject.Inject;

public abstract class InboxViewController {
    static String TAG;
    private Context context;
    protected View inboxView;
    private ExpandableListView listView;

    protected abstract int geLayoutId();

    static {
        TAG = "InboxViewController";
    }

    @Inject
    protected InboxViewController(Context context) {
        this.context = context;
        GuiceModule.get().injectMembers(this);
        createView();
    }

    public void createView() {
        this.inboxView = ((LayoutInflater) this.context.getSystemService("layout_inflater")).inflate(geLayoutId(), null);
        this.listView = (ExpandableListView) this.inboxView.findViewById(R.id.swipe_side_listview);
        this.listView.setEmptyView(this.inboxView.findViewById(R.id.inbox_empty_view));
        InboxViewManager inboxViewManager = (InboxViewManager) GuiceModule.get().getInstance(InboxViewManager.class);
        this.inboxView.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return false;
            }
        });
    }

    public View getView() {
        return this.inboxView;
    }

    public ExpandableListView getListView() {
        return this.listView;
    }

    public void onDrawerWillOpen() {
        View welcomeView = this.inboxView.findViewById(R.id.welcome_screen);
        View noNewMessagesView = this.inboxView.findViewById(R.id.no_new_messages_screen);
        TextView noNewMessagesTextView = (TextView) this.inboxView.findViewById(R.id.no_new_messages_textview);
        View emptyView = this.inboxView.findViewById(R.id.inbox_empty_view);
        welcomeView.setVisibility(8);
        noNewMessagesView.setVisibility(0);
        noNewMessagesTextView.setText(R.string.inbox_no_new_messages);
        this.listView.setVisibility(0);
        this.listView.setEmptyView(emptyView);
    }

    public void showWelcomeDialog() {
        this.listView.setVisibility(4);
        View welcomeView = this.inboxView.findViewById(R.id.welcome_screen);
        View noNewMessagesView = this.inboxView.findViewById(R.id.no_new_messages_screen);
        welcomeView.setVisibility(0);
        noNewMessagesView.setVisibility(8);
    }

    public void showUnlockMessage() {
        View noNewMessagesView = this.inboxView.findViewById(R.id.inbox_empty_view);
        TextView noNewMessagesTextView = (TextView) this.inboxView.findViewById(R.id.no_new_messages_textview);
        this.listView.setVisibility(4);
        noNewMessagesView.setVisibility(0);
        noNewMessagesTextView.setText(R.string.lockscreen_inbox_unlock_message);
    }

    static int getRecusiveTop(View view) {
        if (view == null || !(view.getParent() instanceof View)) {
            return 0;
        }
        return getRecusiveTop((View) view.getParent()) + view.getTop();
    }

    public int getBottomOfInbox() {
        int lastItemIndexPosition = this.listView.getChildCount() - 1;
        if (lastItemIndexPosition == -1 || this.listView.getChildAt(lastItemIndexPosition) == null) {
            return getRecusiveTop(this.listView) + this.inboxView.findViewById(R.id.empty_inbox).getHeight();
        } else if (this.listView.getVisibility() == 4) {
            return getRecusiveTop(this.listView) + this.inboxView.findViewById(R.id.empty_inbox).getHeight();
        } else {
            View lastVisibleItem = this.listView.getChildAt(lastItemIndexPosition);
            int itemBottom = getRecusiveTop(lastVisibleItem) + lastVisibleItem.getHeight();
            int listBottom = getRecusiveTop(this.listView) + this.listView.getHeight();
            return listBottom <= itemBottom ? listBottom : itemBottom;
        }
    }
}
