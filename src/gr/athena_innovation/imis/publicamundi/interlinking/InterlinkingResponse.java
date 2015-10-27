package gr.athena_innovation.imis.publicamundi.interlinking;

import gr.athena_innovation.imis.publicamundi.interlinking.InterlinkingException.ErrorType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.stream.JsonWriter;

public class InterlinkingResponse {
	private int _responseStatus;
	private String _responseMessage;
	private CSV_Table _results;
	private List <String> _fields;
	
	InterlinkingResponse (int code, String message, CSV_Table results){
		this._responseStatus = code;
		this._responseMessage = message;
		this._results = results;
		this._fields = null;
	}
	
	InterlinkingResponse (int code, String message){
		this._responseStatus = code;
		this._responseMessage = message;
		this._results = null;
		this._fields = null;
	}
	
	public InterlinkingResponse(int code, String message,
			List<String> fields) {
		this._responseStatus = code;
		this._responseMessage = message;
		this._results = null;
		this._fields = fields;
	}

	public int getResponseStatus() {
		return this._responseStatus;
	}
	public String getResponseMessage() {
		return this._responseMessage;
	}
	public CSV_Table getResults() {
		return this._results;
	}
	
	/*
	 *  This version of writeResponse is used when user is searching an index with the ordinary or the
	 *  wildcard manner.
	 */
	public void writeResponse(ExtendedJsonWriter writer, String mode) throws InterlinkingException{
		if (mode.equals("index")){
			try{
				writer.beginObject();
				writer.name("status").value(this.getResponseStatus());
				writer.name("message").value(this.getResponseMessage());
				writer.endObject();
				writer.close();
			} catch (IOException e){
		    	throw new InterlinkingException("Uknown error while forming search response.", 
		    			false, ErrorType.InternalServerError);
		    }
		} else if (mode.equals("fields")){
			try {
				writer.beginObject();
				writer.name("status").value(this.getResponseStatus());
				writer.name("message").value(this.getResponseMessage());
				if(this._fields != null){
		    		this._writeResponseFields(writer, this._fields);
	    		} else{
		    		this._writeResponseFields(writer, new ArrayList<String>());
	    		}
				writer.endObject();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new InterlinkingException("Uknown error while forming fields response.", 
		    			false, ErrorType.InternalServerError);
			}
		}else if(mode.equals("search") || mode.equals("like")){
			try{
	    		writer.beginObject();
	    		writer.name("status").value(this.getResponseStatus());
				writer.name("message").value(this.getResponseMessage());
	    		if(this._results != null){
		    		this._writerResponseResultsNumber (writer, this._results.getRecords().size());
		    		this._writeResponseFields(writer, this._results.getFields());
		    		this._writeResponseRecords(writer, this._results.getRecords());
	    		} else{
		    		this._writerResponseResultsNumber (writer, 0);
		    		this._writeResponseFields(writer, new ArrayList<String>());
		    		this._writeResponseRecords(writer, new ArrayList<CSV_Record>());
	    		}
	    		writer.endObject();
		    	writer.close();
		    } catch (IOException e){
		    	throw new InterlinkingException("Uknown error while forming search response.", 
		    			false, ErrorType.InternalServerError);
		    }
		} 
	}
	
	private void _writerResponseResultsNumber(ExtendedJsonWriter writer, int size) throws IOException {
		writer.name("results_number").value(size);
	}

	private void _writeResponseFields(ExtendedJsonWriter writer, 
			List <String> fields) throws IOException {
		
		writer.name("fields");
		writer.array(fields.toArray(new String[fields.size()]));
		
	}
	
	private void _writeResponseRecords(ExtendedJsonWriter writer,
			List<CSV_Record> records) throws IOException {
		
		writer.name("records");
		writer.beginArray();
		for (CSV_Record record : records){
			this._writeResponseRecord(writer, record);
		}
		writer.endArray();
	}
	
	private void _writeResponseRecord(ExtendedJsonWriter writer,
			CSV_Record record) throws IOException {
		writer.beginObject();
		for (String key: record.keySet()){
			writer.name(key).value(record.getValue(key));
		}writer.endObject();
	}
}
