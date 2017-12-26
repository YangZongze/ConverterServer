package com.golaxy.converter.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * http请求工具类
 */
public class HttpRequestUtils {
	
	/**
	 * post请求
	 * @param url
	 * @param body
	 * @param noNeedResponse
	 * @return
	 */
	public static String httpPost(String url, String body, boolean noNeedResponse) {
		String responsebody = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(url);

		httppost.setEntity(new StringEntity(body, "utf-8"));
		
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
			
			StatusLine statusLine = response.getStatusLine();
			HttpEntity entity = response.getEntity();
			if (statusLine.getStatusCode() >= 300) {
				throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
			}
			if (entity == null) {
				throw new ClientProtocolException("Response contains no content");
			}		
			//System.out.println(entity.getContentEncoding());
			responsebody = EntityUtils.toString(entity);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (response != null)
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return responsebody;
	}
	
	public static Map<String, Object> httpPost(String url, Map<String, String> headers, String body,
                                               boolean noNeedResponse) throws TimeoutException {
		Map<String, Object> result = new HashMap<>();
		String responsebody = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		HttpPost httppost = null;
        URI uri = null;
		
		for (int i=0; i<5; i++) {
			try {
			    httppost = uri==null ? new HttpPost(url) : new HttpPost(uri);

                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(3000)
                        .setConnectionRequestTimeout(1000)
                        .setSocketTimeout(5000).build();
                httppost.setConfig(requestConfig);
			
				if (headers != null) {
					for (Map.Entry<String, String> header: headers.entrySet()) {
						httppost.addHeader(header.getKey(), header.getValue());
					}
				}
				httppost.setEntity(new StringEntity(body, "utf-8"));
				
				response = httpclient.execute(httppost);
				
				StatusLine statusLine = response.getStatusLine();
				HttpEntity entity = response.getEntity();
				
				result.put("code", statusLine.getStatusCode());
				if (statusLine.getStatusCode() < 300) {
					responsebody = EntityUtils.toString(entity);
				} else {
					if (entity != null) 
						responsebody = EntityUtils.toString(entity);
					//throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
				}
				if (entity == null) {
					throw new ClientProtocolException("Response contains no content");
				}		
				//responsebody = EntityUtils.toString(entity);
			} catch (Exception e) {
				e.printStackTrace();
				if(e instanceof ConnectionPoolTimeoutException){
					if(response != null){
						try {
							response.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					httppost.releaseConnection();

					try {
						Thread.sleep(300);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					continue;
				}
				if (e instanceof URISyntaxException) {
                    try{
                        URL url1 = new URL(url);
                        uri = new URI(url1.getProtocol(), url1.getHost(), url1.getPath(), url1.getQuery(), null);
                    }catch(Exception e2){
                        e2.printStackTrace();
                    }
                    continue;
                }
			}

			result.put("body", responsebody);
			break;
		}

		if (response == null) {
		    throw new TimeoutException("conection failure");
        }

		return result;
	}
	
	public static Map<String, Object> httpPut(String url, Map<String, String> headers, String body,
                                              boolean noNeedResponse) throws TimeoutException {
		Map<String, Object> result = new HashMap<>();
		String responsebody = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		HttpPut httpPut = null;
        URI uri = null;
		
		for (int i=0; i<5; i++) {
			try {
			    httpPut = uri==null ? new HttpPut(url) : new HttpPut(uri);

                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setConnectionRequestTimeout(1000)
                        .setSocketTimeout(5000).build();
                httpPut.setConfig(requestConfig);
			
				if (headers != null) {
					for (Map.Entry<String, String> header: headers.entrySet()) {
						httpPut.addHeader(header.getKey(), header.getValue());
					}
				}
				httpPut.setEntity(new StringEntity(body, "utf-8"));
				
				response = httpclient.execute(httpPut);
				
				StatusLine statusLine = response.getStatusLine();
				HttpEntity entity = response.getEntity();
				
				result.put("code", statusLine.getStatusCode());
				if (statusLine.getStatusCode() < 300) {
					responsebody = EntityUtils.toString(entity);
				} else {
					if (entity != null) 
						responsebody = EntityUtils.toString(entity);
					//throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
				}
				if (entity == null) {
					throw new ClientProtocolException("Response contains no content");
				}		
			} catch (Exception e) {
				if(e instanceof ConnectionPoolTimeoutException){
					if(response!=null){
						try {
							response.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					httpPut.releaseConnection();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
				}

                if (e instanceof URISyntaxException) {
                    try{
                        URL url1 = new URL(url);
                        uri = new URI(url1.getProtocol(), url1.getHost(), url1.getPath(), url1.getQuery(), null);
                    }catch(Exception e2){
                        e2.printStackTrace();
                    }
                    continue;
                }
			} 
			result.put("body", responsebody);
			break;
		}
        if (response == null) {
            throw new TimeoutException("conection failure");
        }

		return result;
	}
	
	public static String httpPost(String url, Map<String, String> headers, List<NameValuePair> params, boolean noNeedResponse) {
		String responsebody = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		
		try {
			HttpPost httppost = new HttpPost(url);
		
			if (headers != null) {
				for (Map.Entry<String, String> header: headers.entrySet()) {
					httppost.addHeader(header.getKey(), header.getValue());
				}
			}
			httppost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			
			response = httpclient.execute(httppost);
			
			StatusLine statusLine = response.getStatusLine();
			HttpEntity entity = response.getEntity();
			
			if (statusLine.getStatusCode() >= 300) {
				if (entity != null) 
					responsebody = EntityUtils.toString(entity);
				throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
			}
			if (entity == null) {
				throw new ClientProtocolException("Response contains no content");
			}		
			//System.out.println(entity.getContentEncoding());
			responsebody = EntityUtils.toString(entity);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (response != null)
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		return responsebody;
	}
	
	/**
	 * get请求
	 * @param url
	 * @return
	 */
	public static String httpGet(String url) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(url);
		//httpget.addHeader("Accept", "application/vnd.kafka.json.v2+json");
		
		ResponseHandler<String> rh = new ResponseHandler<String>() {
			@Override
			public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
				StringBuffer buf = new StringBuffer();
				StatusLine statusLine = response.getStatusLine();
				HttpEntity entity = response.getEntity();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
				}
				if (entity == null) {
					throw new ClientProtocolException("Response contains no content");
				}
				ContentType contentType = ContentType.getOrDefault(entity);
				Charset charset = contentType.getCharset();
				Reader reader = null;
				if (charset != null)
					reader = new InputStreamReader(entity.getContent(), charset);
				else
					reader = new InputStreamReader(entity.getContent(), "utf-8");
				
				int ch = 0;
				while((ch=reader.read()) != -1) {
					buf.append((char)ch);
				}
				return buf.toString();
			}	
		};
	
		try {
			return httpclient.execute(httpget, rh);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	/**
	 * get请求
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static String httpGet(String url, Map<String, String> headers, List<NameValuePair> params) throws UnsupportedEncodingException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String paramsStr = "";
		if (params != null) {
			for (NameValuePair param: params) {
				paramsStr += "&" + param.getName() + "=" + URLEncoder.encode(param.getValue(), "UTF-8");
			}
			paramsStr = paramsStr.replaceFirst("&", "");
		}
		url = url + "?" + paramsStr;
		HttpGet httpget = new HttpGet(url);
		if (headers != null) {
			for (Map.Entry<String, String> header: headers.entrySet()) {
				httpget.addHeader(header.getKey(), header.getValue());
			}
		}
		
		ResponseHandler<String> rh = new ResponseHandler<String>() {
			@Override
			public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
				StringBuffer buf = new StringBuffer();
				StatusLine statusLine = response.getStatusLine();
				HttpEntity entity = response.getEntity();
				if (statusLine.getStatusCode() >= 300) {
					throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
				}
				if (entity == null) {
					throw new ClientProtocolException("Response contains no content");
				}
				ContentType contentType = ContentType.getOrDefault(entity);
				Charset charset = contentType.getCharset();
				Reader reader = null;
				if (charset != null)
					reader = new InputStreamReader(entity.getContent(), charset);
				else
					reader = new InputStreamReader(entity.getContent(), "utf-8");
				
				int ch = 0;
				while((ch=reader.read()) != -1) {
					buf.append((char)ch);
				}
				return buf.toString();
			}	
		};
	
		try {
			return httpclient.execute(httpget, rh);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
	}

}



