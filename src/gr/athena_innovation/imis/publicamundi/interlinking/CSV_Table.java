package gr.athena_innovation.imis.publicamundi.interlinking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CSV_Table {
	private List<String> fields = new ArrayList<String>();
	private List<CSV_Record> records = new ArrayList<CSV_Record>();
	
	public List<String> getFields() {
		return fields;
	}
	
	public void setFields(String [] fields){
		this.fields = new ArrayList<String>();
		for(int i=0; i<fields.length; i++){
			this.appendField(fields[i]);
		}
	}
	
	public void appendField(String field){
		this.fields.add(field);
	}

	public List<CSV_Record> getRecords() {
		return records;
	}
	
	public void appendRecord(String [] values){
		Map <String, String> record = new HashMap<String, String>();
		for (int i =0; i < this.fields.size(); i++){
			record.put(this.fields.get(i), values[i]);
		}
		this.records.add(new CSV_Record(record));
	}
	
	@Override
	public String toString(){
		String fields = this.fields.get(0);
		for (int i=1; i< this.fields.size(); i++){
			fields += ", " + this.fields.get(i);
		}
		for (int i=0; i<this.records.size(); i++){
			fields += '\n';
			fields += this.records.get(i).getValue(this.fields.get(0));
			for(int j=1; j < this.fields.size(); j++){
				fields += ", " + this.records.get(i).getValue(this.fields.get(j));
			}
		}
		
		return fields;
	}
}