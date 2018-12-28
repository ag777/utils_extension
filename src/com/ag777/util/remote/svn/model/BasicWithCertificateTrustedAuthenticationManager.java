package com.ag777.util.remote.svn.model;

import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
 
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;

public class BasicWithCertificateTrustedAuthenticationManager extends BasicAuthenticationManager {

	@SuppressWarnings("deprecation")
	public BasicWithCertificateTrustedAuthenticationManager(String userName, String password) {
		super(userName, password);
	}

	@Override
	public TrustManager getTrustManager(SVNURL url) throws SVNException {
		try {
			// HTTPS URL requires certificate trust process
			// if (url != null && url.getProtocol() != null &&
			// url.getProtocol().startsWith("https")) {
			// TrustManagerUtils comes from commons-net:commons-net:3.3
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new java.security.cert.X509Certificate[] {};
				}
				public void checkClientTrusted(X509Certificate[] chain, String authType) {
				}
				public void checkServerTrusted(X509Certificate[] chain, String authType) {
				}
			} };
			return trustAllCerts[0];
			// }
		} catch (Exception e) {
			throw new SVNException(SVNErrorMessage.create(SVNErrorCode.IO_ERROR, e.getMessage()), e);
		}
	}

}
