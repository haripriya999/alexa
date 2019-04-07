package com.amazonaws.lambda.demo;


import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
public class QuizSpeechletRequestStreamHandler  extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds;
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds = new HashSet<String>();
        supportedApplicationIds.add("amzn1.ask.skill.461a1250-980c-473c-a87f-ba23253a9679");
    }

    public QuizSpeechletRequestStreamHandler() {
        super(new QuizSpeechlet(), supportedApplicationIds);
    }
}
