// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.ops;

import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;

public class ReadCsv
{
    private boolean hasComment;
    private char comment;
    private BufferedReader in;
    private int lineNumber;
    
    public ReadCsv(final InputStream in) {
        this.hasComment = false;
        this.lineNumber = 0;
        this.in = new BufferedReader(new InputStreamReader(in));
    }
    
    public void setComment(final char comment) {
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
        while (true) {
            ++this.lineNumber;
            final String line = this.in.readLine();
            if (line == null) {
                return null;
            }
            if (this.hasComment && line.charAt(0) == this.comment) {
                continue;
            }
            return this.parseWords(line);
        }
    }
    
    protected List<String> parseWords(final String line) {
        final List<String> words = new ArrayList<String>();
        boolean insideWord = !this.isSpace(line.charAt(0));
        int last = 0;
        for (int i = 0; i < line.length(); ++i) {
            final char c = line.charAt(i);
            if (insideWord) {
                if (this.isSpace(c)) {
                    words.add(line.substring(last, i));
                    insideWord = false;
                }
            }
            else if (!this.isSpace(c)) {
                last = i;
                insideWord = true;
            }
        }
        if (insideWord) {
            words.add(line.substring(last));
        }
        return words;
    }
    
    private boolean isSpace(final char c) {
        return c == ' ' || c == '\t';
    }
}
