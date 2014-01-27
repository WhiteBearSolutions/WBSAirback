package com.whitebearsolutions.imagine.wbsairback.util;

import java.util.List;

//import com.whitebearsolutions.imagine.wbsairback.servlets.BackupJobsExtended.Filters;
//import com.whitebearsolutions.imagine.wbsairback.servlets.BackupJobsExtended.Rules;

public class jQgridParameters {
		private Integer page;
		public Integer getPage() {return page;}
		public void setPage(Integer value) {this.page = value;}
		
		private Integer rows;
		public Integer getRows() {return rows;}
		public void setRows(Integer value) {this.rows = value;}
		
		private String searchField;
		public String getSearchField() {return searchField;}
		public void setSearchField(String value) {this.searchField = value;}
		
		private String searchString;
		public String getSearchString() {return searchString;}
		public void setSearchString(String value) {this.searchString = value;}
		
		private String searchOper;
		public String getSearchOper() {return searchOper;}
		public void setSearchOper(String value) {this.searchOper = value;}
		
		private String sidx;
		public String getSidx() {return sidx;}
		public void setSidx(String value) {this.sidx = value;}
		
		private String sord;
		public String getSord() {return sord;}
		public void setSord(String value) {this.sord = value;}
		
		private Boolean search;
		public Boolean getSearch() {return search;}
		public void setSearch(Boolean value) {this.search = value;}
		
		private Filters filters;
		public Filters getFilters() {return filters;}
		public void setFilters(Filters value) {this.filters = value;}
		

	
	public class Filters {
		private String groupOp;
		public String getGroupOp() { return groupOp; }
		public void setGroupOp(String value) { this.groupOp = value; }
		
		private List<Rules> rules; 
		public List<Rules> getRules() {return rules;}
		public void setRules(List<Rules> value) {this.rules = value;}
	}
	
	public class Rules {
		private String field;
		public String getField() { return field; }
		public void setField(String value) { this.field = value; }
		
		private String op;
		public String getOp() { return op; }
		public void setOp(String value) { this.op = value; }
		
		private String data;
		public String getData() { return data; }
		public void setData(String value) { this.data = value; }
		
	}
}
