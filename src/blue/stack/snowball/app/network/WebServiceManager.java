package blue.stack.snowball.app.network;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.Settings.Secure;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class WebServiceManager {
	private static final String BASE_URL = "http://squanda-env.elasticbeanstalk.com";
	private static final String ENDPOINT_ASK_FOR_INVITE_CODE = "http://squanda-env.elasticbeanstalk.com/askForInvite";
	private static final String ENDPOINT_REMOTE_DIAGNOSTICS = "http://squanda-env.elasticbeanstalk.com/remoteDiagnostics";
	private static final String ENDPOINT_REMOTE_LOGGER = "http://squanda-env.elasticbeanstalk.com/remoteLogger";
	private static final String ENDPOINT_REQUEST_INVITE_CODE = "http://squanda-env.elasticbeanstalk.com/requestInvite";
	private static final String ENDPOINT_USE_INVITE_CODE = "http://squanda-env.elasticbeanstalk.com/useInvite";
	private static final String TAG = "WebServiceManager";
	@Inject
	Context context;
	RequestQueue requestQueue;

	class ResponseListenerImpl<T> implements Listener<T>, ErrorListener {
		WebServiceResponseListener<T> listener;

		public ResponseListenerImpl(WebServiceResponseListener<T> listener) {
			this.listener = listener;
		}

		@Override
		public void onResponse(T response) {
			if (this.listener != null) {
				this.listener.onResponse(response);
			}
		}

		@Override
		public void onErrorResponse(VolleyError error) {
			if (this.listener != null) {
				this.listener.onError();
			}
		}
	}

	@Inject
	private WebServiceManager() {
	}

	@Inject
	private void start() {
		this.requestQueue = Volley.newRequestQueue(this.context);
	}

	public void stop() {
	}

	public void shouldAskForInviteCode(WebServiceResponseListener<Boolean> responseListener) {
		ResponseListenerImpl<Boolean> listener = new ResponseListenerImpl(responseListener);
		GsonRequest request = new GsonRequest(0, ENDPOINT_ASK_FOR_INVITE_CODE, Boolean.class, listener, listener);
		request.setHeaders(getStandardHeaders());
		this.requestQueue.add(request);
	}

	public void requestInviteCode(String email, WebServiceResponseListener<Boolean> responseListener) {
		Map<String, String> params = new HashMap();
		params.put("emali", email);
		ResponseListenerImpl<Boolean> listener = new ResponseListenerImpl(responseListener);
		GsonRequest request = new GsonRequest(1, ENDPOINT_REQUEST_INVITE_CODE, Boolean.class, listener, listener);
		request.setParams(params);
		request.setHeaders(getStandardHeaders());
		this.requestQueue.add(request);
	}

	public void useInviteCode(String inviteCode, WebServiceResponseListener<Boolean> responseListener) {
		Map<String, String> params = new HashMap();
		params.put("inviteCode", inviteCode);
		ResponseListenerImpl<Boolean> listener = new ResponseListenerImpl(responseListener);
		GsonRequest request = new GsonRequest(1, ENDPOINT_USE_INVITE_CODE, Boolean.class, listener, listener);
		request.setParams(params);
		request.setHeaders(getStandardHeaders());
		this.requestQueue.add(request);
	}

	public void sendRemoteLogData(String logDataBuffer) {
		sendRemoteLogData(logDataBuffer, null);
	}

	public void sendRemoteLogData(String logDataBuffer, WebServiceResponseListener<Void> responseListener) {
		String androidId = Secure.getString(this.context.getContentResolver(), "android_id");
		Map<String, String> params = new HashMap();
		params.put("androidId", androidId);
		params.put("log", logDataBuffer);
		ResponseListenerImpl<Void> listener = new ResponseListenerImpl(responseListener);
		GsonRequest request = new GsonRequest(1, ENDPOINT_REMOTE_LOGGER, Boolean.class, listener, listener);
		request.setParams(params);
		request.setHeaders(getStandardHeaders());
		this.requestQueue.add(request);
	}

	public void sendRemoteDiagnostics(String logDataBuffer) {
		sendRemoteDiagnostics(logDataBuffer, null);
	}

	public void sendRemoteDiagnostics(String logDiagnostics, WebServiceResponseListener<Void> responseListener) {
		String androidId = Secure.getString(this.context.getContentResolver(), "android_id");
		Map<String, String> params = new HashMap();
		params.put("androidId", androidId);
		params.put("diagnostics", logDiagnostics);
		ResponseListenerImpl<Void> listener = new ResponseListenerImpl(responseListener);
		GsonRequest request = new GsonRequest(1, ENDPOINT_REMOTE_DIAGNOSTICS, Boolean.class, listener, listener);
		request.setParams(params);
		request.setHeaders(getStandardHeaders());
		this.requestQueue.add(request);
	}

	Map<String, String> getStandardHeaders() {
		Map<String, String> headers = new HashMap();
		headers.put("Build", Build.ID);
		headers.put("OsVersion", VERSION.RELEASE);
		headers.put("Model", Build.MODEL);
		return headers;
	}
}
