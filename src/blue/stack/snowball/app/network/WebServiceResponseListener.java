package blue.stack.snowball.app.network;

public interface WebServiceResponseListener<T> {
    void onError();

    void onResponse(T t);
}
