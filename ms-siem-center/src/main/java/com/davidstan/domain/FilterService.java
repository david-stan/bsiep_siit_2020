package com.davidstan.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FilterService {

	@Autowired
	private LogFilter logFilter;
	
	public void setRegex(String regex) {
		this.logFilter.setFilterByContent(regex);
	}
}
