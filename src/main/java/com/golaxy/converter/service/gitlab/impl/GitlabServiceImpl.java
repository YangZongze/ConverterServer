package com.golaxy.converter.service.gitlab.impl;

import com.golaxy.converter.entity.gitlab.GitlabGetData;
import com.golaxy.converter.entity.gitlab.GitlabSetData;
import com.golaxy.converter.exception.ExistException;
import com.golaxy.converter.service.gitlab.IGitlabService;
import com.golaxy.converter.utils.CommonUtils;
import com.golaxy.converter.utils.HttpRequestUtils;
import com.golaxy.converter.utils.JackJsonUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.*;

@Service("gitlabService")
public class GitlabServiceImpl implements IGitlabService {
	
	private final static Logger logger = LoggerFactory.getLogger(GitlabServiceImpl.class);
	private static Properties properties = new Properties();
	private static InputStream in = GitlabServiceImpl.class.getClassLoader().getResourceAsStream("config.properties");
	private static String gitlabBaseUrl;
	private static String privateToken;
	private static String gitlabBasePath;
	private static String contentType = "application/json;charset=UTF-8";

	static {
		try {
		    // 配置文件中有中文
			properties.load(new InputStreamReader(in, "UTF-8"));

			gitlabBaseUrl = properties.getProperty("gitlabBaseUrl");
			privateToken = properties.getProperty("gitlabPrivateToken");
			gitlabBasePath = properties.getProperty("gitlabBasePath");
			gitlabBasePath = gitlabBasePath.replaceAll("[/]{2,}", "/");
            if (gitlabBasePath.substring(0, 1).equals("/"))
                gitlabBasePath = gitlabBasePath.substring(1);
            if (gitlabBasePath.substring(gitlabBasePath.length()-1, gitlabBasePath.length()).equals("/"))
                gitlabBasePath = gitlabBasePath.substring(0, gitlabBasePath.length()-1);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String gitlabSave(String articleName, String fileName, String filePath, String userName, boolean update) throws ExistException {

        String fileGitPath = gitlabBasePath + "/" + CommonUtils.getStringDate() + "/" + articleName + "/" + fileName;
		String fileBase64Content = CommonUtils.file2Base64(filePath);
		boolean state;

		fileGitPath = fileGitPath.replaceAll("[/]{2,}", "/");
		try {
			if (!update)
				state = gitlabAdd(filePath, fileGitPath, fileBase64Content, userName);
			else
				state = gitlabUpdate(filePath, fileGitPath, fileBase64Content, userName);
		} catch (ExistException e) {
			throw new ExistException();
		}
		if (!state)
			return null;
		else
			return fileGitPath;
	}

	@Override
	public String gitlabSaveNone(String articleName, String fileName, String filePath, String userName) throws ExistException {

        String fileGitPath = gitlabBasePath + "/" + CommonUtils.getStringDate() + "/" + articleName + "/" + fileName;
		boolean state;
		try {
			state = gitlabAdd(filePath, fileGitPath, "", userName);
		} catch (ExistException e) {
			throw new ExistException();
		}
		if (!state)
			return null;
		else
			return fileGitPath;
	}

	@Override
	public boolean gitlabAdd(String filePath, String fileGitPath, String fileBase64Content, String userName) throws ExistException {
		
		// gitlab接口地址
		String url = gitlabBaseUrl + fileGitPath;
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", contentType);
		headers.put("PRIVATE-TOKEN", privateToken);
		
		GitlabSetData gitlabData = new GitlabSetData();
		gitlabData.setBranch("master");
		gitlabData.setEncoding("base64");
		gitlabData.setCommit_message(userName + " 添加: " + fileGitPath);
		gitlabData.setPath(fileGitPath);
		gitlabData.setContent(fileBase64Content);
		
		String body = JackJsonUtils.toJson(gitlabData);
		
		try {
			boolean saveStatus;
			int retryTimes = 1;
			do {
				Map<String, Object> result = HttpRequestUtils.httpPost(url, headers, body, false);
				String body1 = result.get("body").toString().replaceAll("\n", "").trim();
				if ( (int) result.get("code") > 300 ) {
					if (body1.contains("already exists")) {
						// {"message":"A file with this name already exists"}
						logger.error("Gitlab | " + filePath + " | " + body1);
						throw new ExistException();
					} else if (body1.contains("refresh") || body1.contains("try again") || body1.contains("Branch diverged")) {
						// {"message":"Could not update branch master. Please refresh and try again."}
						// {"message":"Branch diverged"}
						saveStatus = false;
						logger.error("Gitlab | " + filePath + " | " + body1 + " | retrying times:" + retryTimes++);
						try {
							Thread.sleep(500);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						continue;
					} else {
						logger.error("Gitlab | " + filePath + " | " + body1);
						return false;
					}
				}
				logger.info("Gitlab | " + filePath + " | " + body1);
				break;
			} while (!saveStatus);
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
			logger.error("Gitlab | " + filePath + " | " + e.getMessage());
			return false;
		} 
		
		return true;
	}

	@Override
	public boolean gitlabUpdate(String filePath, String fileGitPath, String fileBase64Content, String userName) {
		
		// gitlab接口地址
		String url = gitlabBaseUrl + fileGitPath;
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", contentType);
		headers.put("PRIVATE-TOKEN", privateToken);
		
		GitlabSetData gitlabData = new GitlabSetData();
		gitlabData.setBranch("master");
		gitlabData.setEncoding("base64");
		gitlabData.setCommit_message(userName + " 更新: " + fileGitPath);
		gitlabData.setPath(fileGitPath);
		gitlabData.setContent(fileBase64Content);
		
		String body = JackJsonUtils.toJson(gitlabData);
		
		try {
			boolean saveStatus = true;
			int retryTimes = 1;
			do {
				Map<String, Object> result = HttpRequestUtils.httpPut(url, headers, body, false);
				String body1 = result.get("body").toString().replaceAll("\n", "").trim();
				if ( (int) result.get("code") > 300 ) {
					if (body1.contains("already exists")) {//已存在
						logger.error("Gitlab | " + filePath + " | " + body1);
						throw new ExistException();
					} else if (body1.contains("refresh") || body1.contains("try again") || body1.contains("Branch diverged")) {
						saveStatus = false;
						logger.error("Gitlab | " + filePath + " | " + body1 + " | retrying times:" + (retryTimes++));
						try {
							Thread.sleep(300);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						continue;
					} else {
						logger.error("Gitlab | " + filePath + " | " + body1);
						return false;
					}
				}
				logger.info("Gitlab | " + filePath + " | " + body1);
				break;
			} while (!saveStatus);
		} catch (Exception e) {
			logger.error("Gitlab | " + filePath + " | " + e.getMessage());
			return false;
		} 
		
		return true;
	}

	@Override
	public String gitlabGet(String fileGitPath) throws Exception{
		
		String url = gitlabBaseUrl + fileGitPath;
		
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", contentType);
		headers.put("PRIVATE-TOKEN", privateToken);
		
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("file_path", fileGitPath));
		params.add(new BasicNameValuePair("ref", "master"));
		
		String result = HttpRequestUtils.httpGet(url, headers, params);
		
		GitlabGetData gitlabGetData = JackJsonUtils.fromJson(result, GitlabGetData.class);
		
		BASE64Decoder decoder = new BASE64Decoder();	
		
		return new String(decoder.decodeBuffer(gitlabGetData.getContent()));
	}

}

