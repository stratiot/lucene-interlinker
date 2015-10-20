package gr.athena_innovation.imis.publicamundi.interlinking;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CSV_Record {
	private Map<String, String> data = new HashMap<String, String>();

	
	public CSV_Record(Map<String, String> record) {
		this.data = record;
	}

	public String getValue(String key){
		return this.data.get(key);
	}
	
	public int size(){
		return this.data.size();
	}
	
	public Set<String> keySet(){
		return data.keySet();
	}
	
	public void setValue(String key, String value){
		this.data.put(key, value);
	}
	
	
	public Map<String, String> getValues() {
		return data;
	}
	
}
