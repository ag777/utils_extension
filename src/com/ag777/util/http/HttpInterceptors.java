package com.ag777.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLSocketFactory;

import com.ag777.util.http.interceptor.GzipRequestInterceptor;
import com.ag777.util.http.model.MyCookieJar;
import com.ag777.util.http.model.ProgressResponseBody;
import com.ag777.util.http.model.SSLSocketClient;

import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 工具包包含的okhttp自定义拦截器合集
 * 
 * @author ag777
  * @version create on 2018年08月02日,last modify at 2018年08月02日
 */
public class HttpInterceptors {

	private HttpInterceptors() {}
	
	/**
	 * cookie持久化
	 * @return
	 */
	public static CookieJar cookieJar() {
		return new MyCookieJar();
	}
	
	/**
	 * 导入https证书文件
	 * @param certificates
	 * @return
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws KeyManagementException 
	 */
	public static SSLSocketFactory cer(InputStream... certificates) throws KeyManagementException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
		return SSLSocketClient.getSSLSocketFactory(certificates);
	}
	
	/**
	 * 进度监听
	 * @param listener
	 * @return
	 */
	public static Interceptor interceptor_progress(ProgressResponseBody.ProgressListener listener) {
		return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                //这里将ResponseBody包装成我们的ProgressResponseBody
                return response.newBuilder()
                        .body(new ProgressResponseBody(response.body(),listener))
                        .build();
            }
        };
	}
	
	/**
	 * 在发起请求的时候自动加入header，Accept-Encoding: gzip, 解析返回体
	 * @return
	 */
	public static Interceptor interceptor_gzip() {
		return new GzipRequestInterceptor();
	}
}
