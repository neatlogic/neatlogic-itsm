package codedriver.module.process.util;

import codedriver.framework.process.constvalue.RestfulType;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 在java中处理http请求.
 * 
 */
@Component
public class HttpUtil {
	protected static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			return;
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			return;
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	} };

	public static String getHttpConnectionData(String url, String authMode, String accessKey, String accessSecret, String restfulType, String encodingType){
		HttpURLConnection connection = null;
		InputStream is = null;
		OutputStream os = null;
		BufferedReader br = null;
		StringBuffer result = new StringBuffer();
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
				@Override
				public boolean verify(String paramString, SSLSession paramSSLSession) {
					return true;
				}
			});
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			URL getUrl = new URL(url);
			connection = (HttpURLConnection) getUrl.openConnection();
			String authorization = "";
			String currentTime = Long.toString(System.currentTimeMillis());
			if (StringUtils.isNotBlank(authMode)){
				if (authMode.equalsIgnoreCase("basic")) {
					String sign = accessKey + ":" + accessSecret;
					authorization = "Basic " + Base64.encodeBase64String(sign.getBytes("utf-8"));
				}
			}
			connection.setUseCaches(false);
			connection.setRequestMethod(restfulType);
			if (RestfulType.POST.getValue().equals(restfulType)){
				connection.setDoOutput(true);
				connection.setDoInput(true);
			}
			connection.setRequestProperty("x-access-date", currentTime);
			connection.setRequestProperty("Authorization", authorization);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			connection.setRequestProperty("Accept", "text/plain, application/json, */*");
			connection.connect();
			if (connection.getResponseCode() == 200){
				is = connection.getInputStream();
				if (is != null){
					br = new BufferedReader( new InputStreamReader(is, encodingType));
					String temp = null;
					if((temp=br.readLine())!=null){
						result.append(temp);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}finally {
			if (br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (is != null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			connection.disconnect();
		}
		return result.toString();
	}
}