package com.awf.spring.model;

import java.io.Serializable;
import java.util.Date;



public abstract class AspPersistentObject implements Serializable {

	public static final String SCHEMA_NAME = "BIZFUSE";
	public static final String COLUMNNAME_ID = "ID";
	public static final String COLUMNNAME_CREATED_DATE = "CREATED_DATE";
	public static final String PROPERTY_CREATED_DATE = "createdDate";
	public static final String COLUMNNAME_MODIFIED_DATE = "MODIFIED_DATE";
	public static final String PROPERTY_MODIFIED_DATE = "modifiedDate";
	public static final String TEXT = "NVARCHAR(2000)";
	public static final String NUMBER = "NUMERIC(20)";
	public static final String TEXT_SHORT = "NVARCHAR(255)";
	public static final String DATE = "datetime";


	private Long id;
	private Date createdDate;
	private Date modifiedDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
}
