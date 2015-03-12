package blue.stack.snowball.app.inbox.ui;

import java.util.ArrayList;
import java.util.Iterator;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import blue.stack.snowball.app.apps.App;
import blue.stack.snowball.app.apps.AppManager;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.Conversation;
import blue.stack.snowball.app.inbox.InboxListener;
import blue.stack.snowball.app.inbox.InboxManager;
import blue.stack.snowball.app.inbox.Message;
import blue.stack.snowball.app.inbox.ReadStateManager;
import blue.stack.snowball.app.inbox.ui.InboxConversationView.OnConversationViewExpandedListener;
import blue.stack.snowball.app.photos.ProfilePhoto;
import blue.stack.snowball.app.photos.ProfilePhotoManager;
import blue.stack.snowball.app.photos.ProfilePhotoManager.OnProfilePhotoLoadedListener;
import blue.stack.snowball.app.ui.SwipeDismissListViewTouchListener;
import blue.stack.snowball.app.ui.SwipeDismissListViewTouchListener.DismissCallbacks;

import com.google.inject.Inject;

public class InboxCursorAdapter extends CursorTreeAdapter implements InboxListener {
    private static final String TAG = "InboxCursorAdapter";
    ArrayList<AsyncTask<Void, Void, Cursor>> activeBackgroundQueries;
    @Inject
    AppManager appManager;
    Context context;
    long firstReadConversation;
    @Inject
    InboxManager inboxManager;
    ExpandableListView parent;
    @Inject
    ReadStateManager readStateManager;

    class AnonymousClass_1 implements DismissCallbacks {
        final /* synthetic */ ExpandableListView val$parent;

        AnonymousClass_1(ExpandableListView expandableListView) {
            this.val$parent = expandableListView;
        }

        public boolean canDismiss(int position) {
            return true;
        }

        public void onDismiss(ListView listView, int[] reverseSortedPositions) {
            for (int reverseSortedPosition : reverseSortedPositions) {
                long packedPosition = this.val$parent.getExpandableListPosition(reverseSortedPosition);
                int type = ExpandableListView.getPackedPositionType(packedPosition);
                int groupPosition;
                if (type == 1) {
                    groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                    int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
                    Log.d(TAG, "Removing group = " + groupPosition + "  |  child = " + childPosition + "  |  global position = " + reverseSortedPosition);
                    InboxCursorAdapter.this.inboxManager.deleteMessage(InboxCursorAdapter.this.inboxManager.getMessageFromViewCursor(InboxCursorAdapter.this.getChild(groupPosition, childPosition)));
                    InboxCursorAdapter.this.reset();
                } else if (type == 0) {
                    groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                    Log.d(TAG, "Removing group = " + groupPosition + "  |  global position = " + reverseSortedPosition);
                    InboxCursorAdapter.this.inboxManager.deleteConversation(InboxCursorAdapter.this.inboxManager.getConversationFromViewCursor(InboxCursorAdapter.this.getGroup(groupPosition)));
                    InboxCursorAdapter.this.reset();
                }
            }
        }
    }

    class AnonymousClass_2 implements OnProfilePhotoLoadedListener {
        final /* synthetic */ Context val$context;
        final /* synthetic */ InboxConversationView val$conversationView;

        AnonymousClass_2(InboxConversationView inboxConversationView, Context context) {
            this.val$conversationView = inboxConversationView;
            this.val$context = context;
        }

        public void onProfilePhotoLoaded(String appId, String senderId, String senderName, ProfilePhoto photo) {
            if (InboxCursorAdapter.this.areStringsEqual(appId, this.val$conversationView.getAppId()) && InboxCursorAdapter.this.areStringsEqual(senderId, this.val$conversationView.getAppSpecificSenderId()) && InboxCursorAdapter.this.areStringsEqual(senderName, this.val$conversationView.getFrom())) {
                this.val$conversationView.setProfilePhoto(this.val$context, photo);
            }
        }
    }

    class AnonymousClass_3 implements OnConversationViewExpandedListener {
        final /* synthetic */ int val$groupPosition;
        final /* synthetic */ boolean val$isExpanded;
        final /* synthetic */ ExpandableListView val$listView;

        AnonymousClass_3(boolean z, ExpandableListView expandableListView, int i) {
            this.val$isExpanded = z;
            this.val$listView = expandableListView;
            this.val$groupPosition = i;
        }

        public void onConversationViewExpanded(InboxConversationView view) {
            if (this.val$isExpanded) {
                this.val$listView.collapseGroup(this.val$groupPosition);
            } else {
                this.val$listView.expandGroup(this.val$groupPosition);
            }
        }
    }

    public static InboxCursorAdapter createInstance(Context context, ExpandableListView parent) {
        return new InboxCursorAdapter(context, ((InboxManager) GuiceModule.get().getInstance(InboxManager.class)).getConversationsView(), parent);
    }

    private InboxCursorAdapter(Context context, Cursor cursor, ExpandableListView parent) {
        super(cursor, context);
        GuiceModule.get().injectMembers(this);
        this.context = context;
        this.parent = parent;
        this.activeBackgroundQueries = new ArrayList();
        this.firstReadConversation = 0;
        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(parent, new AnonymousClass_1(parent));
        touchListener.setSwipeToDismissViewResourceId(R.id.swipe_to_dismiss_view);
        parent.setOnTouchListener(touchListener);
        parent.setOnScrollListener(touchListener.makeScrollListener());
        parent.setSelector(17170445);
    }

    public void start() {
        this.inboxManager.addListener(this);
    }

    public void stop() {
        this.inboxManager.removeListener(this);
    }

    public void onInboxUpdated() {
        reset();
    }

    public void onInboxMessageAdded(Message message) {
        reset();
    }

    public void onInboxCleared() {
        reset();
    }

    protected Cursor getChildrenCursor(Cursor groupCursor) {
        return this.inboxManager.getMessagesView(this.inboxManager.getConversationFromViewCursor(groupCursor).getSenderUserId());
    }

    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup viewGroup) {
        InboxConversationView threadView = new InboxConversationView(viewGroup.getContext());
        initConversationView(context, cursor, threadView, this.inboxManager.getConversationFromViewCursor(cursor), isExpanded);
        return threadView;
    }

    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        initConversationView(context, cursor, (InboxConversationView) view, this.inboxManager.getConversationFromViewCursor(cursor), isExpanded);
    }

    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup viewGroup) {
        InboxMessageView entryView = new InboxMessageView(viewGroup.getContext());
        initMessageView(context, entryView, this.inboxManager.getMessageFromViewCursor(cursor), isLastChild);
        return entryView;
    }

    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        initMessageView(context, (InboxMessageView) view, this.inboxManager.getMessageFromViewCursor(cursor), isLastChild);
    }

    void reset() {
        this.firstReadConversation = 0;
        Iterator i$ = this.activeBackgroundQueries.iterator();
        while (i$.hasNext()) {
            ((AsyncTask) i$.next()).cancel(false);
        }
        this.activeBackgroundQueries.clear();
        changeCursor(this.inboxManager.getConversationsView());
        notifyDataSetChanged();
    }

    void initConversationView(Context context, Cursor cursor, InboxConversationView conversationView, Conversation conversation, boolean isExpanded) {
        conversationView.setAppId(conversation.getLastMessage().getAppId());
        conversationView.setAppSpecificSenderId(conversation.getSender().getAppSpecificUserId());
        conversationView.setFrom(conversation.getSender().getDisplayName());
        conversationView.setMessagePreview(conversation.getLastMessage().getBody());
        conversationView.setTimestamp(conversation.getLastMessage().getTimestamp());
        conversationView.setNumUnreadMessages(conversation.getNumUnreadMessages());
        App app = this.appManager.getAppById(conversation.getLastMessage().getAppId());
        Drawable icon = app.getAppIcon();
        ProfilePhoto profilePhoto = app.getProfilePhotoForMessage(conversation.getLastMessage());
        conversationView.setProfilePhoto(context, profilePhoto);
        if (profilePhoto == null) {
            ((ProfilePhotoManager) GuiceModule.get().getInstance(ProfilePhotoManager.class)).loadProfilePhoto(conversation.getSender().getAppId(), conversation.getSender().getAppSpecificUserId(), conversation.getSender().getDisplayName(), new AnonymousClass_2(conversationView, context));
        }
        conversationView.setIcon(icon);
        int index = cursor.getPosition();
        boolean showAllMessagesDivider = false;
        if (index == 0 && conversation.getNumUnreadMessages() <= 0) {
            this.firstReadConversation = conversation.getLastMessage().getTimestamp();
        } else if (index > 0 && conversation.getNumUnreadMessages() <= 0 && conversation.getLastMessage().getTimestamp() >= this.firstReadConversation) {
            this.firstReadConversation = conversation.getLastMessage().getTimestamp();
            showAllMessagesDivider = true;
        }
        conversationView.showAllMessagesDivider(showAllMessagesDivider);
    }

    boolean areStringsEqual(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        return s1 != null && s1.equals(s2);
    }

    void initMessageView(Context context, InboxMessageView entryView, Message message, boolean isLastChild) {
        entryView.setMessage(message.getBody());
        entryView.setTimestamp(message.getTimestamp());
        if (isLastChild) {
            entryView.setShowMessageConversationSeperator(true);
        } else {
            entryView.setShowMessageConversationSeperator(false);
        }
    }

    public int getChildTypeCount() {
        return 2;
    }

    public int getChildType(int groupPosition, int childPosition) {
        return childPosition == 0 ? 1 : 0;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        InboxConversationView groupView = (InboxConversationView) super.getGroupView(groupPosition, isExpanded, convertView, parent);
        ExpandableListView listView = (ExpandableListView) parent;
        int numChildren = groupView.getNumUnreadMessages();
        groupView.setOnConversationExpandedListener(new AnonymousClass_3(isExpanded, listView, groupPosition));
        if (numChildren <= 1 || !isExpanded) {
            groupView.setShowConversationMessageSeperator(false);
        } else {
            groupView.setShowConversationMessageSeperator(true);
        }
        if (numChildren > 1 || !isExpanded) {
            groupView.setExpanded(isExpanded);
        } else {
            listView.collapseGroup(groupPosition);
            groupView.setExpanded(false);
        }
        if (groupPosition == 0) {
            groupView.setTopMargin((int) this.context.getResources().getDimension(R.dimen.inbox_card_0_top_margin));
        } else {
            groupView.setTopMargin(0);
        }
        return groupView;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return childPosition == 0 ? new EmptyInboxMessageView(parent.getContext()) : super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
    }
}
