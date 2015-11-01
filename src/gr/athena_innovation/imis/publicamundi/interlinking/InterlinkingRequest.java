package gr.athena_innovation.imis.publicamundi.interlinking;

import java.util.Map;

public class InterlinkingRequest {
	private String mode;
	private String searchTerm;
	private String referenceDataset;
	private String file;
	private String indexField;
	private String index;
	private boolean originals;

	InterlinkingRequest(String mode, Map <String, String> parameters){
		this.mode = mode;
		if (this.mode.equals("search") || this.mode.equals("like")){
			this.searchTerm = parameters.get("searchTerm");
			this.referenceDataset = parameters.get("referenceDataset");
			this.file = null;
			this.indexField = null;
			this.index = null;
		}else if(this.mode.equals("index")){
			this.searchTerm = null;
			this.referenceDataset = null;
			this.file = parameters.get("file");
			this.indexField = parameters.get("indexField");
			this.index = parameters.get("index");
		}else if(this.mode.equals("fields")){
			this.searchTerm = null;
			this.referenceDataset = null;
			this.file = null;
			this.indexField = null;
			this.index = parameters.get("index");
			if (parameters.get("originals") == "true"){
				this.originals = true;
			} else{
				this.originals = false;
			}
		}
	}
	
	public String getSearchTerm() {
		return searchTerm;
	}
	public String getReferenceDataset() {
		return referenceDataset;
	}
	public String getMode() {
		return mode;
	}

	public String getFile() {
		return file;
	}

	public String getIndexField() {
		return indexField;
	}

	public String getIndex() {
		return index;
	}
	
	public boolean areOriginals() {
		return originals;
	}

}
