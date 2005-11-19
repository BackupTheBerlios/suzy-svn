package de.berlios.suzy.irc.plugin;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.Plugin;

import java.util.Arrays;

/**
 *
 * @author fx21
 */

public class DicePlugin implements Plugin {

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getCommands()
     */
    public String[] getCommands() {
        return new String[] {
                "roll"
        };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getRestrictedCommands()
     */
    public String[] getRestrictedCommands() {
        return new String[] {
        };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#handleEvent(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public void handleEvent(IrcCommandEvent ice) {
        if (ice.getCommand().equals("roll")) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), 
					  MessageTypes.PRIVMSG,
					  roll(ice.getMessageContent()));
        }
    }

    public String[] getHelp(IrcCommandEvent ice) {
        // TODO Auto-generated method stub
        return null;
    }

    private String roll(String dice)
    {

	StringBuilder sb = new StringBuilder();
	sb.append("roll:");

	String[] diceArray = dice.split(" +");

	for(String s : diceArray)
	    {
		sb.append(" ").append(rollDice(s));
	    }

	return sb.toString();
    }

    private String rollDice(String dice)
    {
	if(dice.matches("\\d*d\\d+"))
	    return rollNumericDice(dice);
	else if(dice.matches("\\d*d\\{.*\\}"))
	    return rollSpecialDice(dice);
	else if(dice.matches("\\d*d\\[\\d+,\\d+\\]"))
	    return rollRangedDice(dice);
	else
	    return dice;
    }

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
    private String rollSpecialDice(String dice)
    {
	int indexOfD = dice.indexOf('d');
	int numberOfDice = Integer.parseInt(dice.substring(0,indexOfD));
	String[] list = dice.substring(indexOfD+2,dice.length()-1).split(",");

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

    private String rollRangedDice(String dice)
    {
	int indexOfD = dice.indexOf('d');
	int numberOfDice = Integer.parseInt(dice.substring(0,indexOfD));
	String[] list = dice.substring(indexOfD+2,dice.length()-1).split(",");
	int minValue = Integer.parseInt(list[0]);
	int maxValue = Integer.parseInt(list[1]);


	int[] values = new int[numberOfDice];
	int sum = 0;

	for(int i = 0; i < numberOfDice; i++)
	    {
		int value = (int)(Math.random()*(maxValue-minValue+1)
				  +minValue);
		values[i] = value;
		sum += value;
	    }

	if(numberOfDice > 1)
	    return Arrays.toString(values)+" (sum: "+sum+")";
	else
	    return String.valueOf(sum);
    }


}
