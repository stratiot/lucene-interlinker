package gr.athena_innovation.imis.publicamundi.interlinking;

import gr.athena_innovation.imis.publicamundi.interlinking.InterlinkingException.ErrorType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Configurer {
	
	private Path confFile;
	
	Configurer (String confDir){
		this.confFile = Paths.get(confDir);
	}
	
	public void setConf(String index, String searchField) throws InterlinkingException{
		System.out.println(this.confFile);
		if (!Files.isReadable(this.confFile)){
			throw new InterlinkingException("Configuration file '" +confFile.toAbsolutePath()+ "' does not exist or is not readable, please check the path", false, ErrorType.DataNotAvailable);			
		} else{
			final BufferedReader reader;
			final BufferedWriter writer;
			String line;
			
			Map<String, String> indexes = new HashMap<String, String>();
			try {		
				// configuration is loaded to indexes HashMap
				reader = Files.newBufferedReader(this.confFile, StandardCharsets.UTF_8);
				while ((line = reader.readLine()) != null) {
			    	indexes.put(line.split(":")[0], line.split(":")[1]);
				}
				reader.close();
				
				if(indexes.get(index) == null){
					//Add a new index configuration record
					indexes.put(index, searchField);
				} else {
					//Update an existing index configuration record
					indexes.put(index, searchField);
				}
				// Update configuration file
				writer = Files.newBufferedWriter(this.confFile, StandardCharsets.UTF_8);
				for (int i=0; i< indexes.keySet().size(); i++){
					line = (String) (indexes.keySet().toArray()[i] + ":" + indexes.get(indexes.keySet().toArray()[i]));
					writer.write(line);
			        writer.newLine();
				}
				writer.close();
				
			} catch (IOException e) {
				throw new InterlinkingException("Configuration file '" +confFile.toAbsolutePath()+ "' " +
						"could not be read.", false, ErrorType.InternalServerError);
			} catch (ArrayIndexOutOfBoundsException e){
				throw new InterlinkingException("Configuration file '" +confFile.toAbsolutePath()+ "' " +
						"is malformed and cannot be read.", false, ErrorType.InternalServerError);
			}
		}
	}
	
	public String getConf(String index) throws InterlinkingException{
		String result = null;
		System.out.println(this.confFile);
		if (!Files.isReadable(this.confFile)){
			throw new InterlinkingException("Configuration file '" +confFile.toAbsolutePath()+ "' does not exist or is not readable, please check the path", false, ErrorType.DataNotAvailable);			
		} else{
			final BufferedReader reader;
			String line;
			
			Map<String, String> indexes = new HashMap<String, String>();
			try {		
				// configuration is loaded to indexes HashMap
				reader = Files.newBufferedReader(this.confFile, StandardCharsets.UTF_8);
				while ((line = reader.readLine()) != null) {
			    	indexes.put(line.split(":")[0], line.split(":")[1]);
				}
				reader.close();
				
				if(indexes.get(index) == null){
					//If not found
					throw new InterlinkingException("Index '" +index+ "' " +
							"was not found.", true, ErrorType.ValueNotRecognized);
				} else {
					//If found return it
					result = indexes.get(index);
				}
				
			} catch (IOException e) {
				throw new InterlinkingException("Configuration file '" +confFile.toAbsolutePath()+ "' " +
						"could not be read.", false, ErrorType.InternalServerError);
			} catch (ArrayIndexOutOfBoundsException e){
				throw new InterlinkingException("Configuration file '" +confFile.toAbsolutePath()+ "' " +
						"is malformed and cannot be read.", false, ErrorType.InternalServerError);
			}
		}
		return result;
	}

}
