/** ProcessTweets.java
* @author Serena Jeblee sjeblee1@jhu.edu
*/

import java.io.*;
import java.util.Scanner;
import org.apache.commons.lang3.StringEscapeUtils;

public class ProcessTweets{

public static void main(String[] args){

	if(args.length < 1){
		System.out.println("Usage: java ProcessTweets filename");
		System.exit(1);
	}

	String filename = args[0];
	String outfilename = "twitter_data.txt";
	String junkfilename = "twitter_junk.txt";
	int counter = 0;

	try{
		Scanner infile = new Scanner(new FileReader(filename));
		FileWriter outfile = new FileWriter(outfilename);
		FileWriter junkout = new FileWriter(junkfilename);

		System.out.println("Processing twitter data...");

		while(infile.hasNextLine()){
		    //	if (counter > 26785225){

			if((counter%1000)==0)
				System.out.print(".");
			String line = infile.nextLine();

			//get id
			int index = line.indexOf("\"id\":");
			String id = "[no id found]";
			String text = "[no text found]";

			if(index > -1){
				index += 6;
				int end = line.indexOf(',', index);
				if(end > index)
					id = line.substring(index, end);
			}//end if index > -1	

			//get text
			index = line.indexOf("\"text\": \"");
			if(index > -1){
				index += 9;
				int end = line.indexOf('\"', index);
				if(end > index)
					text = line.substring(index, end);
			}
				
			//check for Arabic characters
			try{
				if(text.indexOf("\\u06") != -1){
					text = StringEscapeUtils.unescapeJava(text);
					outfile.write(id + "\t" + text + "\n");
				}
				else{
					text = StringEscapeUtils.unescapeJava(text);
					junkout.write(id + "\t" + text + "\n");
				}
			}//end try

			catch(NumberFormatException nfe){
				text = "[NumberFormatException!]";
				junkout.write(id + "\t" + text + "\n");
			}
			catch(Exception e){
				text = "[Exception!]";
				junkout.write(id + "\t" + text + "\n");
			}
			
			//	}//end if counter >

			counter++;
		}//end while

		outfile.close();
		infile.close();
		junkout.close();

	}//end try

	catch(FileNotFoundException e){
		System.out.println("FileNotFoundException: " + e.getMessage());
	}
	catch(IOException ioe){
		System.out.println("IOException: " + ioe.getMessage());
	}

	System.out.println("All tweets processed.");

}//end main

}
