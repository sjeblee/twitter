/** Count Annotations - TWITTER VERSION
* Counts different annotations of the same sentence
* @author Serena Jeblee (sjeblee1@jhu.edu)
* Last modified on 27 July 2012
*/

import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class CountAnns{

public static void main(String[] args){

	if(args.length < 1){
		System.out.println("Usage: java CountAnns [annotation-file]");
		System.exit(1);
	}

	String filename = args[0];

	try{
		Scanner infile = new Scanner(new FileReader(filename));
		FileWriter out = new FileWriter(filename + ".counted");

		while(infile.hasNextLine()){
			String line = infile.nextLine();				
			StringTokenizer tok = new StringTokenizer(line, "\t");
			String ann = tok.nextToken();
			String langs = tok.nextToken();
			String tweetid = tok.nextToken();
			String text = tok.nextToken();

			int diacount = 0;

			//normalize langs
			String langstring = langs.substring(1, langs.length()-1); // cut off []
			String[] langarr = langstring.split(",");
			ArrayList<String> arrlist = new ArrayList<String>();
			for(int s=0; s<langarr.length; s++){
				String lang = langarr[s].toLowerCase();
				if(!arrlist.contains(lang))
					arrlist.add(lang);
			}
			langarr = new String[arrlist.size()];
			langarr = arrlist.toArray(langarr);
			Arrays.sort(langarr);
			String newlangs = "[";
			for(int t=0; t<langarr.length; t++){
				if(t != 0)
					newlangs += ",";
				newlangs += langarr[t];
			}
			newlangs += "]";

			//count annotations
			StringTokenizer anntok = new StringTokenizer(ann, "-");
			String ann1 = anntok.nextToken();
			String ann2 = "prompt";
			String ann3 = "prompt";
			if(anntok.hasMoreTokens())
				ann2 = anntok.nextToken();
			if(anntok.hasMoreTokens())
				ann3 = anntok.nextToken();

			if(ann1.equals("egyptian") || ann1.equals("gulf") || ann1.equals("levantine")
			  || ann1.equals("iraqi") || ann1.equals("maghrebi") || ann1.equals("general")){
				diacount++;
			}
			if(ann2.equals("egyptian") || ann2.equals("gulf") || ann2.equals("levantine")
			  || ann2.equals("iraqi") || ann2.equals("maghrebi") || ann2.equals("general")){
				diacount++;
			}
			if(ann3.equals("egyptian") || ann3.equals("gulf") || ann3.equals("levantine")
			  || ann3.equals("iraqi") || ann3.equals("maghrebi")  || ann3.equals("general")){
				diacount++;
			}

			out.write(diacount + "D_" + ann.substring(0, ann.length()-1) + "\t" + newlangs + "\t" + tweetid + "\t" + text + "\n");

		}//end while

		infile.close();
		out.close();

	}//end try
	catch(IOException e){
		System.out.println("ERROR: " + e.getMessage());
	}

}//end main
}
