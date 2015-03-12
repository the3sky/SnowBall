package blue.stack.snowball.app.inbox.ui;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.apps.AppManager.AppLaunchMethod;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.Conversation;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.lockscreen.LockScreenManager;

import com.google.inject.Inject;

public class InboxFragment {
    private static final String TAG = "InboxFragment";
    @Inject
    AppManager appManager;
    Context context;
    InboxCursorAdapter cursorAdapter;
    @Inject
    InboxManager inboxManager;
    InboxCursorAdapter listAdapter;
    ExpandableListView listView;
    @Inject
    LockScreenManager lockScreenManager;
    OnItemClickListener serviceItemClickListener;

    class AnonymousClass_1 implements OnGroupClickListener {
        final /* synthetic */ InboxViewManager val$inboxViewManager;

        AnonymousClass_1(InboxViewManager inboxViewManager) {
            this.val$inboxViewManager = inboxViewManager;
        }

        public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long id) {
            Context context = InboxFragment.this.context;
            Conversation conversation = InboxFragment.this.inboxManager.getConversationFromViewCursor(InboxFragment.this.listAdapter.getGroup(groupPosition));
            if (InboxFragment.this.lockScreenManager.isPhoneLocked()) {
                InboxFragment.this.lockScreenManager.unlockAndLaunchApp(conversation.getLastMessage());
            } else {
                InboxFragment.this.appManager.launchAppForMessageWithBackButton(conversation.getLastMessage(), AppLaunchMethod.Inbox);
            }
            expandableListView.collapseGroup(groupPosition);
            this.val$inboxViewManager.closeDrawer();
            return true;
        }
    }

    class AnonymousClass_2 implements OnChildClickListener {
        final /* synthetic */ InboxViewManager val$inboxViewManager;

        AnonymousClass_2(InboxViewManager inboxViewManager) {
            this.val$inboxViewManager = inboxViewManager;
        }

        public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
            Context context = InboxFragment.this.context;
            Message message = InboxFragment.this.inboxManager.getMessageFromViewCursor(InboxFragment.this.listAdapter.getChild(groupPosition, childPosition));
            if (InboxFragment.this.lockScreenManager.isPhoneLocked()) {
                InboxFragment.this.lockScreenManager.unlockAndLaunchApp(message);
            } else {
                InboxFragment.this.appManager.launchAppForMessage(message, AppLaunchMethod.Inbox);
            }
            expandableListView.collapseGroup(groupPosition);
            this.val$inboxViewManager.closeDrawer();
            return true;
        }
    }

    public InboxFragment(Context context, ExpandableListView listView) {
        GuiceModule.get().injectMembers(this);
        this.context = context;
        this.listView = listView;
        this.cursorAdapter = InboxCursorAdapter.createInstance(context, listView);
        this.cursorAdapter.start();
        setListAdapter(this.cursorAdapter);
        InboxViewManager inboxViewManager = (InboxViewManager) GuiceModule.get().getInstance(InboxViewManager.class);
        listView.setOnGroupClickListener(new AnonymousClass_1(inboxViewManager));
        listView.setOnChildClickListener(new AnonymousClass_2(inboxViewManager));
    }

    public void setListAdapter(InboxCursorAdapter adapter) {
        if (this.listAdapter != null) {
        }
        this.listAdapter = adapter;
        if (this.listView != null) {
            this.listView.setAdapter(adapter);
        }
    }

    public ExpandableListAdapter getListAdapter() {
        return this.listAdapter;
    }

    public void scrollToTop() {
        this.listView.setSelection(0);
    }

    public void onDetach() {
        this.cursorAdapter.stop();
    }

    public void setServiceClickListener(OnItemClickListener listener) {
        this.serviceItemClickListener = listener;
    }
}
