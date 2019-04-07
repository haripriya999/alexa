package com.amazonaws.lambda.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
public class QuizSpeechlet implements SpeechletV2 {
	private static final Logger log = LoggerFactory.getLogger(QuizSpeechlet.class);
     private static int flag=0;
     private static int counter=0;
	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
		log.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
				requestEnvelope.getSession().getSessionId());

		// any initialization logic goes here
	}

	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
		log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
				requestEnvelope.getSession().getSessionId());

		String speechOutput = "Welcome to inQUIZitive,Let's play a game to test your general knowledge skills,You can either take a quiz or solve a set of riddles,Before we start with the game,tell me your name,like my name is 'abc'";
		// If the user either does not reply to the welcome message or says
		// something that is not understood, they will be prompted again with this text.
		String repromptText = "For instructions on what you can say, please say help me.";

		// Here we are prompting the user for input
		return newAskResponse(speechOutput, repromptText);
	}

	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
		IntentRequest request = requestEnvelope.getRequest();
		log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
				requestEnvelope.getSession().getSessionId());

		Intent intent = request.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		Session session = requestEnvelope.getSession();
		if("UserInfo".equals(intentName))
		{
			System.out.println("**************user information****************");
			return username(intent,session);
		}
		if ("StartQuiz".equals(intentName)) {
			System.out.println("**************myStartQuizIntent****************");
			return myStartQuizIntent(intent, session);
		} else if ("TopicIntent".equals(intentName)) {
			System.out.println("*****************myStartQuizIntent****************");
			return myTopicIntent(intent, session);}
			else if("RiddlesIntent".equals(intentName))
			{
			return myRiddles(intent,session);
			
		} else if("RepeatIntent".equals(intentName))
		{
			return repeat(intent,session);
		}
			else if("RiddlesAnswerIntent".equals(intentName))
			return riddleAnswer(intent,session);
			else if ("AnswerIntent".equals(intentName)) {
			return myAnswerIntent(intent, session);
		} else if ("YesIntent".equals(intentName)) {
			return myYesIntent(intent, session);
		} else if ("SkipIntent".equals(intentName)) {
			return mySkipIntent(intent, session);
		} else if ("EndQuizIntent".equals(intentName)) {
			return myEndQuizIntent(intent, session);
		} else if ("AMAZON.HelpIntent".equals(intentName)) {
			return getHelp();
		} else if ("AMAZON.StopIntent".equals(intentName)) {
			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText("Goodbye");

			return SpeechletResponse.newTellResponse(outputSpeech);
		} else if ("AMAZON.CancelIntent".equals(intentName)) {
			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText("Goodbye");

			return SpeechletResponse.newTellResponse(outputSpeech);
		} else {
			String errorSpeech = "This is unsupported.  Please try something else.";
			return newAskResponse(errorSpeech, errorSpeech);
		}
	}

	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
		log.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
				requestEnvelope.getSession().getSessionId());
		// any cleanup logic goes here
	}

	/**
	 * Creates a {@code SpeechletResponse} for the HelpIntent.
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getHelp() {
		String speechOutput = "You can say , Start quiz or new quiz, or, you can say stop...";
		String repromptText = "You can say , Start quiz or new quiz, or, you can say stop...";
		return newAskResponse(speechOutput, repromptText);
	}

	/**
	 * Wrapper for creating the Ask response. The OutputSpeech and {@link Reprompt}
	 * objects are created from the input strings.
	 *
	 * @param stringOutput
	 *            the output to be spoken
	 * @param repromptText
	 *            the reprompt for if the user doesn't reply or is misunderstood.
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		outputSpeech.setText(stringOutput);

		PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
		repromptOutputSpeech.setText(repromptText);
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);

		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}

	/* all user defined methods starts here */

	/**
	 * Creates a {@code SpeechletResponse}.
	 *
	 * @param intent
	 *            intent for the request
	 * @return SpeechletResponse spoken and visual response for the given intent
	 * 
	 */
	private SpeechletResponse username(Intent intent,final Session session)
	{
	
		Slot name=intent.getSlot("name");
		String uname=name.getValue();
		session.setAttribute("username", uname);
		String resString=null;
		try {
		resString="Hello, "+uname+" do you want to play a quiz,or solve riddles?";
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		String responseText = resString;

		return newAskResponse(responseText, responseText);

	}
	private SpeechletResponse myStartQuizIntent(Intent intent, final Session session) {
		
		System.out.println("========myStartQuizIntent===========");
		// Initialize the session attributes
		Slot catSlot = intent.getSlot("game");
		String category = catSlot.getValue();
		session.setAttribute("category", category);
		session.setAttribute("topic", "");
		session.setAttribute("qcount", 0);
		session.setAttribute("rcount", 0);
		session.setAttribute("score", 0);
		session.setAttribute("totalquestions", 0);
       
		String resString = null;
		try {
			if(category.equalsIgnoreCase("quiz"))
			{
				flag=0;
			resString = "please select the genre of the quiz,You can choose from 1). Movies , 2). Sports , 3).Current Affairs , 4).Kids Quiz";
			}
			else
			{
				flag=1;
				resString="Let's solve riddles!! say, start, to begin";
			}
			} catch (Exception e) {
			e.printStackTrace();
		}

		String responseText = resString;

		return newAskResponse(responseText, responseText);

	}

	/**
	 * Creates a {@code SpeechletResponse}.
	 *
	 * @param intent
	 *            intent for the request
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse myTopicIntent(Intent intent, final Session session) {
      flag=0;
		System.out.println("========myTopicIntent===========");
         
		Slot topicSlot = intent.getSlot("topic");
		if (topicSlot != null && topicSlot.getValue() != null) {
			String topic = topicSlot.getValue();
			session.setAttribute("topic", topic);
			session.setAttribute("qcount", 1);
			int qcount = (int) session.getAttribute("qcount");
			String resString = null;
			//resString ="Let's begin!!You can either say answer to continue, to skip the current question say skip and to end the quiz say exit.";
			try {

				String temp = getQuestion(session, qcount);
				resString = temp + " you can choose an option by saying. my answer is one, or one. or say skip. or end quiz";

			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			String responseText = resString;
			return newAskResponse(responseText, responseText);

		} else {
			return getHelp();
		}

	}

	/**
	 * Creates a {@code SpeechletResponse}.
	 *
	 * @param intent
	 *            intent for the request
	 * @return SpeechletResponse spoken and visual response for the given intent
	 * 
	 */
	private SpeechletResponse myAnswerIntent(Intent intent, final Session session) {
		System.out.println("==============myStartQuizIntent===========");
counter=0;
		Slot answerSlot = intent.getSlot("answer");
		if (answerSlot != null && answerSlot.getValue() != null) {
			int qcount = (int) session.getAttribute("qcount");
			int totalquestions = (int) session.getAttribute("totalquestions");
			String answer = (String) session.getAttribute("ans" + qcount);
			int score = (int) session.getAttribute("score");
			String resString = null;
			String resString1 = null;
			if (answerSlot.getValue().equalsIgnoreCase(answer)) {
				score = score + 1;
				session.setAttribute("score", score);
				resString1="Well done, you have given the right answer!";
			}
			else
			{
				String ans_option=(String)session.getAttribute("answerString");
				resString1="option  "+answer+")."+ans_option+". is the correct answer";
			}
			// check all the questions are completed or not
			if (qcount < totalquestions) {
			
				try {

					resString = resString1+"\n  if you want to continue to the next question,say yes, otherwise no. ?";
				} catch (Exception e) {
					e.printStackTrace();
				}

				String responseText = resString;

				return newAskResponse(responseText, responseText);
			} else {
				return myEndQuizIntent(intent, session);
			}

		} else {

			String topic = (String) session.getAttribute("topic");
			int qcount = (int) session.getAttribute("qcount");

			String resString = getQuestion(session, qcount)
					+ ". you can choose an option like my answer is one. or say skip. or end quiz";

			String responseText = resString;

			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText(responseText);

			SimpleCard card = new SimpleCard();
			card.setTitle("Question");
			card.setContent(responseText);

			return SpeechletResponse.newTellResponse(outputSpeech, card);
		}

	}
	
	
	private SpeechletResponse riddleAnswer(Intent intent, final Session session) {
		System.out.println("==============RiddleIntent===========");
          counter=0;
		Slot answerSlot = intent.getSlot("ranswer");
		if (answerSlot != null && answerSlot.getValue() != null) {
			int rcount = (int) session.getAttribute("rcount");
			int totalquestions = (int) session.getAttribute("totalquestions");
			String answer = (String) session.getAttribute("ans" + rcount);
			System.out.println(answer);
			System.out.println(answerSlot);String resString1=null;
			int score = (int) session.getAttribute("score");
			String answer1=answer.toLowerCase();
			if (answer1.contains(answerSlot.getValue())||answerSlot.getValue().contains(answer1)) {
				score = score + 1;
				System.out.println("==============entering if loop to check===========");
				session.setAttribute("score", score);
				resString1="Well done, you have given the right answer! ";
			}
			else
			{
				resString1="Correct answer to the riddle is ,"+ answer;
			}
			// check all the questions are completed or not
			if (rcount < totalquestions) {
				String resString = null;
				try {

					resString = resString1+", continue to the next question? say yes. or no. ?";
					
				} catch (Exception e) {
					System.out.println("=============Exception===========");
					e.printStackTrace();
				}

				String responseText = resString;

				return newAskResponse(responseText, responseText);
			} else {
				return myEndQuizIntent(intent, session);
			}

		} else {
			System.out.println("==============else if no answer===========");
			String topic = (String) session.getAttribute("riddle_topic");
			int rcount = (int) session.getAttribute("rcount");

			String resString = getQuestion(session, rcount)
					+ ". you can choose an option like my answer is one. or say skip. or end quiz";

			String responseText = resString;

			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText(responseText);

			SimpleCard card = new SimpleCard();
			card.setTitle("Question");
			card.setContent(responseText);

			return SpeechletResponse.newTellResponse(outputSpeech, card);
		}

	}

	/**
	 * Creates a {@code SpeechletResponse}.
	 *
	 * @param intent
	 *            intent for the request
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse myYesIntent(Intent intent, final Session session) {
		int qcount , totalquestions;String topic;
		if(flag==0) {
		topic = (String) session.getAttribute("topic");
		 totalquestions = (int) session.getAttribute("totalquestions");
	qcount = (int) session.getAttribute("qcount");
}
else
{
	 topic = (String) session.getAttribute("riddle_topic");
 totalquestions = (int) session.getAttribute("totalquestions");
	 qcount = (int) session.getAttribute("rcount");
	 System.out.println("==============Yes intent of riddles===========");
}
		// check all the questions are completed or not
		if (qcount < totalquestions) {
			if(flag==0) {
			session.setAttribute("qcount", qcount + 1);}
			else
			{
				session.setAttribute("rcount", qcount + 1);
			}
			String resString = null;
			try {

				if(flag==0) {resString = getQuestion(session, (qcount + 1)) + ". choose your answer. or skip. or end quiz";}
				else
				{
					resString = getRiddle(session, (qcount + 1))+" tell your answer,skip or end";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			String responseText = resString;
			return newAskResponse(responseText, responseText);

		} else {
			return myEndQuizIntent(intent, session);
		}

	}

	/**
	 * Creates a {@code SpeechletResponse}.
	 *
	 * @param intent
	 *            intent for the request
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse mySkipIntent(Intent intent, final Session session) {

		int totalquestions = (int) session.getAttribute("totalquestions");int qcount;
		if(flag==0) {
		qcount = (int) session.getAttribute("qcount");
		}
		else {
			qcount = (int) session.getAttribute("rcount");
		}
			
		// check all the questions are completed or not
		if (qcount < totalquestions) {
			if(flag==0) {
			session.setAttribute("qcount", qcount + 1);}
			else
			{
				session.setAttribute("rcount", qcount + 1);
			}
			String resString = null;
			try {
				
				//String topic = (String) session.getAttribute("topic");
				if(flag==0) {
				resString = getQuestion(session, (qcount + 1)) + ". choose your answer. or skip. or end quiz";}
				else
				{
				resString=getRiddle(session,(qcount+1));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			String responseText = resString;

			return newAskResponse(responseText, responseText);

		} else {
			return myEndQuizIntent(intent, session);
		}

	}
	private SpeechletResponse repeat(Intent intent,final Session session)
	{

 System.out.println("in my riddles method");
 counter=1;
		//if (topicSlot != null && topicSlot.getValue() != null) {
			//String topic = topicSlot.getValue();
			//session.setAttribute("riddle_topic",topic);
			//session.setAttribute("rcount", 1);
 int rcount;
 if(flag==0) {
	      
			rcount = (int) session.getAttribute("qcount");
			}
 else
 {
	 rcount = (int) session.getAttribute("rcount");
 }
			String resString = null;String temp;
			//resString ="Let's begin!!You can either say answer to continue, to skip the current question say skip and to end the quiz say exit.";
			try {
if(flag==1) {
	 
				 temp = getRiddle(session, rcount);
				 counter=0;
				resString = temp;}
else
{
 temp = getQuestion(session, rcount);
 counter=0;
	resString = temp;
}

			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			String responseText = resString;
			return newAskResponse(responseText, responseText);
	}

	/**
	 * Creates a {@code SpeechletResponse}.
	 *
	 * @param intent
	 *            intent for the request
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse myRiddles(Intent intent,final Session session)
	{
		flag=1;
 System.out.println("in my riddles method");
		Slot topicSlot = intent.getSlot("type_riddles");
		if (topicSlot != null && topicSlot.getValue() != null) {
			String topic = topicSlot.getValue();
			//session.setAttribute("riddle_topic",topic);
			session.setAttribute("rcount", 1);
			int rcount = (int) session.getAttribute("rcount");
			String resString = null;
			//resString ="Let's begin!!You can either say answer to continue, to skip the current question say skip and to end the quiz say exit.";
			try {

				String temp = getRiddle(session, rcount);
				resString = temp;

			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			String responseText = resString;
			return newAskResponse(responseText, responseText);

		} else {
			return getHelp();
		}
		
	}
	private String getRiddle(final Session session, int qno) {

		String responseText="";
		System.out.println("====== In getQuestion start=====");
		String jsonTxt = "";
		//String topic = (String) session.getAttribute("riddle_topic");
		//String arr[]=topic.split(" ");
		//String topic_selected=arr[0];

		try {
			Path path = Paths.get(QuizSpeechlet.class.getResource("/").toURI());

			// The path for json file in Lambda Instance -
			String resourceLoc = path + "/resources/" +"random"+ ".json";

			File f = new File(resourceLoc);
			if (f.exists()) {

				InputStream is;
				String responseString = "";
				try {
					is = new FileInputStream(f);

					BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					String inputLine;
					StringBuffer response = new StringBuffer();
					while ((inputLine = streamReader.readLine()) != null) {
						response.append(inputLine);
					}
					responseString = response.toString();
				} catch (Exception e) {
					System.out.println("file does not exists");
					System.out.println(resourceLoc);
					responseText = " no questions available in this topic";
				}

				// print in String
				// System.out.println(" ---getJsonData--- " + responseString);
				jsonTxt = responseString;// returns JSONArray as a string

				// converting string to JSONArray
				JSONArray allQuestions = new JSONArray(jsonTxt);
				session.setAttribute("totalquestions",10);

				try {
					JSONObject question;
					if(counter==1)
					{
						int index1=(int)session.getAttribute("curindex");
						 question = (JSONObject) allQuestions.get(index1 - 1);
					}
					else{
						int random_numb=(int)(Math.random()*100);
						while(random_numb>120)
						{
							 random_numb=(int)(Math.random()*100);
						}
					session.setAttribute("curindex",random_numb);
					 question = (JSONObject) allQuestions.get(random_numb - 1);
					 }
					//JSONArray alloptions = (JSONArray) question.get("options");
					//String optionsText = "";
					//for (int i = 0; i < alloptions.length(); i++) {
						//optionsText = optionsText + (i + 1) + " . " + alloptions.getString(i) + ". ";
					//}
					session.setAttribute("ans" + qno, question.getString("answer"));
					responseText = "Here is your " + "riddle " + (qno) + " . " + question.getString("riddle");
						//	+ ". The options are " + optionsText;
				} catch (Exception e) { 
					return responseText;
				}

			} else {
				System.out.println("file does not exists");
				System.out.println(resourceLoc);
				responseText = "no questions available in this topic";
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			responseText = "no questions available in this topic";
		}

		System.out.println(responseText);
		System.out.println("====== In getQuestion end =====");
		return responseText;
	}
	private SpeechletResponse myEndQuizIntent(Intent intent, final Session session) {
		
		String uname=(String)session.getAttribute("username");
		/*if(flag==0) {
		System.out.println("$$$$$$$$$$ myEndQuizIntent$$$$$$$$$$$$");
		}*/
    /* else
     {
    	 topic = (String) session.getAttribute("riddle_topic");
     }*/
		if (flag==1 ) {
			System.out.println("$$$$$$$$$$$ topic not set $$$$$$$$$$$");
			String resString = null;
			try{int score = (int) session.getAttribute("score");
			int qcount = (int) session.getAttribute("rcount");
			if(score<=5)
			{
				resString="Thank you "+uname+" . You have scored " + score + " on "
						+ (qcount)+", better luck next time. if you want to play another game of quiz say start quiz,to play a game of riddles say start riddles .or say stop";
			}
			else if(score>5&&score<8)
			{
				resString = "Thank you "+uname+". . You have scored " + score + " on "
						+ (qcount)+" ,if you want to play another game of quiz say start quiz,to play a game of riddles say start riddles .or say stop";
						
			}
			else if(score==8||score==9)
			{
				resString="Well Played "+uname+". You have scored "+score+" on "+qcount+" in your riddles game! ,if you want to play another game of quiz say start quiz,to play a game of riddles say start riddles .or say stop ";
			}
			else
			{
				resString="Congratulations "+uname+". You have done it!!You are an entusiastic learner "+", You have scored "+score+" on "+qcount+" in your riddles game! ,if you want to play another game of quiz say start quiz,to play a game of riddles say start riddles .or say stop ";
			}}
			catch (Exception e) {
				e.printStackTrace();
		}
			String responseText = resString;
			return newAskResponse(responseText, responseText);
		}
			else {
			String topic;
			topic = (String) session.getAttribute("topic");
			String resString = null;
			if(topic==null)
			{
				System.out.println("no topic set");
			}
			if (topic.length() > 0) {
			try {
			
				int score = (int) session.getAttribute("score");
				int qcount = (int) session.getAttribute("qcount");
				if(score<=5)
				{
					resString="Thank you "+uname+" Your quiz on topic. " + topic + ". is over. You have scored " + score + " on "
							+ (qcount)+", i look forward to help you work on your general knowledge skills,better luck next time. if you want to play another game of quiz say start quiz,to play a game of riddles say start riddles .or say stop";
				}
				else if(score>5&&score<8)
				{
					resString = "Thank you "+uname+". Your quiz on topic. " + topic + ". is over. You have scored " + score + " on "
							+ (qcount)+", if you want to play another game of quiz say start quiz,to play a game of riddles say start riddles .or say stop";
							
				}
				else if(score==8||score==9)
				{
					resString="Well Played "+uname+". You have scored "+score+" on "+qcount+" in your quiz on topic"+topic+"!, if you want to play another game of quiz say start quiz,to play a game of riddles say start riddles .or say stop ";
				}
				else
				{
					resString="Congratulations "+uname+". You have done it!!You are an entusiastic learner "+". You have scored "+score+" on "+qcount+" in your quiz on topic"+topic+"! ,if you want to play another game of quiz say start quiz,to play a game of riddles say start riddles .or say stop ";
				}
				
				} catch (Exception e) {
				e.printStackTrace();
			}
			
			String responseText = resString;
			return newAskResponse(responseText, responseText);
			} else {
			return getHelp();
		}}

	}
/*private  SpeechletResponse choice(Intent intent,final Session session)
{
	Slot choiceSlot = intent.getSlot("choice");
	String resString="";
	if(choiceSlot!=null&&choiceSlot.getValue()!=null)
	{
		String choice = choiceSlot.getValue();
		switch(choice) {
		case "answer":try {

			String temp = getQuestion(session, qcount);
			resString = temp + " you can choose an option by saying. my answer is one. or say skip. or end quiz";

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		String responseText = resString;
		return newAskResponse(responseText, responseText);
		case "skip":
		case "quit":
	
	}

		}
	 else {
			return getHelp();
	}
	
}*/
	/**
	 * returns a question
	 * 
	 */
	private String getQuestion(final Session session, int qno) {

		String responseText = "";
		System.out.println("====== In getQuestion start=====");
		String jsonTxt = "";
		String topic = (String) session.getAttribute("topic");
		String topic_split[]=topic.split(" ");
		String res="";
		for(int i=0;i<topic_split.length;i++)
		{
			res=res+topic_split[i].toLowerCase();
		}

		try {
			Path path = Paths.get(QuizSpeechlet.class.getResource("/").toURI());

			// The path for json file in Lambda Instance -
			String resourceLoc = path + "/resources/" + res + ".json";

			File f = new File(resourceLoc);
			if (f.exists()) {

				InputStream is;
				String responseString = "";
				try {
					is = new FileInputStream(f);

					BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					String inputLine;
					StringBuffer response = new StringBuffer();
					while ((inputLine = streamReader.readLine()) != null) {
						response.append(inputLine);
					}
					responseString = response.toString();
				} catch (Exception e) {
					System.out.println("file does not exists");
					System.out.println(resourceLoc);
					responseText = "no questions available in this topic";
				}

				// print in String
				// System.out.println(" ---getJsonData--- " + responseString);
				jsonTxt = responseString;// returns JSONArray as a string

				// converting string to JSONArray
				JSONArray allQuestions = new JSONArray(jsonTxt);
				session.setAttribute("totalquestions",10);

				try {
					JSONObject question;
					JSONArray alloptions;
					if(counter==1)
					{
						int index=(int)session.getAttribute("currindex");
						question = (JSONObject) allQuestions.get(index - 1);
						 alloptions = (JSONArray) question.get("options");
					}else {
					 int randno=(int)(Math.random()*100);
					 while(randno>100)
					 {
						 randno=(int)(Math.random()*100);
					 }
					 session.setAttribute("currindex", randno);
					 question = (JSONObject) allQuestions.get(randno - 1);
					 alloptions = (JSONArray) question.get("options");}
					String optionsText = "";
					
					for (int i = 0; i < alloptions.length(); i++) {
						optionsText = optionsText + (i + 1) + " ) " + alloptions.getString(i) +".  "+ "\n";
					}
					session.setAttribute("ans" + qno, question.getString("answer"));
					int in=Integer.parseInt(question.getString("answer"));
					String ans_option=alloptions.getString(in-1);
					session.setAttribute("answerString",ans_option);
					/*if(question.getString("question").equals("audio_question"))
					{
						   String audioURL = "https://s3.amazonaws.com/audioquiz/audio_question"+(qno-1)+".mp3";
						   
					}else {*/
					responseText = "Here is your " + "question " + (qno) + ". " + question.getString("question")
							+ ". The options are " + optionsText;
				} catch (Exception e) { 
					return responseText;
				}

			} else {
				System.out.println("file does not exists");
				System.out.println(resourceLoc);
				responseText = "no questions available in this topic";
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			responseText = "no questions available in this topic";
		}

		System.out.println(responseText);
		System.out.println("====== In getQuestion end =====");
		return responseText;
	}

	public static JSONArray shuffleJsonArray(JSONArray array) throws JSONException {
		// Implementing Fisherâ€“Yates shuffle
		Random rnd = new Random();
		for (int i = array.length() - 1; i >= 0; i--) {
			int j = rnd.nextInt(i + 1);
			// Simple swap
			Object object = array.get(j);
			array.put(j, array.get(i));
			array.put(i, object);
		}
		return array;
	}

}

