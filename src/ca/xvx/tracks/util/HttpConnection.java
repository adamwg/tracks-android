package ca.xvx.tracks.util;

import android.util.Log;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

public class HttpConnection {
	private static final String TAG = "HttpConnection";
	
	private static HttpResponse go(String scheme, String host, int port, HttpRequest req,
								   String username, String password) throws Exception {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setUseExpectContinue(params, false);

		UsernamePasswordCredentials auth = getAuth(username, password);
		
		DefaultHttpClient h = new DefaultHttpClient(params);
		h.getCredentialsProvider().setCredentials(AuthScope.ANY, auth);

		BasicHttpContext localcontext = new BasicHttpContext();
		BasicScheme basicAuth = new BasicScheme();
		localcontext.setAttribute("preemptive-auth", basicAuth);
		h.addRequestInterceptor(new PreemptiveAuth(), 0);
		
		return h.execute(new HttpHost(host, port, scheme), req);
	}

	public static HttpResponse get(URI uri, String username, String password) throws Exception {
		HttpGet g = new HttpGet(uri);
		Log.v(TAG, "Get: " + uri);
		return go(uri.getScheme(), uri.getHost(), uri.getPort(), g, username, password);
	}

	public static HttpResponse delete(URI uri, String username, String password) throws Exception {
		HttpDelete d = new HttpDelete(uri);
		Log.v(TAG, "Delete: " + uri);
		return go(uri.getScheme(), uri.getHost(), uri.getPort(), d, username, password);
	}

	public static HttpResponse put(URI uri, String username, String password, String content) throws Exception {
		HttpPut p = new HttpPut(uri);
		Log.v(TAG, "Put: " + uri);
		if(content != null) {
			StringEntity ent = new StringEntity(content);
			Log.v(TAG, "Content: " + content);
			ent.setContentType("text/xml");
			p.setEntity(ent);
		}
		return go(uri.getScheme(), uri.getHost(), uri.getPort(), p, username, password);
	}

	public static HttpResponse post(URI uri, String username, String password, String content) throws Exception {
		HttpPost p = new HttpPost(uri);
		Log.v(TAG, "Post: " + uri);
		if(content != null) {
			StringEntity ent = new StringEntity(content);
			Log.v(TAG, "Content: " + content);
			ent.setContentType("text/xml");
			p.setEntity(ent);
		}
		return go(uri.getScheme(), uri.getHost(), uri.getPort(), p, username, password);
	}

	private static UsernamePasswordCredentials getAuth(String username, String password) {
		return new UsernamePasswordCredentials(username, password);
	}

	private static class PreemptiveAuth implements HttpRequestInterceptor {
		public void process(final HttpRequest request, final HttpContext context)
			throws IOException {
			
			AuthState authState = (AuthState)context.getAttribute(ClientContext.TARGET_AUTH_STATE);
			
			// If no auth scheme avaialble yet, try to initialize it preemptively
			if (authState.getAuthScheme() == null) {
				AuthScheme authScheme = (AuthScheme)context.getAttribute("preemptive-auth");
				CredentialsProvider credsProvider =
					(CredentialsProvider)context.getAttribute(ClientContext.CREDS_PROVIDER);
				HttpHost targetHost = (HttpHost)context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
				if (authScheme != null) {
					Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), 
																				   targetHost.getPort()));
					if (creds == null) {
						throw new IOException("No credentials for preemptive authentication");
					}
					authState.setAuthScheme(authScheme);
					authState.setCredentials(creds);
				}
			}
		}
	}
}