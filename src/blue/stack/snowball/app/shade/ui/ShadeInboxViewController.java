package blue.stack.snowball.app.shade.ui;

import blue.stack.snowball.app.R;
import android.content.Context;
import blue.stack.snowball.app.inbox.ui.InboxViewController;

import com.google.inject.Inject;

public class ShadeInboxViewController extends InboxViewController {
    @Inject
    protected ShadeInboxViewController(Context context) {
        super(context);
    }

    protected int geLayoutId() {
        return R.layout.shade_inbox_drawer;
    }
}
