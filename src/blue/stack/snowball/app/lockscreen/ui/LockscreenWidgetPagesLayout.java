package blue.stack.snowball.app.lockscreen.ui;

import java.util.ArrayList;
import java.util.List;

import blue.stack.snowball.app.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class LockscreenWidgetPagesLayout extends LinearLayout {
    int numPages;
    List<View> pages;

    public LockscreenWidgetPagesLayout(Context context) {
        super(context);
        init(null, 0);
    }

    public LockscreenWidgetPagesLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LockscreenWidgetPagesLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        this.numPages = 0;
        this.pages = new ArrayList();
    }

    public void setNumPages(int numPages) {
        removeAllViews();
        this.pages.clear();
        this.numPages = numPages;
        for (int i = 0; i < numPages; i++) {
            View page = new ImageView(getContext());
            page.setBackgroundResource(R.drawable.lockscreen_page);
            addView(page);
            this.pages.add(page);
            LayoutParams layoutParams = (LayoutParams) page.getLayoutParams();
            layoutParams.width = (int) getResources().getDimension(R.dimen.lockscreen_page_radius);
            layoutParams.height = (int) getResources().getDimension(R.dimen.lockscreen_page_radius);
            int margin = (int) getResources().getDimension(R.dimen.lockscreen_page_margin);
            layoutParams.setMargins(margin, 0, margin, 0);
            page.setLayoutParams(layoutParams);
        }
        if (numPages < 2) {
            setVisibility(8);
        } else {
            setVisibility(0);
        }
        setSelectedPage(0);
    }

    public void setSelectedPage(int selectedPage) {
        if (selectedPage >= 0 && selectedPage < this.pages.size()) {
            for (int i = 0; i < this.pages.size(); i++) {
                View page = (View) this.pages.get(i);
                if (i == selectedPage) {
                    page.setBackgroundResource(R.drawable.lockscreen_page_selected);
                } else {
                    page.setBackgroundResource(R.drawable.lockscreen_page);
                }
            }
            View view = (View) this.pages.get(selectedPage);
        }
    }
}
