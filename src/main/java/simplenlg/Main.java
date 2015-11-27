package simplenlg;
import static spark.Spark.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.List;

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

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
//import 
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
				
				if(isNounPhrase(questionList.getSubject())){
					question.setSubject(nlgFactory.createNounPhrase(questionList.getSubject()));
				} else {
					question.setSubject(questionList.getSubject());
				}
				
				if(isVerbPhrase(questionList.getVerb())){
					question.setVerbPhrase(nlgFactory.createVerbPhrase(questionList.getVerb()));
					//question.setVerbPhrase(questionList.getVerb());
				} else {
					question.setVerb(questionList.getVerb());
				}
				
				if(isNounPhrase(questionList.getObject())){
					question.setObject(nlgFactory.createNounPhrase(questionList.getObject()));
					//question.setObject(questionList.getObject());
				} else {
					question.setObject(questionList.getObject());
				}
				
				
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
				response.header("Access-Control-Allow-Origin", "https://macmania.github.io");
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
				boolean isVerbP = false;
				if(isNounPhrase(symbolsList.getSubject())){
					sentence.setSubject(nlgFactory.createNounPhrase(symbolsList.getSubject()));
				} else {
					sentence.setSubject(symbolsList.getSubject());
				}
				
				if((isVerbP=isVerbPhrase(symbolsList.getVerb()))){
					sentence.setVerbPhrase(nlgFactory.createVerbPhrase(symbolsList.getVerb()));
				} else {
					sentence.setVerb(symbolsList.getVerb());
				}
				
				if(isNounPhrase(symbolsList.getObject())){
					sentence.setObject(nlgFactory.createNounPhrase(symbolsList.getObject()));
				} else {
					sentence.setObject(symbolsList.getObject());
				}
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
				
				if(isVerbP && symbolsList.isVerbParticiple()){
					sentence.setFeature(Feature.PARTICLE, true);
				}
				if(symbolsList.isVerbPassive()){
					sentence.setFeature(Feature.PASSIVE, true);
				}
				if(symbolsList.isVerbProgressive()){
					sentence.setFeature(Feature.POSSESSIVE, true);
				}
				if(symbolsList.isVerbPerfect()){
					sentence.setFeature(Feature.PERFECT, true);
				}
				
				String realizedSentence = realiser.realiseSentence(sentence);
				//response.header("Access-Control-Allow-Origin", "http://localhost:4000");
				response.header("Access-Control-Allow-Origin", "https://macmania.github.io");
				response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
				response.header("Access-Control-Allow-Headers", "Content-Type");
				response.header("Access-Control-Allow-Headers", "negateSentence");
				response.status(200);
				response.type("application/json");
				
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
	
	static boolean isNounPhrase(String phrase) {
		Properties props = new Properties();
		boolean isNounPhrase = false;
		List<String> listTaggers = new ArrayList<String>();
		props.setProperty("annotators", "tokenize, ssplit, pos");
		String tagged = "";
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation = new Annotation(phrase);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sentence : sentences){
			for(CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)){
				tagged = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				
				listTaggers.add(tagged);
				System.out.println(tagged);
			}
		}
		for(int i = 0; i < listTaggers.size(); i++){
			if(listTaggers.get(i).equals("NN") && i != 0 && listTaggers.get(i-1).equals("JJ")){
				isNounPhrase = true; 
			}
			if(listTaggers.get(i).equals("NP"))
				isNounPhrase = true;
		}
		System.out.println("is noun phrase? " + isNounPhrase);
		System.out.println("tagged word " + tagged);
		System.out.println(phrase);
		return isNounPhrase;
	}
	
	static boolean isVerbPhrase(String phrase){
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos");
		String tagged = "";
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation = new Annotation(phrase);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for (CoreMap sentence : sentences){
			for(CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)){
				tagged = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				System.out.println(tagged);
			}
		}
		
		String delim = "/";
 
		String []tokens = tagged.split(delim);
		boolean isVerbPhrase = Arrays.asList(tokens).contains("VP");
		System.out.println("is verb phrase? " + isVerbPhrase);
		System.out.println("tagged word: " + tagged);
		return isVerbPhrase;
	}
}
