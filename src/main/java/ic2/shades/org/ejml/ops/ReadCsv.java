package ic2.shades.org.ejml.ops;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReadCsv {
  private boolean hasComment = false;
  
  private char comment;
  
  private BufferedReader in;
  
  private int lineNumber = 0;
  
  public ReadCsv(InputStream in) {
    this.in = new BufferedReader(new InputStreamReader(in));
  }
  
  public void setComment(char comment) {
    this.hasComment = true;
    this.comment = comment;
  }
  
  public int getLineNumber() {
    return this.lineNumber;
  }
  
  public BufferedReader getReader() {
    return this.in;
  }
  
  protected List<String> extractWords() throws IOException {
    String line;
    while (true) {
      this.lineNumber++;
      line = this.in.readLine();
      if (line == null)
        return null; 
      if (this.hasComment && 
        line.charAt(0) == this.comment)
        continue; 
      break;
    } 
    return parseWords(line);
  }
  
  protected List<String> parseWords(String line) {
    List<String> words = new ArrayList<String>();
    boolean insideWord = !isSpace(line.charAt(0));
    int last = 0;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (insideWord) {
        if (isSpace(c)) {
          words.add(line.substring(last, i));
          insideWord = false;
        } 
      } else if (!isSpace(c)) {
        last = i;
        insideWord = true;
      } 
    } 
    if (insideWord)
      words.add(line.substring(last)); 
    return words;
  }
  
  private boolean isSpace(char c) {
    return (c == ' ' || c == '\t');
  }
}
