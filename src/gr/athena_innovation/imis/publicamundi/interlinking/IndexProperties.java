package gr.athena_innovation.imis.publicamundi.interlinking;

import java.util.ArrayList;
import java.util.List;

public class IndexProperties{
	private String indexName;
	private String indexField;
	private List <String> fields;
	
	IndexProperties(String indexName){
		this.indexName = indexName;
		this.fields = new ArrayList <String> ();
	}
	
	public String getIndexName(){
		return this.indexName;
	}
	
	public void setIndexField(String indexField){
		this.indexField = indexField;
	}
	
	public String getIndexField(){
		return this.indexField;
	}
	
	public void appendField (String field){
		this.fields.add(field);
	}
	
	public List <String> getFields(){
		return this.fields;
	}
	
}
