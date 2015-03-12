package blue.stack.snowball.app.apps;

import java.util.List;

public interface AppListener {
    void onAppLaunched(App app);

    void onEnabledAppsChanged(List<App> list);
}
