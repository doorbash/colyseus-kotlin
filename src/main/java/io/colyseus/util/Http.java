package io.colyseus.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Http {
    private static final int HTTP_CONNECT_TIMEOUT = 10000;
    private static final int HTTP_READ_TIMEOUT = 10000;

    public static class HttpException extends Exception {
        public int code;

        HttpException(String response, int code) {
            super(response);
            this.code = code;
        }
    }

    public static String request(String url) throws IOException, HttpException {
        return request(url, "GET", null, null);
    }

    public static String request(String url, String method) throws IOException, HttpException {
        return request(url, method, null, null);
    }

    public static String request(String url, String method, Map<String, String> httpHeaders) throws IOException, HttpException {
        return request(url, method, httpHeaders, null);
    }

    public static String request(String url, String method, Map<String, String> httpHeaders, String body) throws IOException, HttpException {
        System.out.println("sending http request to server...");
        System.out.println("url is " + url);
        System.out.println("http request body is " + body);
        System.out.println("http request method is " + method);
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod(method);
        if (httpHeaders != null) {
            for (Map.Entry<String, String> header : httpHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        con.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
        con.setReadTimeout(HTTP_READ_TIMEOUT);

        if (body != null) {
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int code = con.getResponseCode();
        System.out.println("http response code is " + code);

        InputStream is;
        if (code != HttpURLConnection.HTTP_OK)
            is = con.getErrorStream();
        else
            is = con.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            sb.append(responseLine.trim());
        }
        String response = sb.toString();

        if (code != HttpURLConnection.HTTP_OK) {
            throw new HttpException(response, code);
        }

        return response;
    }
}
