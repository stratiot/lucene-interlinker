package gr.athena_innovation.imis.publicamundi.interlinking;

import gr.athena_innovation.imis.publicamundi.interlinking.InterlinkingResponse;
import gr.athena_innovation.imis.publicamundi.interlinking.Indexer;
import gr.athena_innovation.imis.publicamundi.interlinking.InterlinkingException;
import gr.athena_innovation.imis.publicamundi.interlinking.InterlinkingRequest;
import gr.athena_innovation.imis.publicamundi.interlinking.InterlinkingException.ErrorType;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Servlet implementation class Interlinker
 */
@WebServlet("/Interlinker")
public class Interlinker extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private InterlinkingRequest request;
	private InterlinkingResponse response;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Interlinker() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		byte[] jsonData = new byte[request.getContentLength()];
		InputStream sis = request.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(sis);
        bis.read(jsonData, 0, jsonData.length);
        
		OutputStream out = response.getOutputStream();
		ExtendedJsonWriter writer = new ExtendedJsonWriter(new OutputStreamWriter(out, "UTF-8"));
        
    	int responseStatusCode = -1;
    	String responseMessage = null;
    	
        try{
        	// Parsing Request
        	this.request = this.parseRequest(jsonData);
        	

        	// Indexing Data
        	if(this.request.getMode().equals("index")){
        		
        		this.indexCSV(this.request.getFile(), 
						this.request.getIndex(), 
						this.request.getIndexField());
        		
        		// Creating response
        		responseStatusCode = 0;
        		responseMessage = "File '" + this.request.getFile() + "' has been succesfully indexed.";
        		this.response = new InterlinkingResponse(responseStatusCode, responseMessage);
        		this.response.writeResponse(writer, this.request.getMode());
        		//this.writeResponse(writer, this.request.getMode(), message);
        	} 
        	//Searching Data
        	else if(this.request.getMode().equals("search")){
        		CSV_Table results = this.searchSimilar(this.request.getReferenceDataset(),
        				this.request.getSearchTerm(), this.request.getMode());

        		// Creating response 
        		responseStatusCode = 1;
        		if (results.getRecords().size() > 0){
        			if(results.getRecords().size() == 1)
        				responseMessage = "One result was found.";
        			else
        				responseMessage = results.getRecords().size() + " were results found.";
        				
        		} else{
        			responseMessage = "No results were found.";
        		}
        			
        		this.response = new InterlinkingResponse(responseStatusCode, responseMessage, results);
        		this.response.writeResponse(writer, this.request.getMode());
        		//this.writeResponse(writer, this.request.getMode(), results);
        	} 
        	// Searching Data with a wildcard (*) like operand
        	else if(this.request.getMode().equals("like")){
        		CSV_Table results = this.searchSimilar(this.request.getReferenceDataset(),
        				this.request.getSearchTerm(), this.request.getMode());
        		
        		responseStatusCode = 2;
        		if (results.getRecords().size() > 0){
        			if(results.getRecords().size() == 1)
        				responseMessage = "One result was found.";
        			else
        				responseMessage = results.getRecords().size() + " were results found.";
        				
        		} else{
        			responseMessage = "No results were found.";
        		}
        		
        		// Creating response
        		this.response = new InterlinkingResponse(responseStatusCode, responseMessage, results);
        		this.response.writeResponse(writer, this.request.getMode());
        		//this.writeResponse(writer, this.request.getMode(), results);
        	} else if(this.request.getMode().equals("fields")){
        		responseStatusCode = 3;
        		responseMessage = "Index fields returned.";
        		List <String> fields = this.getIndexFields(this.request.getIndex(), this.request.areOriginals());
        		this.response = new InterlinkingResponse(responseStatusCode, responseMessage, fields);
        		this.response.writeResponse(writer, this.request.getMode());
        	}
        	
        	
        }catch (InterlinkingException e){
        	System.err.println("Exception occured. "+"Type: " + e.getErrorType() +
        			" Reason: " + e.getMessage());
        	
        	responseStatusCode = -1;
        	responseMessage = "Exception occured. "+"Type: " + e.getErrorType() +
        			" Reason: " + e.getMessage();
        	
        	this.response = new  InterlinkingResponse(responseStatusCode, responseMessage);
        	try {
				this.response.writeResponse(writer, this.request.getMode());
			} catch (InterlinkingException e1) {
				e1.printStackTrace();
			}
        } 
	}


	
	private InterlinkingRequest parseRequest(byte[] request) 
			throws IOException, InterlinkingException {
		InputStream in = new ByteArrayInputStream(request);
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		
		// Mode gets two values: "index" and "search"
		String mode = null;
		
		// In case mode is "search" the following parameters must be set.
		String searchTerm = null;
		String referenceDataset = null;
		
		//In case mode is "index" the following parameters must be set.
		String index = null;
		String indexField = null;
		String file = null;
		
		// In case mode is "fields" the following parameter optionally is set
		String originals = null;
		try{
			reader.beginObject();
			while(reader.hasNext()){
				String name = reader.nextName();
				if(name.equals("mode")){
					mode = reader.nextString();
				} else if (name.equals("reference")) {
					referenceDataset = reader.nextString();
				} else if (name.equals("term")) {
					searchTerm = reader.nextString();
				} else if (name.equals("index")){
					index = reader.nextString();
				}else if (name.equals("index_field")){
					indexField = reader.nextString();
				}else if (name.equals("file")){
					file = reader.nextString();
				}else if (name.equals("originals")){
					originals = reader.nextString();
				} else {
			         reader.skipValue();
			    }
			}
			reader.endObject();
			
		}finally {
			reader.close();
		}
		if(mode == null){
			throw new InterlinkingException("Invalid request: Parameter \"mode\" was not set", true, ErrorType.MalformedRequest);
		}
		else if(!mode.equals("search") && !mode.equals("index") && !mode.equals("like") && !mode.equals("fields")){
			throw new InterlinkingException("Invalid request: Parameter \"mode\" has invalid value. " +
					"Valid values are \"search\", \"index\" and \"like\".", true, ErrorType.Inconsistent);
		} 
		else if(searchTerm  == null && (mode.equals("search") || mode.equals("like"))){
			throw new InterlinkingException("Invalid request: Parameter \"term\" was not set", true, ErrorType.MalformedRequest);
		} 
		else if (referenceDataset == null && (mode.equals("search") || mode.equals("like"))){
			throw new InterlinkingException("Invalid request: Parameter \"reference\" was not set", true, ErrorType.MalformedRequest);
		} 
		else if (indexField == null && mode.equals("index")){
			throw new InterlinkingException("Invalid request: Parameter \"index_field\" was not set", true, ErrorType.MalformedRequest);
		} 
		else if (index == null && (mode.equals("index") || mode.equals("fields"))){
			throw new InterlinkingException("Invalid request: Parameter \"index\" was not set", true, ErrorType.MalformedRequest);
		} 
		else if (file == null && mode.equals("index")){
			throw new InterlinkingException("Invalid request: Parameter \"file\" was not set", true, ErrorType.MalformedRequest);
		}
		
		Map <String, String> parameters = new HashMap <String, String>();
		if (mode.equals("search")){
			parameters.put("searchTerm", searchTerm);
			parameters.put("referenceDataset", referenceDataset);
		} else if (mode.equals("like")){
			parameters.put("searchTerm", searchTerm);
			parameters.put("referenceDataset", referenceDataset);
			this.request = new InterlinkingRequest(mode, parameters);
		} else if (mode.equals("index")){
			parameters.put("index", index);
			parameters.put("indexField", indexField);
			parameters.put("file", file);
		} else if (mode.equals("fields")){
			parameters.put("index", index);
			if (originals == "true" || originals == null)
				parameters.put("originals", "true");
			else if (originals == "false"){
				parameters.put("originals", "false");
			}
		}
		
		return new InterlinkingRequest(mode, parameters);
	}
	
	private void indexCSV (String fileName, String index_str, String indexField) throws InterlinkingException{
		String local_data_sub_dir = "/WEB-INF/data/";
		String local_index_sub_dir = "/WEB-INF/indices/";
		String local_conf_file = "/WEB-INF/conf/indices.conf";
		String data_real_dir =  this.getServletContext().getRealPath(local_data_sub_dir + fileName);
		String index_real_dir = this.getServletContext().getRealPath(local_index_sub_dir + index_str);
		String conf_real_dir = this.getServletContext().getRealPath(local_conf_file);
		
		Indexer indexer = new Indexer(data_real_dir, index_real_dir, indexField);
		List <String> fields = indexer.index();
		
		// Update index configuration with this index
		Configurer conf = new Configurer (conf_real_dir);
		conf.setConf(index_str, indexField, fields);
	}
	
	private List <String> getIndexFields (String index, boolean originals) throws InterlinkingException{
		String local_conf_file = "/WEB-INF/conf/indices.conf";
		String conf_real_dir = this.getServletContext().getRealPath(local_conf_file);
				
		// Update index configuration with this index
		Configurer conf = new Configurer (conf_real_dir);
		IndexProperties idx = conf.getConf(index);
		if (idx == null){
			throw new InterlinkingException("Index '" + index + "' does not exist.", 
	    			 true, ErrorType.MalformedRequest);
		}
		List <String> fields = idx.getFields();
		if (fields != null && fields.size() > 0){
			String indexField = idx.getIndexField();
			List <String> real_fields = new ArrayList <String> ();
			real_fields.add(indexField);
			
			if(originals == false){
				real_fields.add("scoreField");
			}
			for (String field : fields){
				if(!field.equals(indexField))
					real_fields.add(field);
			}
			return real_fields;
			
		} else {
			throw new InterlinkingException("Fields for index '" + index + "' could not be retrieved.", 
	    			 false, ErrorType.InternalServerError);
		}
	}
	
	private CSV_Table searchSimilar(String referenceDataset, String searchTerm, String mode) throws InterlinkingException {
		String local_index_sub_dir = "/WEB-INF/indices/";
		String local_conf_file = "/WEB-INF/conf/indices.conf";
		String conf_real_dir = this.getServletContext().getRealPath(local_conf_file);
		String index_name = null;
		String searchField = null;
		
		if (mode.equals("search")){
			index_name = referenceDataset;
			searchField = "searchFieldText";
		} 
		else if (mode.equals("like")){
			index_name =  referenceDataset + "_unstemmed";
			searchField = "likeStringText";
		}
		String index_real_dir = this.getServletContext().getRealPath(local_index_sub_dir + index_name);
		
		//Query indexes.conf configuration file to retrieve information about the searchField
		Configurer conf = new Configurer (conf_real_dir);
		String orgiginalSearchField = conf.getConf(referenceDataset).getIndexField();
		
		
		Searcher searcher = new Searcher();
		return searcher.search(index_real_dir, searchField, searchTerm, mode, orgiginalSearchField);
	}
	
	
	// This version of writeResponse is used when user is searching an index
	/*
	private void writeResponse(ExtendedJsonWriter writer, String mode, CSV_Table results) throws InterlinkingException {
	    try{
	    	if(mode.equals("search") || mode.equals("like")){
	    		writer.beginObject();
	    		this.writerResponseResultsNumber (writer, results.getRecords().size());
	    		this.writeResponseFields(writer, results.getFields());
	    		this.writeResponseRecords(writer, results.getRecords());
	    		writer.endObject();
	    		
	    	}
	    	writer.close();
	    } catch (IOException e){
	    	throw new InterlinkingException("Uknown error while forming search response.", 
	    			false, ErrorType.InternalServerError);
	    }
	}
	*/

}