package blue.stack.snowball.app.core;

public interface AsyncCallback {
    void onComplete(Object obj);

    void onError(String str);
}
