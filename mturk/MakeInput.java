/** MakeInput - TWITTER VERSION
* Makes an input file for Mechanical Turk Arabic classification task
* @author Serena Jeblee (sjeblee1@jhu.edu)
* Last modified on 27 July 2012
*
* Specify the number of sentences per HIT on line 33
*/

import java.io.*;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Scanner;
import java.util.List;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import javax.imageio.ImageIO;

public class MakeInput{

public static void main(String[] args){

	if(args.length < 2){
		System.out.println("Usage: java MakeInput [input-file] [output-prefix]");
		System.exit(1);
	}

	int itemsPerLine = 12;	//number of sentences per HIT

	String filename = args[0];
	String outname = args[1];

	try{
		//Open files
		Scanner infile = new Scanner(new FileReader(filename));
		Scanner inmsa = new Scanner(new FileReader("controls-msa.short"));
		Scanner india = new Scanner(new FileReader("controls-dia.short"));
		FileWriter outfile = new FileWriter(outname + ".input");
		FileWriter controlout = new FileWriter(outname + ".controlmap");
		FileWriter imageout = new FileWriter(outname + "-images.input");

		//setup
		int counter = 0;
		int linenum = 1;
		int item = 1;
		boolean begofline = true;
		String buffer = "";
		String imagebuffer = "";

		while(infile.hasNextLine()){
			if(counter==0){	//write headers
				outfile.write("idnum");
				imageout.write("idnum");
				for(int i=1; i<=itemsPerLine; i++){
					outfile.write("\tid" + i + "\tsent" + i);
					imageout.write("\tsent" + i);
				}
				outfile.write("\n");
				imageout.write("\n");
			}
			else{
				if(begofline){ //write id num
					buffer += linenum + "\t";
					imagebuffer += linenum + "\t";
					linenum++;
					begofline = false;
				}
				boolean skip = false;				
				if(item==11){ //insert control sentence
					if(inmsa.hasNextLine()){
						String controlline = inmsa.nextLine();
						StringTokenizer tok = new StringTokenizer(controlline);
						String ann = tok.nextToken(); 
						String control = tok.nextToken(); 
						while(tok.hasMoreTokens())
							control += " " + tok.nextToken();
							//format:  lineid msanum ann / lineid dianum ann
						controlout.write((linenum-1) + "\t" + item + "\t" + ann +"\n");
						buffer += "0\t" + control;
						imagebuffer += generateImage(control, linenum-1, item);
					}
				}
				else if(item==12){
					if(india.hasNextLine()){
						String controlline = india.nextLine();
						StringTokenizer tok = new StringTokenizer(controlline);
						String ann = tok.nextToken(); 
						String control = tok.nextToken(); 
						while(tok.hasMoreTokens())
							control += " " + tok.nextToken();
							//format:  lineid msanum ann / lineid dianum ann
						controlout.write((linenum-1) + "\t" + item + "\t" + ann +"\n");
						buffer += "0\t" + control;
						imagebuffer += generateImage(control, linenum-1, item);
					}
				}
				else{ //write actual sentence
					String line = infile.nextLine();
					StringTokenizer linetok = new StringTokenizer(line, "\t");
					try{

					String tweetid = linetok.nextToken();
					String text = linetok.nextToken();
					if(!line.equals("￼")){	//skip over [obj]
						buffer += line;
						imagebuffer += generateImage(text, linenum-1, item);
					}
					else{
						skip=true;
						counter--;
					}

					}//end try
					catch(NoSuchElementException nsee){
						System.out.println("NoSuchElementException at line " + counter + ": " + line);
						System.out.println(nsee.getMessage());
					}
					
				}//end else
				if(!skip){
					if((counter%itemsPerLine)==0){ //line is done, prepare for new line
						outfile.write(buffer + "\n");
						imageout.write(imagebuffer + "\n");
						buffer = "";
						imagebuffer = "";
						begofline = true;
						item = 1;
					}
					else{
						buffer += "\t";
						imagebuffer += "\t";
						item++;
					}
				}//end if !skip
			}//end else
			counter++;

		}//end while
	
		infile.close();
		inmsa.close();
		india.close();
		outfile.close();
		controlout.close();
		imageout.close();

	}//end try

	catch(IOException e){
		System.out.println("IOException: " + e.getMessage());
	}

}//end main

public static String generateImage(String text, int groupnum, int itemnum){
       	String fileName = groupnum + "-" + itemnum;

	//if(groupnum < 231){
	//	System.out.println("Cached, skipping...");
	//	return fileName + ".png";
	//}

        File newFile = new File("./" + fileName + ".png");

	//System.out.println("Creating image " + fileName);

	Font font = new Font("Arabic Transparent", Font.PLAIN, 18);
        //Font font = new Font("Tahoma", Font.PLAIN, 14);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        Rectangle2D bounds = font.getStringBounds(text, frc);
        int w = Math.max((int) bounds.getWidth(), 1);
        int h = Math.max((int) bounds.getHeight(), 1);

	//check for long strings
	boolean multiline = false;
	List<String> lines = null;
	int numlines = 1;
	int splitlength = 40;
	int maxlength = splitlength;
	if(text.length() > splitlength){ //if no spaces to separate by, treat as one line
		multiline = true;

		System.out.println("Creating multiline image " + fileName);

		numlines = (text.length()/splitlength) + 1;
		String[] parts = new String[numlines];
		int index = 0;
		int k=0;
		boolean done = false;
		while((k<numlines) && !done){
			String seg;
			if((text.length()-index) < splitlength){	//last segment
				seg = text.substring(index);
				done = true;
			}
			else{
				int endindex = text.indexOf(' ', index+splitlength);	//find the next space
				if(endindex == -1) //if no more spaces split after maximum length
					endindex = index + maxlength;
				if(endindex > text.length()-1){
					seg = text.substring(index);
					done = true;
				}
				else
					seg = text.substring(index, endindex);
				//System.out.println("-- adding segment: " + seg);
				if(seg.length() > maxlength)
					maxlength = seg.length();
				index = endindex;
				k++;
			}
			parts[k] = seg;
		}//end while
		numlines = parts.length;	//adjust numlines
    		lines =  Arrays.asList(parts);
		w = Math.max((w/numlines), 1);
		h = h*numlines;

		boolean haseng = false;
		for(int cnum=0; cnum<text.length(); cnum++){
			if(Character.isLetter(text.charAt(cnum))){
				haseng = true;
				break;
			}
		}
		if(haseng)
			w = w + (maxlength*3);
		else
			w = w + (maxlength*2);
		//System.out.println("-- w: " + w + ", h: " + h);
	}

	h = h + 5;
	//w = w + 85;
       
        //create a BufferedImage object
      	BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
       
        //set color and other parameters
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        g.setFont(font);

	float x = (float) bounds.getX();
	float y = (float) -bounds.getY();

	if(multiline){
		for(String line : lines){
			if(line != null){
				g.drawString(line, x, y);
				y += 20;
			}
		}
	}
        else  
       		g.drawString(text, x, y);

     	g.dispose();
      
        //creating the file
	try{
       		ImageIO.write(image, "PNG", newFile);
	}
	catch(IOException e){
		System.out.println("Error writing image: " + e.getMessage());
	}
	catch(Exception ex){
		System.out.println(ex.getMessage());
		System.out.println("STATS: id: " + fileName + ", w: " + w + ", h: " + h + ", numlines: " + numlines);
	}

	return fileName + ".png";
}//end generateImage

}


















