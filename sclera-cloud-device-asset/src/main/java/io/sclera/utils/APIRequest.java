package io.sclera.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.projecthaystack.io.HZincReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Map;


@Service
public class APIRequest {

    public ResponseEntity<String> validateResponse(HttpURLConnection con) throws IOException {
        int status = con.getResponseCode();
        BufferedReader in = null;
        String inputLine = null;
        if (status > 299) {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        }

        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        con.disconnect();
        return new ResponseEntity<String>(content.toString(), HttpStatus.valueOf(status));
    }

    public ResponseEntity<String> validateAndFormatSlaveResponse(HttpURLConnection con) throws IOException {
        int status = con.getResponseCode();
        BufferedReader in = null;
        String inputLine = null;
        if (status > 299) {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        }

        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine).append("\n"); // \n-preserving behavior
        }

        in.close();
        con.disconnect();
        return new ResponseEntity<String>(content.toString(), HttpStatus.valueOf(status));
    }

    public String getParamsString(String apiurl, Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        result.append(apiurl);

        if (params != null) {
            result.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }

            String resultString = result.toString();
            return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
        }

        return result.toString();

    }


    public ResponseEntity<String> httpRequest(String apiurl, String requestMethod, String requestBody, Map<String, String> headers, Map<String, String> parameters, Integer connectionTimeout, Integer readTimeout) {

        try {
            URL url = new URL(getParamsString(apiurl, parameters));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(requestMethod);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("charset", "UTF-8");


            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            if (readTimeout == null)
                readTimeout = 50000;
            if (connectionTimeout == null)
                connectionTimeout = 50000;

            con.setDoOutput(true);
            con.setReadTimeout(readTimeout);
            con.setConnectTimeout(connectionTimeout);

            if (requestBody != null) {
                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = requestBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                out.flush();
                out.close();
            }
            //////////////////////////////////////////////////////////////////////////////
            int statusCode = con.getResponseCode();
            if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                // Handle 401 Unauthorized
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            }
            //////////////////////////////////////////////////////////////////////////////

            return this.validateResponse(con);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

    // Preserve newlines in response body for slave formatter (used by Slave)
    public ResponseEntity<String> httpRequestSlaveFormatter(String apiurl, String requestMethod, String requestBody, Map<String, String> headers, Map<String, String> parameters, Integer connectionTimeout, Integer readTimeout) {

        try {
            URL url = new URL(getParamsString(apiurl, parameters));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(requestMethod);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("charset", "UTF-8");


            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            if (readTimeout == null)
                readTimeout = 50000;
            if (connectionTimeout == null)
                connectionTimeout = 50000;

            con.setDoOutput(true);
            con.setReadTimeout(readTimeout);
            con.setConnectTimeout(connectionTimeout);

            if (requestBody != null) {
                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = requestBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                out.flush();
                out.close();
            }
            //////////////////////////////////////////////////////////////////////////////
            int statusCode = con.getResponseCode();
            if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                // Handle 401 Unauthorized
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            }
            //////////////////////////////////////////////////////////////////////////////

            return this.validateAndFormatSlaveResponse(con);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }


    // temepropry with basic auth

    public ResponseEntity<String> httpRequesttest(String apiurl, String requestMethod, String requestBody, Map<String, String> headers, Map<String, String> parameters, Integer connectionTimeout, Integer readTimeout) {

        try {
            URL url = new URL(getParamsString(apiurl, parameters));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(requestMethod);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("charset", "UTF-8");

            // test
            final String customerKey = "c244fh1ih8qg00aumk80";
            // Customer secret
            final String customerSecret = "bea191be24984a53ad24dd6a5f735586";

            // Concatenate customer key and customer secret and use base64 to encode the concatenated string
            String plainCredentials = customerKey + ":" + customerSecret;
            String base64Credentials = new String(java.util.Base64.getEncoder().encode(plainCredentials.getBytes()));
            // Create authorization header
            String authorizationHeader = "Basic " + base64Credentials;


            con.setRequestProperty("Authorization", authorizationHeader);
            // test End

            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            if (readTimeout == null)
                readTimeout = 50000;
            if (connectionTimeout == null)
                connectionTimeout = 50000;

            con.setDoOutput(true);
            con.setReadTimeout(readTimeout);
            con.setConnectTimeout(connectionTimeout);

            if (requestBody != null) {
                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = requestBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                out.flush();
                out.close();
            }

            return this.validateResponse(con);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }


    }

    public String dockerHttpRequest(String apiUrl, String requestBody, Map<String, String> headers, Map<String, String> parameters, String method) {
        try {
            String urlWithParams = getParamsString(apiUrl, parameters);

            HttpUriRequest request = null;

            switch (method) {
                case "GET":
                    request = new HttpGet(urlWithParams);
                    break;
                case "POST":
                    request = new HttpPost(urlWithParams);
                    break;
                case "PUT":
                    request = new HttpPut(urlWithParams);
                    break;
                case "DELETE":
                    request = new HttpDelete(urlWithParams);
                    break;
            }

            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");
            request.addHeader("charset", "UTF-8");

            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    request.addHeader(header.getKey(), header.getValue());
                }
            }

            if (requestBody != null && method.equals("POST")) {
                ((HttpPost) request).setEntity(new StringEntity(requestBody));
            }

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(request);
            HttpEntity httpEntity = response.getEntity();
            String apiOutput = (httpEntity != null && httpEntity.getContentLength() > 0) ? EntityUtils.toString(httpEntity) : "{}";

            return apiOutput;
        } catch (IOException e) {
            System.out.println(e);
        }

        return null;
    }

    /**********************************************Daintree API's******************************************************************/

    public ResponseEntity<String> httpRequestForDaintreeAccessToken(String authUrl, String client_id, String client_secret, String request, Map<String, String> parameter, Integer connectionTimeout, Integer readTimeout) {
        try {
            String encoding = Base64.encodeBase64String((client_id + ":" + client_secret).getBytes("UTF-8"));
            URL url = new URL(getParamsString(authUrl, parameter));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(request);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "UTF-8");
            con.setRequestProperty("Authorization", "Basic " + encoding);
            if (readTimeout == null)
                readTimeout = 50000;
            if (connectionTimeout == null)
                connectionTimeout = 50000;

            con.setDoOutput(true);
            con.setReadTimeout(readTimeout);
            con.setConnectTimeout(connectionTimeout);
            APIRequest apiRequest = new APIRequest();
            return apiRequest.validateResponse(con);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<String> httpRequestForDaintreeReadOperation(String apiurl, String mimeType, String token, String parameters, String requestbody, Integer connectionTimeout, Integer readTimeout) {
        URL url;
        try {
            url = new URL(apiurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Connection", "Close");
            connection.setRequestProperty("Content-Type", mimeType == null ? "text/zinc; charset=utf-8" : mimeType);
            Writer cout = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            cout.write(parameters);
            cout.close();
            StringBuffer s = new StringBuffer(1024);
            Reader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            int n;
            while ((n = r.read()) > 0) s.append((char) n);
            return new ResponseEntity<>(new HZincReader(s.toString()).readGrid().toJson(), HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

    /**********************************************Daintree API's******************************************************************/

    /**********************************************Siemens API's******************************************************************/

    public ResponseEntity<String> httpsRequest(String apiurl, String requestMethod, String requestBody, Map<String, String> headers, Map<String, String> parameters, Integer connectionTimeout, Integer readTimeout) {

        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            URL url = new URL(getParamsString(apiurl, parameters));
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod(requestMethod);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("charset", "UTF-8");
            if (con instanceof HttpsURLConnection) {
                ((HttpsURLConnection) con).setSSLSocketFactory(sc.getSocketFactory());
                ((HttpsURLConnection) con).setHostnameVerifier((hostname, session) -> true);
            }

            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            if (readTimeout == null)
                readTimeout = 50000;
            if (connectionTimeout == null)
                connectionTimeout = 50000;

            con.setDoOutput(true);
            con.setReadTimeout(readTimeout);
            con.setConnectTimeout(connectionTimeout);

            if (requestBody != null) {
                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = requestBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                out.flush();
                out.close();
            }

            return this.validateResponse(con);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

    /**********************************************Siemens API's******************************************************************/

    public ResponseEntity<String> gaiameshHttpRequest(String apiurl, String requestMethod, String requestBody, Map<String, String> headers, Map<String, String> parameters, Integer connectionTimeout, Integer readTimeout) {

        int maxRetries = 3;
        long backoffMillis = 1000; // 1s → 2s → 4s

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            HttpURLConnection con = null;
            try {
                URL url = new URL(getParamsString(apiurl, parameters));
                con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod(requestMethod);
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                if (headers != null) {
                    headers.forEach(con::setRequestProperty);
                }

                con.setConnectTimeout(connectionTimeout != null ? connectionTimeout : 50000);
                con.setReadTimeout(readTimeout != null ? readTimeout : 50000);

                if (requestBody != null && !requestBody.isBlank()) {
                    con.setDoOutput(true);
                    try (OutputStream os = con.getOutputStream()) {
                        os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                        os.flush();
                    }
                }

                int status = con.getResponseCode();

                if (status == HttpStatus.UNAUTHORIZED.value()) {
                    return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
                }

                return validateResponse(con);

            } catch (SocketTimeoutException | ConnectException e) {
                System.out.println("Network error (attempt " + attempt + "/" + maxRetries + ") while calling " + apiurl + " : " + e.getMessage());

                if (attempt == maxRetries) {
                    return new ResponseEntity<>("Connection timed out", HttpStatus.GATEWAY_TIMEOUT);
                }

                try {
                    Thread.sleep(backoffMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return new ResponseEntity<>("Request interrupted", HttpStatus.SERVICE_UNAVAILABLE);
                }
                backoffMillis *= 2;

            } catch (Exception e) {
                System.out.println("Unexpected error while calling " + apiurl + " : " + e.getMessage());
                return new ResponseEntity<>("Internal error", HttpStatus.INTERNAL_SERVER_ERROR);

            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }
        return new ResponseEntity<>("Request failed", HttpStatus.SERVICE_UNAVAILABLE);
    }


}

