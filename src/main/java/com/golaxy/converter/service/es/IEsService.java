package com.golaxy.converter.service.es;


import com.golaxy.converter.entity.es.ESSetData;
import org.apache.http.NameValuePair;

import java.util.List;

public interface IEsService {
	
	public String esIndex(ESSetData esSetData) throws Exception;
	
	public void esSearch(List<NameValuePair> params);
	
	public void esUpdate(ESSetData esSetData);

}

