/** FixTweetsMore for data with user and location
* Makes tweets one line each and gets rid of empty lines
* @author Serena Jeblee (sjeblee1@jhu.edu)
* Last modified on 8 August 2012
*/

import java.io.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Scanner;

public class FixTweetsMore{

public static void main(String[] args){

	if(args.length < 1){
		System.out.println("Usage: java FixTweetsMore [input-file]");
		System.exit(1);
	}

	String filename = args[0];
	
	try{
		Scanner infile = new Scanner(new FileReader(filename));
		FileWriter out = new FileWriter(filename + ".fixed");
		String outline = "";
		boolean first = true;
		boolean repeat = false;
		int numdup = 0;
		ArrayList<String> ids = new ArrayList<String>();

		while(infile.hasNextLine()){
			String line = infile.nextLine();
			StringTokenizer tok = new StringTokenizer(line);
			if(tok.hasMoreTokens()){
				String idnum = tok.nextToken();
				int id = -1;
				try{
					if(idnum.length()<4)
						throw new NumberFormatException();
					id = Integer.parseInt(idnum.substring(0,4)); //found new tweet
					String user = "[missed]";
					String loc = "[missed]";
					//	try{
					    user = tok.nextToken();
					    loc = tok.nextToken();
					    //	}
					//			catch(NoSuchElementException nsee){
					//  System.out.println("NSEE: " + line);
					//}
					if(ids.contains(idnum)){
						repeat = true;
						numdup++;
					}
					else{
						ids.add(idnum);
						repeat = false;
					}

					if(first)
						first = false;
					else if(!repeat)
						out.write(outline + "\n");
					if(!repeat)
						outline = idnum + "\t" + user + "\t" + loc + "\t";
				}
				catch(NumberFormatException nfe){//continued tweet
					if(!repeat)
						outline += " " + idnum;
				}
				catch(NoSuchElementException nsee){
				    tok = new StringTokenizer(line);
				    if(!repeat){
					outline += " " + tok.nextToken();
				    }
				}
	
				if(!repeat)
					while(tok.hasMoreTokens())
						outline += " " + tok.nextToken();
			}//end if tok has tokens

		}//end while

		infile.close();
		out.close();
		System.out.println(numdup + " duplicates removed");

	}//end try
	catch(IOException e){
		System.out.println("ERROR: " + e.getMessage());
	}

}//end main

}









