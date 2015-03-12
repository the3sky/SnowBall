package blue.stack.snowball.app.shade.ui;

import java.util.Iterator;

import blue.stack.snowball.app.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import blue.stack.snowball.app.core.GuiceModule;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;
import blue.stack.snowball.app.ui.ExpandableListView;

import com.google.inject.Inject;

public class ShadePanelHolder extends PanelHolder {
    ShadeHeaderView header;
    @Inject
    InboxViewManager inboxViewManager;

    public ShadePanelHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        if (!isInEditMode()) {
            GuiceModule.get().injectMembers(this);
            setWillNotDraw(false);
            getContext().registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    ShadePanelHolder.this.refreshFooterPosition();
                }
            }, new IntentFilter(ExpandableListView.LAYOUT_CHILDREN));
        }
    }

    public void setHeaderView(ShadeHeaderView header) {
        this.header = header;
    }

    public void setExpandedHeight(float expandedHeight) {
        super.setExpandedHeight(expandedHeight);
        this.header.setExpandedHeight(expandedHeight);
        refreshFooterPosition();
    }

    public void refreshFooterPosition() {
        View footer = findViewById(R.id.shade_settings_footer);
        View touchHandle = findViewById(R.id.handle);
        int bottom = this.inboxViewManager.getInboxViewController().getBottomOfInbox();
        if (this.expandedHeight < ((float) bottom)) {
            bottom = (int) this.expandedHeight;
        }
        footer.setY((float) bottom);
        touchHandle.setY((float) bottom);
    }

    Boolean isNearlyFullyExpanded() {
        Iterator i$ = this.mPanels.iterator();
        return i$.hasNext() ? Boolean.valueOf(((PanelView) i$.next()).isNearlyFullyExpanded()) : Boolean.valueOf(false);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
