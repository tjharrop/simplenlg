package simplenlg;
import static spark.Spark.*;

import org.eclipse.jetty.server.Response;

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
	static class NewSimpleNLGSentencePayload {
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
	
	//interrogative
	static class QuestionSentence extends NewSimpleNLGSentencePayload{
		private String typeQuestion; 
		
		public String getTypeQuestion(){
			return typeQuestion;
		} 
	}
	
	//static class ModifierSentence

	private static final int HTTP_BAD_REQUEST = 400;
	public static void main(String[] args){
		
		String origin = "http://localhost:4000";
		
		port(getHerokuAssignedPort());
		get("/hello", (request, response) -> "Hello World");
		
		options("/generate-sentence", (request, response)->{
			if(request.host().equals("http://localhost:4000")){
				response.header("Access-Control-Allow-Origin", "http://localhost:4000");
			}
			response.header("Allow-Control-Allow-Origin", "http://macmania.github.io");
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", "Content-Type");
			response.header("Access-Control-Allow-Headers", "negateSentence");
			System.out.println(request.body());
			return "hello";
		});
		options("/generate-question", (request, response)->{
			response.header("Allow-Control-Allow-Origin", "http://macmania.github.io");
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", "Content-Type");
			return "";
		});
		
		post("/generate-question", (request, response) ->{
			Gson gson = new Gson(); 
			Lexicon lexicon = Lexicon.getDefaultLexicon(); 
			NLGFactory nlgFactory = new NLGFactory(lexicon); 
			Realiser realiser = new Realiser(lexicon); 
			
			try {
				ObjectMapper mapper = new ObjectMapper(); 
				
				QuestionSentence questionList = mapper.readValue(request.body(), QuestionSentence.class);
				
				SPhraseSpec question = nlgFactory.createClause();
				question.setSubject(questionList.getSubject());
				question.setVerb(questionList.getVerb());
				question.setObject(questionList.getObject());
				
				switch (questionList.getTypeQuestion()){
					case "what_sub":
						question.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_SUBJECT);
						break;
					case "what_obj":
						question.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_OBJECT);
						break;
					case "how":
						question.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW);
						break;
					case "how_pred":
						question.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW_PREDICATE);
						break;
					case "where":
						question.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHERE);
						break;
					case "who_indirect":
						question.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_INDIRECT_OBJECT);
						break;
					case "who_obj":
						question.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_OBJECT);
						break;
					case "who_sub":
						question.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_SUBJECT);
						break;
					case "why":
						question.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHY);
						break;
					case "yes_no":
						question.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.YES_NO);
						break;
					case "how_many":
						question.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW_MANY);
						break;
				}
				String realizedQuestion = realiser.realiseSentence(question);
				//response.header("Access-Control-Allow-Origin", "http://localhost:4000");
				response.header("Access-Control-Allow-Origin", "http://macmania.github.io");
				response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
				response.header("Access-Control-Allow-Headers", "Content-Type");
				response.header("Access-Control-Allow-Headers", "negateSentence");
				response.status(200);
				response.type("application/json");
				
				System.out.println(realizedQuestion);
				return gson.toJson(realizedQuestion);
			} catch(JsonParseException jpe){
				response.status(HTTP_BAD_REQUEST);
				return "";
			}
		});
		
		post("/generate-sentence", (request, response) -> {			
			Gson gson = new Gson();
			Lexicon lexicon = Lexicon.getDefaultLexicon();
			NLGFactory nlgFactory = new NLGFactory(lexicon);
			Realiser realiser = new Realiser(lexicon);
			System.out.println(request.body());
			try {
				ObjectMapper mapper = new ObjectMapper(); 
				
				NewSimpleNLGSentencePayload symbolsList = mapper.readValue(request.body(), NewSimpleNLGSentencePayload.class);
				System.out.println(symbolsList.getObject());
				System.out.println(request.body());
				
				if (!symbolsList.isValid()){
					System.out.println(symbolsList.getObject());
					response.status(HTTP_BAD_REQUEST);
					return "";
				}
				if(symbolsList.getTypeSentence().equals("SubjectVerbObject") && symbolsList.isValidSubVerbObj()) {
					
					SPhraseSpec sentence = nlgFactory.createClause();
					sentence.setSubject(symbolsList.getSubject());
					sentence.setVerb(symbolsList.getVerb());
					sentence.setObject(symbolsList.getObject());
					if(request.headers().contains("negateSentence") &&  request.headers("negateSentence").equals("True")){
						sentence.setFeature(Feature.NEGATED, true);
					}
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
					//response.header("Access-Control-Allow-Origin", "http://localhost:4000");
					response.header("Access-Control-Allow-Origin", "http://macmania.github.io");
					response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
					response.header("Access-Control-Allow-Headers", "Content-Type");
					response.header("Access-Control-Allow-Headers", "negateSentence");
					response.status(200);
					response.type("application/json");
					
					//Response.SC_ACCEPTED;
					System.out.println(realizedSentence);
					return gson.toJson(realizedSentence);	
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
	static String getOrigin(){
		ProcessBuilder processBuilder = new ProcessBuilder();
		if (processBuilder.environment().get("PORT") != null) {
			return "http://macmania.github.io"; 
		}
		else {
			return "http://localhost:4000";
		}
	}
	
	static int getHerokuAssignedPort() {
		ProcessBuilder processBuilder = new ProcessBuilder(); 
		if (processBuilder.environment().get("PORT") != null) {
			return Integer.parseInt(processBuilder.environment().get("PORT"));
		}
		return 4567;
	}
}
