package gr.athena_innovation.imis.publicamundi.interlinking;

import gr.athena_innovation.imis.publicamundi.interlinking.InterlinkingException.ErrorType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * This class Indexes a CSV file to lucene
 * 
 */
public class Indexer {
	private String index;
	private String searchField;
	private boolean update;
	private Path csvFileDir;
	
	/**
	 * It creates an Indexer object given the following parameters.
	 * @param csvFilePath
	 * @param index
	 * @param searchField
	 */
	Indexer(String csvFilePath, String index, String searchField){
		this.update = false;
		this.index = index;
		this.csvFileDir = Paths.get(csvFilePath);
		this.searchField = searchField;
	}
	
	/**
	 * It creates an Indexer object given the following parameters. Parameter update is 
	 * boolean and it dictates weather the new index will update or create a new index.
	 * @param csvFilePath
	 * @param index
	 * @param searchField
	 * @param update
	 */
	Indexer(String csvFilePath, String index, String searchField, boolean update){
		this(csvFilePath, index, searchField);
		this.update = update;
	}

	
    
	public void index() throws InterlinkingException{
		
		if (!Files.isReadable(this.csvFileDir)){
			throw new InterlinkingException("Document file '" +this.csvFileDir.toAbsolutePath() + 
					"' does not exist or is not readable, please check the path", true, ErrorType.DataNotAvailable);			
		}
		
		try{
			System.out.println("Indexing contents of file "+ this.csvFileDir +"\nto directory '" + this.index + "'...");
			
			Directory dir = FSDirectory.open(Paths.get(this.index));
			GreekAnalyzer analyzer = new GreekAnalyzer();
			
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			
			if (!this.update){
				// Creating new index, and removing previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			}else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}
			
			
			IndexWriter writer = new  IndexWriter(dir, iwc);
			List <String> fields =  this.IndexDocs(writer, this.csvFileDir, this.searchField);
			
			writer.close();
			
			
			
			/*
			System.out.println("---------------------Printing all docs-----------------------");
		    IndexReader iter_reader = DirectoryReader.open(FSDirectory.open(Paths.get(this.index)));
		    System.out.println(iter_reader.maxDoc());
		    for (int i=0; i<iter_reader.maxDoc(); i++) {

		    	Document doc = iter_reader.document(i);
		    	String msg = fields.get(0) + ":" + doc.get(fields.get(0));
		    	for(int j=1;j<fields.size(); j++){
		    		msg = msg + "\t| " + fields.get(j) + ": " + doc.get(fields.get(j));
		    	}
		    	System.out.println(msg);
		    }
		    System.out.println("-------------------------------------------------------------");
		    */
			
		} catch (IOException e){
			System.out.println(" caught a " + e.getClass() +
					           "\n with message: " + e.getMessage());
		}
		
	}
	
	public List<String> IndexDocs(IndexWriter writer, Path path, String searchField) throws IOException{
		InputStream stream = Files.newInputStream(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line;
		
		// This is csv's header line
		line = reader.readLine();
		line = line.replace("\"", "");
		String [] fields = line.split(",");
		
		CSV_Table csv = new CSV_Table();
		csv.setFields(fields);
		
		// These are the records
		while ((line = reader.readLine()) != null) {
			line = line.replace("\"", "");
			String [] record = line.split(",");
			for (int i=0; i< record.length; i++){
				record[i] = record[i].trim();
			}
			csv.appendRecord(record);
	    }		
	    reader.close();
	    
	    //Adding fields to index
	    for (int i=0; i<csv.getRecords().size(); i++ ){
	    	Document doc = new Document();
	    	String key;
	    	String value;
	    	Field field;
	    	
	    	//Adding fields as TextFields 
	    	for (int j=0; j<csv.getFields().size() ; j++){
	    		key = csv.getFields().get(j);
	    		value = csv.getRecords().get(i).getValue(key);
	    		field = new TextField(key, value, Field.Store.YES);
	    		doc.add(field);
	    		if(key.equals(this.searchField)){
	        		key = "searchFieldText";
	    			value = value.toLowerCase();
	    			value = Normalizer.normalize(value, Normalizer.Form.NFD);
	    			value = value.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	    			field = new TextField(key, value, Field.Store.YES);
	        		doc.add(field);
	    		}
	    	}
			
	    	if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
		    	//System.out.println("adding " + csv.getRecords().get(i).getValue(csv.getFields().get(0)));
		    	writer.addDocument(doc);
		    } else {
		    	//System.out.println("updating " + csv.getRecords().get(i).getValue("placename"));
		    	writer.updateDocument(new Term("id", csv.getRecords().get(i).getValue(csv.getFields().get(0))), doc);
		    }
	    }
	
		List<String> temp_fields = csv.getFields();
		temp_fields.add("searchFieldText");
	    csv.setFields(temp_fields.toArray(new String[temp_fields.size()]));
	    
	    return csv.getFields();
	}
}

