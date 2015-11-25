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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

//@Data
class NewSimpleNLGSentencePayload {
	private String subject; 
	private String verb; 
	private String object; 
	private String typeSentence; 
	private String verbTense;
	private String isProgressive;
	private String isModel;
	private String isParticiple;
	private String isPerfect;
	private String isPassive;
	private String negateSentence;
//	public NewSimpleNLGSentencePayload(String sub, String verb, String obj, String typeSentence,
//				String verbTense, String isProgressive, String isModel, String isParticiple, 
//				String isPerfect, String isPassive){
//		subject = sub;
//		this.verb = verb;
//		object = obj;
//		this.typeSentence = typeSentence;
//		this.verbTense = verbTense; 
//		this.isProgressive = isProgressive.equals("True");
//		this.isModel = isModel.equals("True"); 
//		this.isParticiple = isParticiple.equals("True");
//		this.isPerfect = isPerfect.equals("True");
//		this.isPassive = isPassive.equals("True");
//	}
	
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
	
	public boolean isVerbProgressive() {
		return isProgressive.equals("True");
	}
	
	public boolean isVerbModal() {
		return isModel.equals("True");
	}
	
	public boolean isVerbParticiple() {
		return isParticiple.equals("True");
	}
	
	public boolean isVerbPassive() {
		return isPassive.equals("True");
	}
	
	public boolean isVerbPerfect() {
		return isPerfect.equals("True");
	}
	
	public boolean negateSentence(){
		return this.negateSentence.equals("True");
	}
}

//interrogative
class QuestionSentence extends NewSimpleNLGSentencePayload{
	private String typeQuestion; 
	
	public String getTypeQuestion(){
		return typeQuestion;
	} 
}

public class Main {
	

	
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
			response.header("Access-Control-Allow-Origin", "http://macmania.github.io");
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", "Content-Type");
			response.header("Access-Control-Allow-Headers", "negateSentence");
			System.out.println(request.body());
			return "hello";
		});
		options("/generate-question", (request, response)->{
			response.header("Access-Control-Allow-Origin", "http://macmania.github.io");
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
			

			Gson jsonToJava = new GsonBuilder().create();
			
			NewSimpleNLGSentencePayload symbolsList = jsonToJava.fromJson(request.body(), NewSimpleNLGSentencePayload.class);


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
					}
				}
				
				if(symbolsList.negateSentence()){
					sentence.setFeature(Feature.NEGATED, true);
				}
				if(symbolsList.isVerbModal()){
					sentence.setFeature(Feature.MODAL, true);
				}
//					if(symbolsList.isVerbParticiple()){
//						sentence.setFeature(Feature.P;
//					}
				if(symbolsList.isVerbPassive()){
					sentence.setFeature(Feature.PASSIVE, true);
				}
				if(symbolsList.isVerbPassive()){
					sentence.setFeature(Feature.PASSIVE, true);
				}
				if(symbolsList.isVerbPerfect()){
					sentence.setFeature(Feature.PERFECT, true);
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
