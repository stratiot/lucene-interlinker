package gr.athena_innovation.imis.publicamundi.interlinking;

import java.io.IOException;
import java.io.Writer;

import com.google.gson.stream.JsonWriter;

public class ExtendedJsonWriter extends JsonWriter{

	public ExtendedJsonWriter(Writer out) {
		super(out);
		// TODO Auto-generated constructor stub
	}
	
	public < E > ExtendedJsonWriter array(E [] inputArray) throws IOException{
		this.beginArray();
		for (E element : inputArray){
			this.value((String) element);
		}
		this.endArray();
		
		return this;
	}

}