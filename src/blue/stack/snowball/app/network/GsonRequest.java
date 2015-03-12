package blue.stack.snowball.app.network;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

public class GsonRequest<T> extends Request<T> {
	private String body;
	private final Class<T> clazz;
	private final Gson gson;
	private Map<String, String> headers;
	private final Listener<T> listener;
	private Map<String, String> params;
	private RequestType requestType;

	public enum RequestType {
		RequestTypeWwwFormUrlEncoded,
		RequestTypeJson
	}

	public GsonRequest(int method, String url, Class<T> clazz, Listener<T> listener, ErrorListener errorListener) {
		super(method, url, errorListener);
		this.gson = new Gson();
		this.clazz = clazz;
		this.listener = listener;
		this.requestType = RequestType.RequestTypeWwwFormUrlEncoded;
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		if (this.body == null) {
			return super.getBody();
		}
		try {
			return this.body.getBytes(getParamsEncoding());
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
		}
	}

	@Override
	public String getBodyContentType() {
		return this.requestType == RequestType.RequestTypeJson ? "application/json; charset=" + getParamsEncoding()
				: "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return this.params;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		return this.headers != null ? this.headers : super.getHeaders();
	}

	@Override
	protected void deliverResponse(T response) {
		this.listener.onResponse(response);
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		try {
			return Response.success(this.gson.fromJson(
					new String(response.data, HttpHeaderParser.parseCharset(response.headers)), this.clazz),
					HttpHeaderParser.parseCacheHeaders(response));
		} catch (Throwable e) {
			return Response.error(new ParseError(e));
		}
	}
}
