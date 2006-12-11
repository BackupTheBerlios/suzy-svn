package de.berlios.suzy.irc.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.Plugin;

/**
 *
 * @author fx21
 */

public class DicePlugin implements Plugin {

    /* command listing
     */
    public String[] getCommands() {
        return new String[] {
                "roll"
        };
    }

    /* restricted command listing (empty)
     */
    public String[] getRestrictedCommands() {
        return new String[] {
        };
    }

    /* respond to "roll" commands
     */
    public void handleEvent(IrcCommandEvent ice) {
        if (ice.getCommand().equals("roll")) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), 
					  MessageTypes.PRIVMSG,
					  roll(ice.getMessageContent()));
        }
    }

    //help message

    public String[] getHelp(IrcCommandEvent ice) {
        // TODO Auto-generated method stub
        return new String[]{
	    "usage: roll {list of dice}",
	    "Dice available: normal: XdY, special: Xd{A,B,C,...}, ranged: Xd[Y,Z]"	
	};
    }

    //main roll method. splits the string up and rolls each dice.
    //puts together the response string

    private String roll(String dice)
    {

	StringBuilder sb = new StringBuilder();
	sb.append("roll:");

	String[] diceArray = splitDice(dice);

	for(String s : diceArray)
	    {
		sb.append(" ").append(rollDice(s));
	    }

	return sb.toString();
    }

    //split the string up in words
    //spaces inside {}'s and []'s are to be ignored

    private String[] splitDice(String dice)
    {

	List<String> ret = new ArrayList<String>();
	
	StringBuilder currentChunk = new StringBuilder();

	boolean ignoreSpaces = false;

	for(int i = 0; i < dice.length(); i++)
	    {
		char c = dice.charAt(i);

		if(c == ' ' && !ignoreSpaces)
		    {
			ret.add(currentChunk.toString());
			currentChunk = new StringBuilder();

		    }
		else
		    {
			currentChunk.append(c);

			if(c == '{' || c == '[')
			    ignoreSpaces = true;
			else if(c == '}' || c == ']')
			    ignoreSpaces = false;

		    }

	    }

	ret.add(currentChunk.toString());

	return ret.toArray(new String[ret.size()]);

    }

    //determines what kind of dice we're rolling and passes that on to
    //the right method

    private String rollDice(String dice)
    {
	if(dice.matches("\\d*d\\d+"))
	    return rollNumericDice(dice);
	else if(dice.matches("\\d*d\\{.*\\}"))
	    return rollSpecialDice(dice);
	else if(dice.matches("\\d*d\\[ *-?\\d+ *, *-?\\d+\\ *]"))
	    return rollRangedDice(dice);
	else
	    return dice;
    }

    /* standard die, XdY
     * rolls X times, yielding a result between 1 and Y each time, and
     * returns the results
     * if more than one die is rolled, each die is displayed and the sum is
     * calculated 
     */

    private String rollNumericDice(String dice)
    {
	int indexOfD = dice.indexOf('d');
	int numberOfDice = Integer.parseInt(dice.substring(0,indexOfD));
	int maxValue = Integer.parseInt(dice.substring(indexOfD+1));

	int[] values = new int[numberOfDice];
	int sum = 0;

	for(int i = 0; i < numberOfDice; i++)
	    {
		int value = (int)(Math.random()*maxValue+1);
		values[i] = value;
		sum += value;
	    }

	if(numberOfDice > 1)
	    return Arrays.toString(values)+" (sum: "+sum+")";
	else
	    return String.valueOf(sum);
    }

    /* die with named sides, Xd{A,B,C,...}
     * rolls X times, yielding a result from the list each time
     * all dice are displayed
     */

    private String rollSpecialDice(String dice)
    {
	int indexOfD = dice.indexOf('d');
	int numberOfDice = Integer.parseInt(dice.substring(0,indexOfD));
	String[] list = dice.substring(indexOfD+2,dice.length()-1)
	    .trim().split(" *, *");

	String[] values = new String[numberOfDice];

	for(int i = 0; i < numberOfDice; i++)
	    {
		int index = (int)(Math.random()*list.length);
		values[i] = list[index];
	    }

	if(numberOfDice > 1)
	    return Arrays.toString(values);
	else
	    return values[0];
    }

    /* ranged die, Xd[Y,Z]
     * rolls X times, yielding a result between Y and Z (inclusive) each time
     * and returns the results
     * if more than one die is rolled, each die is displayed and the sum is
     * calculated 
     */

    private String rollRangedDice(String dice)
    {
	int indexOfD = dice.indexOf('d');
	int numberOfDice = Integer.parseInt(dice.substring(0,indexOfD));
	String[] list = dice.substring(indexOfD+2,dice.length()-1)
	    .trim().split(" *, *");
	int minValue = Integer.parseInt(list[0]);
	int maxValue = Integer.parseInt(list[1]);

	int[] values = new int[numberOfDice];
	int sum = 0;

	for(int i = 0; i < numberOfDice; i++)
	    {
		int value = (int)(Math.random()*(maxValue-minValue+1))
				  +minValue;
		values[i] = value;
		sum += value;
	    }

	if(numberOfDice > 1)
	    return Arrays.toString(values)+" (sum: "+sum+")";
	else
	    return String.valueOf(sum);
    }


}
