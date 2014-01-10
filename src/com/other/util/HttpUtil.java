package com.other.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpUtil {
    
    public static String getHttp(String httpUrl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection)new URL(httpUrl).openConnection();
        conn.setConnectTimeout(5000);

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while((line = br.readLine()) != null)
            sb.append(line);

        return sb.toString();
    }

    public static String getHttps(String httpsUrl) throws Exception {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[] { new MyTrustManager() },
                new SecureRandom());

        //HttpsURLConnection.setFollowRedirects(true);
        HttpsURLConnection conn = (HttpsURLConnection) new URL(httpsUrl)
                .openConnection();
        //conn.setInstanceFollowRedirects(true);
        conn.setSSLSocketFactory(sc.getSocketFactory());
        conn.setHostnameVerifier(new MyHostnameVerifier());

        boolean redirect = false;
        int status = conn.getResponseCode();
        if (status != HttpsURLConnection.HTTP_OK) {
            if (status == HttpsURLConnection.HTTP_MOVED_TEMP
                || status == HttpsURLConnection.HTTP_MOVED_PERM
                    || status == 307
                    || status == HttpsURLConnection.HTTP_SEE_OTHER)
            redirect = true;
        }
        
        if (redirect){
            String newUrl = conn.getHeaderField("Location");
            String cookies = conn.getHeaderField("Set-Cookie");
            conn = (HttpsURLConnection) new URL(newUrl).openConnection();
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(new MyHostnameVerifier());

            conn.setRequestProperty("Cookie", cookies);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "google.com");
        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line);
        return sb.toString();
    }

    static private class MyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    static private class MyTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

}
