package gr.athena_innovation.imis.publicamundi.interlinking;


import gr.athena_innovation.imis.publicamundi.interlinking.InterlinkingException.ErrorType;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
	
	//TODO change the following line
	private CSV_Table results = new CSV_Table();
	
	public CSV_Table search(String index, String searchField, String searchTerm) throws InterlinkingException {
		String [] index_url_parts = index.split("/");
		String index_name = index_url_parts[index_url_parts.length-1];
		
		String queryString_simple = searchTerm.toLowerCase();
		queryString_simple = Normalizer.normalize(queryString_simple, Normalizer.Form.NFD);
	    queryString_simple = queryString_simple.replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); 
	    queryString_simple = queryString_simple.trim();
	    	    
	    
	    System.out.println(queryString_simple);
	    
	    int hitsPerPage = 10;
	    try{
		    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
		    IndexSearcher searcher = new IndexSearcher(reader);
		    Analyzer greek_analyzer = new GreekAnalyzer();
		    SimpleQueryParser parser =  new SimpleQueryParser(greek_analyzer, searchField);
		    Query query = parser.parse(queryString_simple);
		    
		    this.doPagingSearch(searcher,  query, hitsPerPage, searchField, index_name);
		    reader.close();
	    } catch (IOException e){
	    	throw new InterlinkingException("Problem occured when accessing index '" + index + "'.", 
	    			 true, ErrorType.InternalServerError);			
	    } catch (InterlinkingException e){
	    	throw e;
	    }
	    return this.results;
	}

	private void doPagingSearch(IndexSearcher searcher, Query query, 
				int hitsPerPage, String searchField, String index) throws IOException, InterlinkingException {
	    // Collect enough docs to show 5 pages
	    TopDocs results = searcher.search(query, 5 * hitsPerPage);
	    ScoreDoc[] hits = results.scoreDocs;
	    
	    int numTotalHits = results.totalHits;
	    System.out.println(numTotalHits + " total matching documents");

	    int start = 0;
	    int end = Math.min(numTotalHits, hitsPerPage);
	        
	    // Gathering fields in a list
	    Document doc = searcher.doc(hits[0].doc);
		List <String> fields = new ArrayList <String> ();
		for (int i=0; i < doc.getFields().size(); i++){
			if(!doc.getFields().get(i).name().equals("searchFieldText")){
				fields.add(doc.getFields().get(i).name());
			}
		}
		// Verifying that searchField is among index's indexed fields
		if (searfFieldNotInFields(searchField, fields)){
			throw new InterlinkingException("Index essing index '" + index + "' does not contain field '" + searchField + "'.", 
	    			 true, ErrorType.InternalServerError);
		}
		List <String> final_fields = new ArrayList <String> ();
		// The search field comes first then the score field, then the rest fields indexed
		final_fields.add(searchField);
		final_fields.add("scoreField");
		for (int i=0; i < fields.size(); i++){
			if(!fields.get(i).equals(searchField)){
				final_fields.add(fields.get(i));
			}
		} 
		// Setting result fields		
		this.results.setFields(final_fields.toArray(new String[final_fields.size()]));
		//this.results.setFields(final_fields.toArray(new String[final_fields.size()]));
	    
	    // Identifying the best score in order to normalize all scores
	    double best_score = 0;
	    for (int i = start; i < end; i++) {
	    	if (best_score < hits[i].score){
				best_score = hits[i].score;
			}
	    }
	    // Updating records
	    for (int i = start; i < end; i++) {
	    	List <String> temp_record = new ArrayList <String> ();
	    	doc = searcher.doc(hits[i].doc);
	    	
	    	for(int j=0; j < this.results.getFields().size(); j++){
	    		String current_field = this.results.getFields().get(j);
	    		if(current_field.equals("scoreField")){
	    			temp_record.add(Double.toString(normalize(hits[i].score, best_score)));
	    		} else{
	    			temp_record.add(doc.get(current_field));
	    		}
	    	}
	    	this.results.appendRecord(temp_record.toArray(new String[temp_record.size()]));
	    }
	}

	private boolean searfFieldNotInFields(String searchField,
			List<String> fields) {
		for (int i=0; i< fields.size(); i++){
			if(searchField.equals(fields.get(i)))
				return false;
		}
		return true;
	}
	
	private double normalize (double score, double best_score){
		return score/best_score;
	}
}
