package simplenlg;
import static spark.Spark.*;
import simplenlg.framework.*;
import simplenlg.lexicon.*;
import simplenlg.realiser.english.*;
import simplenlg.phrasespec.*;
import simplenlg.features.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Main {
	
	//@Data
	static class NewSentencePayload {
		private String subject; 
		private String verb; 
		private String object; 
		private String typeSentence; 
		private String verbTense;
		//for now
		public boolean isValid(){
			return subject != null;
		}
		
		public boolean isValidSubVerbObj(){
			return subject != null && object != null && typeSentence != null; 
		}
		
		public String getSubject(){
			return subject; 
		}
		
		public String getVerb(){
			return verb;
		}
		
		public String getObject(){
			return object;
		}
		
		public String getTypeSentence(){
			return typeSentence;
		}
		
		public String getVerbTense(){
			return verbTense;
		}
	}

	private static final int HTTP_BAD_REQUEST = 400;
	public static void main(String[] args){
		port(getHerokuAssignedPort());
		get("/hello", (request, response) -> "Hello World");
		
		post("/generate-sentence", (request, response) -> {
			Gson gson = new Gson();
			Lexicon lexicon = Lexicon.getDefaultLexicon();
			NLGFactory nlgFactory = new NLGFactory(lexicon);
			Realiser realiser = new Realiser(lexicon);
			System.out.println(request.body());
			try {
				ObjectMapper mapper = new ObjectMapper(); 
				NewSentencePayload symbolsList = mapper.readValue(request.body(), NewSentencePayload.class);
				System.out.println(symbolsList.getObject());
				System.out.println(request.body());
				
				if (!symbolsList.isValid()){
					System.out.println(symbolsList.getObject());
					response.status(HTTP_BAD_REQUEST);
					return "";
				}
				if(symbolsList.getTypeSentence().equals("SubjectVerbObject") && symbolsList.isValidSubVerbObj()){
					SPhraseSpec sentence = nlgFactory.createClause();
					sentence.setSubject(symbolsList.getSubject());
					sentence.setVerb(symbolsList.getVerb());
					sentence.setObject(symbolsList.getObject());
					
					if(!symbolsList.getVerbTense().equals("present")){
						switch(symbolsList.getVerbTense()){
							case "past": 
								sentence.setFeature(Feature.TENSE, Tense.PAST);
								break;
							case "future":
								sentence.setFeature(Feature.TENSE, Tense.FUTURE);
								break;
							case "present_progressive":
								sentence.setFeature(Feature.PROGRESSIVE, true);
								break;
							case "past_progressive": 
								sentence.setFeature(Feature.PROGRESSIVE, true);
								sentence.setFeature(Feature.TENSE, Tense.PAST);
								break;
							case "future_progressive":
								sentence.setFeature(Feature.PROGRESSIVE, true);
								sentence.setFeature(Feature.TENSE, Tense.FUTURE);
								break;
						}
					}
					
					String realizedSentence = realiser.realiseSentence(sentence);
					
					response.status(200);
					response.type("application/json");
					System.out.println(realizedSentence);
					return gson.toJson(realizedSentence);
					
				} else if (symbolsList.getTypeSentence().equals("")) {
					
				}
				
				
				
			} catch(JsonParseException jpe){
				response.status(HTTP_BAD_REQUEST);
				return "";
			}
			
			System.out.println(request.attributes());
			System.out.println(request.body());
			response.body("Hello");
			return "";
		});
	}
	
	static int getHerokuAssignedPort() {
		ProcessBuilder processBuilder = new ProcessBuilder(); 
		if (processBuilder.environment().get("PORT") != null) {
			return Integer.parseInt(processBuilder.environment().get("PORT"));
		}
		return 4567;
	}
}
