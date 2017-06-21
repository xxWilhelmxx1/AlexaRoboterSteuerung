package robotcontroller;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

/**
 * This class could be the handler for an AWS Lambda function powering an Alexa Skills Kit
 * experience. To do this, simply set the handler field in the AWS Lambda console to
 * "helloworld.HelloWorldSpeechletRequestStreamHandler" For this to work, you'll also need to build
 * this project using the {@code lambda-compile} Ant task and upload the resulting zip file to power
 * your function.
 */
public final class RobotControllerSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds = new HashSet<String>();
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
    	// amzn1.ask.skill.98baae62-b5ab-4ba2-b548-b1de8067df0a
        supportedApplicationIds.add("amzn1.ask.skill.98baae62-b5ab-4ba2-b548-b1de8067df0a");
       
    }

    public RobotControllerSpeechletRequestStreamHandler() {
        super(new RobotControllerSpeechlet(), supportedApplicationIds);
    }
}
