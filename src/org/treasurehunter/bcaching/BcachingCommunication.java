/*
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package org.treasurehunter.bcaching;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;

/**
 * Communicates with the bacching.com server to fetch geocaches as a GPX
 * InputStream.
 * 
 * @author Mark Bastian
 */
public class BcachingCommunication {
    private final String mUsername;

    private final String mHashword;

    private final String mBaseUrl = "http://www.electromedica.in/location.php";

    private final int mTimeout = 60000; // millisec

    public BcachingCommunication(String username, String password) {
        mUsername = username;
        String hashword = "";
        try {
            hashword = encodeHashword(username, password);
        } catch (Exception ex) {
            Log.e("TreasureHunter", ex.toString());
        }
        mHashword = hashword;
    }

    public void validateCredentials() throws Exception {
        // attempt to login at server
        // failure will throw an exception
        SendRequest("a=login&app=TreasureHunter");
    }

    public String encodeHashword(String username, String password) throws Exception {

        return encodeMd5Base64(password + username);
    }

    private String encodeQueryString(String username, String hashword, String params)
            throws Exception {

        if (username == null)
            throw new IllegalArgumentException("username is required.");
        if (hashword == null)
            throw new IllegalArgumentException("hashword is required.");
        if (params == null || params.length() == 0)
            throw new IllegalArgumentException("params are required.");

        StringBuffer sb = new StringBuffer();
        sb.append("u=");
        sb.append(URLEncoder.encode(username));
        sb.append("&");
        sb.append(params);
        sb.append("&time=");
        java.util.Date date = java.util.Calendar.getInstance().getTime();
        sb.append(date.getTime());
        String signature = encodeMd5Base64(sb.toString() + hashword);
        sb.append("&sig=");
        sb.append(URLEncoder.encode(signature));
        return sb.toString();
    }

    public InputStream SendRequest(Hashtable<String, String> params) throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append("");

        return SendRequest(sb.toString());
    }

    public InputStream SendRequest(String query) throws Exception {

        if (query == null || query.length() == 0)
            throw new IllegalArgumentException("query is required");

        final URL url = getURL("", "", query);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setReadTimeout(mTimeout);
        conn.setConnectTimeout(mTimeout);
        conn.addRequestProperty("Accept-encoding", "gzip");
        int responseCode = conn.getResponseCode(); // Will wait for response
        InputStream in = conn.getInputStream();

        String contentEncoding = conn.getContentEncoding();
        Log.d("TreasureHunter", "BcachingCommunication response length=" + conn.getContentLength()
                + ", contentEncoding=" + (contentEncoding == null ? "null" : contentEncoding));
        if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
            in = new java.util.zip.GZIPInputStream(in);
        }
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return in;
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            throw new Exception(sb.toString());
        }
    }

    private URL getURL(String username, String hashword, String params) throws Exception {

        return new URL(mBaseUrl);
    }

    public String encodeMd5Base64(String s) throws Exception {

        byte[] buf = s.getBytes();
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        md.update(buf, 0, buf.length);
        buf = new byte[16];
        md.digest(buf, 0, buf.length);
        return base64Encode(buf);
    }

    private static char[] map1 = new char[64];
    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            map1[i++] = c;
        }
        for (char c = 'a'; c <= 'z'; c++) {
            map1[i++] = c;
        }
        for (char c = '0'; c <= '9'; c++) {
            map1[i++] = c;
        }
        map1[i++] = '+';
        map1[i++] = '/';
    }

    public static String base64Encode(byte[] in) {
        int iLen = in.length;
        int oDataLen = (iLen * 4 + 2) / 3;// output length without padding
        int oLen = ((iLen + 2) / 3) * 4;// output length including padding
        char[] out = new char[oLen];
        int ip = 0;
        int op = 0;
        int i0, i1, i2, o0, o1, o2, o3;
        while (ip < iLen) {
            i0 = in[ip++] & 0xff;
            i1 = ip < iLen ? in[ip++] & 0xff : 0;
            i2 = ip < iLen ? in[ip++] & 0xff : 0;
            o0 = i0 >>> 2;
            o1 = ((i0 & 3) << 4) | (i1 >>> 4);
            o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
            o3 = i2 & 0x3F;
            out[op++] = map1[o0];
            out[op++] = map1[o1];
            out[op] = op < oDataLen ? map1[o2] : '=';
            op++;
            out[op] = op < oDataLen ? map1[o3] : '=';
            op++;
        }
        return new String(out);
    }
}
