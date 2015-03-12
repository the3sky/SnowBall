package blue.stack.snowball.app.core;

import android.content.Context;
import blue.stack.snowball.app.inbox.ui.InboxViewController;
import blue.stack.snowball.app.inbox.ui.InboxViewManager;
import blue.stack.snowball.app.shade.ShadeViewManager;
import blue.stack.snowball.app.shade.ui.ShadeInboxViewController;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class GuiceModule extends AbstractModule {
    private static Object lock;
    private static GuiceModule singleton;
    Context context;
    private Injector injector;

    static {
        singleton = null;
        lock = new Object();
    }

    private GuiceModule(Context context) {
        this.injector = null;
        this.context = context;
    }

    private static GuiceModule getInstance() {
        if (singleton != null) {
            return singleton;
        }
        throw new NullPointerException("Guice module should have been constructed earlier via construct().");
    }

    public static GuiceModule construct(Context context) {
        if (singleton == null) {
            synchronized (lock) {
                if (singleton == null) {
                    singleton = new GuiceModule(context.getApplicationContext());
                    singleton.injector = Guice.createInjector(singleton);
                }
            }
        }
        return singleton;
    }

    public static Injector get() {
        return getInstance().injector;
    }

    protected void configure() {
        bind(Context.class).toInstance(this.context);
        bind(InboxViewManager.class).to(ShadeViewManager.class);
        bind(InboxViewController.class).to(ShadeInboxViewController.class);
    }
}
