package com.golaxy.converter.service.es.impl;

import com.golaxy.converter.entity.es.ESSetData;
import com.golaxy.converter.service.es.IEsService;
import com.golaxy.converter.utils.HttpRequestUtils;
import com.golaxy.converter.utils.JackJsonUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Service("esService")
public class EsServiceImpl implements IEsService {
	
	private final static Logger logger = Logger.getLogger(EsServiceImpl.class);
	private static Properties properties = new Properties();
	private static InputStream in = EsServiceImpl.class.getClassLoader().getResourceAsStream("config.properties");
	
	static {
		try {
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String esIndex(ESSetData esSetData) throws Exception {

	    String indexId = null;
		String url = properties.getProperty("esIndex");
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("article_uid", esSetData.getArticle_uid()));
		params.add(new BasicNameValuePair("title", esSetData.getTitle()));
		params.add(new BasicNameValuePair("content", esSetData.getContent()));
		params.add(new BasicNameValuePair("path", esSetData.getPath()));
		params.add(new BasicNameValuePair("page", String.valueOf(esSetData.getPage())));
		params.add(new BasicNameValuePair("totalpage", String.valueOf(esSetData.getTotalpage())));
		params.add(new BasicNameValuePair("public_status", Boolean.toString(esSetData.getPublic_status())));
		params.add(new BasicNameValuePair("source", String.valueOf(esSetData.getSource())));		
		params.add(new BasicNameValuePair("author", esSetData.getAuthor()));
		params.add(new BasicNameValuePair("publish_time", esSetData.getPublish_time()));

		try {
			// 调用方法一
			//Map<String, Object> result = HttpRequestUtils.httpPost(url, null, params, false);
			// 调用方法二
			String requestBody = IOUtils.toString(new UrlEncodedFormEntity(params, "UTF-8").getContent(), "UTF-8");
			Map<String, Object> result = HttpRequestUtils.httpPost(url, headers, requestBody, false);
			
			String responseBody = result.get("body").toString().replaceAll("\n", "").trim();
			
			if ( (int) result.get("code") > 300 ) { 
				logger.error("ES | " + responseBody);
				throw new Exception(responseBody);
			} else {
				@SuppressWarnings("unchecked")
				Map<String, Object> res = JackJsonUtils.fromJson(responseBody, Map.class);
				int code = (int) res.get("code");
				if (code == 200) {
                    logger.info("ES | " + responseBody);
                    indexId = (String) res.get("iid");
				} else {
                    logger.error("ES | " + responseBody);
                    throw new Exception(responseBody);
				}
			}	
		} catch (Exception e) {
			logger.error("ES | " + e.getMessage());
			throw new Exception(e.getMessage());
		}

		return indexId;
	}

	@Override
	public void esSearch(List<NameValuePair> params) {

	}

	@Override
	public void esUpdate(ESSetData esSetData) {
		
	}
}

