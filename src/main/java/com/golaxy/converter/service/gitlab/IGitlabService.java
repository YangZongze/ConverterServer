package com.golaxy.converter.service.gitlab;

import com.golaxy.converter.exception.ExistException;

public interface IGitlabService {
	
	public String gitlabSave(String articleName, String fileName, String filePath, String userName, boolean update) throws ExistException;
	
	public String gitlabSaveNone(String articleName, String fileName, String filePath, String userName) throws ExistException;

	public boolean gitlabAdd(String filePath, String fileGitPath, String fileBase64Content, String userName) throws ExistException;
	
	public boolean gitlabUpdate(String filePath, String fileGitPath, String fileBase64Content, String userName);

	public String gitlabGet(String fileGitPath) throws Exception;

}

