package ca.xvx.tracks.util;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

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
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

public class HttpConnection {
	private static final String TAG = "HttpConnection";
	
	private static HttpResponse go(String scheme, String host, int port, HttpRequest req,
								   String username, String password, boolean badcert) throws Exception {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setUseExpectContinue(params, false);

		UsernamePasswordCredentials auth = getAuth(username, password);
		
		DefaultHttpClient h;

		if(badcert) {
			SchemeRegistry sr = new SchemeRegistry();
			sr.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			sr.register(new Scheme("https", new IgnoreSSLSocketFactory(), 443));
			ClientConnectionManager cm = new SingleClientConnManager(params, sr);
			h = new DefaultHttpClient(cm, params);
		} else {
			h = new DefaultHttpClient(params);
		}
		h.getCredentialsProvider().setCredentials(AuthScope.ANY, auth);

		BasicHttpContext localcontext = new BasicHttpContext();
		BasicScheme basicAuth = new BasicScheme();
		localcontext.setAttribute("preemptive-auth", basicAuth);
		h.addRequestInterceptor(new PreemptiveAuth(), 0);

		return h.execute(new HttpHost(host, port, scheme), req);
	}

	public static HttpResponse get(URI uri, String username, String password, boolean badcert) throws Exception {
		HttpGet g = new HttpGet(uri);
		Log.v(TAG, "Get: " + uri);
		return go(uri.getScheme(), uri.getHost(), uri.getPort(), g, username, password, badcert);
	}

	public static HttpResponse delete(URI uri, String username, String password, boolean badcert) throws Exception {
		HttpDelete d = new HttpDelete(uri);
		Log.v(TAG, "Delete: " + uri);
		return go(uri.getScheme(), uri.getHost(), uri.getPort(), d, username, password, badcert);
	}

	public static HttpResponse put(URI uri, String username, String password,
								   String content, boolean badcert) throws Exception {
		HttpPut p = new HttpPut(uri);
		Log.v(TAG, "Put: " + uri);
		if(content != null) {
			StringEntity ent = new StringEntity(content);
			Log.v(TAG, "Content: " + content);
			ent.setContentType("text/xml");
			p.setEntity(ent);
		}
		return go(uri.getScheme(), uri.getHost(), uri.getPort(), p, username, password, badcert);
	}

	public static HttpResponse post(URI uri, String username, String password,
									String content, boolean badcert) throws Exception {
		HttpPost p = new HttpPost(uri);
		Log.v(TAG, "Post: " + uri);
		if(content != null) {
			StringEntity ent = new StringEntity(content);
			Log.v(TAG, "Content: " + content);
			ent.setContentType("text/xml");
			p.setEntity(ent);
		}
		return go(uri.getScheme(), uri.getHost(), uri.getPort(), p, username, password, badcert);
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

	private static class IgnoreSSLSocketFactory implements SocketFactory, LayeredSocketFactory {
		private static SSLContext _sslc;

		static {
			try {
				_sslc = SSLContext.getInstance("TLS");
				_sslc.init(null, new TrustManager[] { new TrustAllManager() }, null);
			} catch(Exception e) { }
		}

		@Override
		public Socket connectSocket(Socket sock, String host, int port, InetAddress addr,
									int localPort, HttpParams params)
			throws IOException, UnknownHostException, ConnectTimeoutException {

			int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
			int soTimeout = HttpConnectionParams.getSoTimeout(params);

			InetSocketAddress rAddr = new InetSocketAddress(host, port);
			SSLSocket ssl = (SSLSocket)(sock != null ? sock : createSocket());

			if(addr != null || localPort > 0) {
				if(localPort < 0) {
					localPort = 0;
				}

				InetSocketAddress local = new InetSocketAddress(addr, localPort);
				ssl.bind(local);
			}

			ssl.connect(rAddr, connTimeout);
			ssl.setSoTimeout(soTimeout);

			return ssl;
		}

		@Override
		public Socket createSocket() throws IOException {
			return _sslc.getSocketFactory().createSocket();
		}

		@Override
		public Socket createSocket(Socket sock, String host, int port, boolean autoClose)
			throws IOException, UnknownHostException {

			return _sslc.getSocketFactory().createSocket(sock, host, port, autoClose);
		}

		@Override
		public boolean isSecure(Socket socket) throws IllegalArgumentException {
			return true;
		}
	}

	private static class TrustAllManager implements X509TrustManager {
		private X509TrustManager _manager;
		
		public TrustAllManager() throws NoSuchAlgorithmException, KeyStoreException {
			super();
			TrustManagerFactory fact = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			fact.init((KeyStore)null);
			TrustManager[] managers = fact.getTrustManagers();
			if(managers.length == 0) {
				throw new NoSuchAlgorithmException("No algorithm");
			}
			_manager = (X509TrustManager)managers[0];
		}

		@Override
		public void checkClientTrusted(X509Certificate[] certs, String type)
			throws CertificateException, IllegalArgumentException {
			
			_manager.checkClientTrusted(certs, type);
		}

		@Override
		public void checkServerTrusted(X509Certificate[] certs, String type)
			throws CertificateException, IllegalArgumentException {
			
			if(certs != null && certs.length == 1) {
				certs[0].checkValidity();
			} else {
				_manager.checkServerTrusted(certs, type);
			}
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return _manager.getAcceptedIssuers();
		}
	}
}