package blue.stack.snowball.app.ui;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;

public class ExpandableListView extends android.widget.ExpandableListView {
    public static String LAYOUT_CHILDREN;

    static {
        LAYOUT_CHILDREN = "blue.stack.snowball.app.ui.ShadeExpandableListView.layoutChildren";
    }

    public ExpandableListView(Context context) {
        super(context);
    }

    public ExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void layoutChildren() {
        super.layoutChildren();
        getContext().sendBroadcast(new Intent(LAYOUT_CHILDREN));
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }
}
