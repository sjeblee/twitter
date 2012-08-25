/** Merge - TWITTER VERSION
* Merges different annotations of the same sentence
* @author Serena Jeblee (sjeblee1@jhu.edu)
* Last modified on 27 July 2012
*/

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Merge{

public static void main(String[] args){

	if(args.length < 1){
		System.out.println("Usage: java ProcessResults [annotation-file]");
		System.exit(1);
	}
	
	long time1 = System.currentTimeMillis();

	String filename = args[0];

	try{
		File f = new File(filename);
		FileWriter out = new FileWriter("temp.txt");

		int linenum = 0;
		int lastcopied = -1;
		boolean done = false;
		ArrayList<Integer> linestoskip = new ArrayList<Integer>();

		while(!done){
			Scanner infile = new Scanner(new FileReader(filename));
			linenum = 0;
			
			//skip to current line
			//System.out.print("Scrolling through lines: ");
			while(infile.hasNextLine() && (linenum < lastcopied)){
				infile.nextLine();
				linenum++;
				//System.out.print(linenum + ", ");
			}
			//System.out.println();

			if(!infile.hasNextLine())
				done = true;
			else if(linestoskip.contains(linenum+1)){ //skip if we've already matched it
					lastcopied = linenum+1;
					//System.out.println("- lastcopied = " + lastcopied);
					//System.out.println("Skipping line " + lastcopied);
			}
			else{
				//System.out.println("Processing line " + (linenum+1));
				String line = infile.nextLine();
				linenum++;				
				StringTokenizer tok = new StringTokenizer(line, "\t");
				String ann = tok.nextToken();
				if(ann.contains("most") || ann.contains("mixed") || ann.contains("little")){
					int index = ann.indexOf('-');
					ann = ann.substring(index+1) + "-";
				}
				String langs = tok.nextToken();
				langs = langs.substring(1, langs.length()-1); // ignore []
				String tweetid = tok.nextToken();
				String text = tok.nextToken();

				//search for text
				lastcopied = linenum;
				//System.out.println("- lastcopied = " + lastcopied);
				while(infile.hasNextLine()){
					String matchline = infile.nextLine();
					linenum++;
					StringTokenizer tok2 = new StringTokenizer(matchline, "\t");
					String matchann = tok2.nextToken();
					String matchlangs = tok2.nextToken();
					String matchtweet = tok2.nextToken();
					String matchtext = tok2.nextToken();
					//System.out.println("Comparing " + text + "\n\tand " + matchtext);
					if(tweetid.equals(matchtweet)){ //match found!
						//System.out.println("Merging " + tweetid + "(" + lastcopied 
						//		+ ") and " + matchtweet + "(" + linenum + ")");
						if(matchann.contains("most") || matchann.contains("mixed") || matchann.contains("little")){
							int index = matchann.indexOf('-');
							matchann = matchann.substring(index+1) + "-";
						}
						ann += matchann; //add annotation
						matchlangs = matchlangs.substring(1, matchlangs.length()-1); // ignore []
						String[] langstomatch = matchlangs.split(",");
						for(String sl : langstomatch){ //merge languages
							if(!langs.contains(sl))
								langs += "," + sl;
						}
						linestoskip.add(linenum); //save line number so we skip it later
					}
				}//end while

				out.write(ann + "\t[" + langs + "]\t" + tweetid + "\t" + text + "\n");
			}//end else

			infile.close();
		}//end while

		out.close();

		//System.out.println("linestoskip: ");
		//for(Integer i : linestoskip){
		//	System.out.print(i + ", ");
		//}
		//System.out.println();

		//move temp to original file
		boolean deleted = f.delete(); //delete original
		File file = new File("temp.txt");
		File file2 = new File(filename);
		boolean success = file.renameTo(file2);
		if((!success)||(!deleted))
			System.out.println("ERROR: temp file NOT successfully swapped.");

	}//end try
	catch(IOException e){
		System.out.println("ERROR: " + e.getMessage());
	}
	
	long time2 = System.currentTimeMillis();
	double timems = (double) time2 - time1;
	double time = timems/1000;
	int mins = (int) Math.floor(time/60.0);
	int secs = (int) time-(60*mins);
	System.out.printf("Done: took %d mins %d seconds (%.2f ms)\n", mins, secs, timems);

	}//end main
}










