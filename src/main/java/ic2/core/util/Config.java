// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import java.text.NumberFormat;
import java.util.Locale;
import java.text.ParseException;
import java.io.FileOutputStream;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Date;
import java.text.DateFormat;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.ListIterator;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.Reader;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import com.google.common.base.Charsets;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class Config
{
    private final Config parent;
    public final String name;
    private String comment;
    private boolean saveWithParent;
    private final Map<String, Config> sections;
    private final Map<String, Value> values;
    private static final String lineSeparator;
    
    public Config(final String name) {
        this(null, name, "");
    }
    
    private Config(final Config parent, final String name, final String comment) {
        this.saveWithParent = true;
        this.sections = new LinkedHashMap<String, Config>();
        this.values = new LinkedHashMap<String, Value>();
        assert parent != this;
        this.parent = parent;
        this.name = name;
        this.comment = comment;
    }
    
    public Config getRoot() {
        Config ret;
        for (ret = this; ret.parent != null; ret = ret.parent) {}
        return ret;
    }
    
    public Config getSub(final String key) {
        final List<String> parts = split(key, '/');
        return this.getSub(parts, parts.size(), false);
    }
    
    public Config addSub(final String key, final String aComment) {
        assert split(key, '/').size() == 1;
        Config config = this.sections.get(key);
        if (config == null) {
            config = new Config(this, key, aComment);
            this.sections.put(key, config);
        }
        else {
            config.comment = aComment;
        }
        return config;
    }
    
    public Value get(final String key) {
        final List<String> parts = split(key, '/');
        final Config config = this.getSub(parts, parts.size() - 1, false);
        if (config == null) {
            return null;
        }
        return config.values.get(parts.get(parts.size() - 1));
    }
    
    public void set(final String key, final Value value) {
        final List<String> parts = split(key, '/');
        assert parts.get(parts.size() - 1).equals(value.name);
        final Config config = this.getSub(parts, parts.size() - 1, true);
        config.values.put(parts.get(parts.size() - 1), value);
    }
    
    public <T> void set(final String key, final T value) {
        final List<String> parts = split(key, '/');
        final Config config = this.getSub(parts, parts.size() - 1, true);
        final String tName = parts.get(parts.size() - 1);
        Value existingValue = config.values.get(tName);
        if (existingValue == null) {
            existingValue = new Value(tName, "", null);
            config.values.put(tName, existingValue);
        }
        existingValue.set(value);
    }
    
    public Value remove(final String key) {
        final List<String> parts = split(key, '/');
        final Config config = this.getSub(parts, parts.size() - 1, true);
        final String tName = parts.get(parts.size() - 1);
        return config.values.remove(tName);
    }
    
    public void clear() {
        this.sections.clear();
        this.values.clear();
    }
    
    public void sort() {
        final List<Map.Entry<String, Value>> valueList = new ArrayList<Map.Entry<String, Value>>(this.values.entrySet());
        Collections.sort(valueList, new Comparator<Map.Entry<String, Value>>() {
            @Override
            public int compare(final Map.Entry<String, Value> a, final Map.Entry<String, Value> b) {
                return a.getKey().compareTo((String)b.getKey());
            }
        });
        this.values.clear();
        for (final Map.Entry<String, Value> entry : valueList) {
            this.values.put(entry.getKey(), entry.getValue());
        }
    }
    
    public Iterator<Config> sectionIterator() {
        return this.sections.values().iterator();
    }
    
    public boolean hasChildSection() {
        return !this.sections.isEmpty();
    }
    
    public int getNumberOfSections() {
        return this.sections.size();
    }
    
    public Iterator<Value> valueIterator() {
        return this.values.values().iterator();
    }
    
    public boolean isEmptySection() {
        return this.values.isEmpty();
    }
    
    public int getNumberOfConfigs() {
        return this.values.size();
    }
    
    public void setSaveWithParent(final boolean saveWithParent) {
        this.saveWithParent = saveWithParent;
    }
    
    public void load(final InputStream is) throws IOException, ParseException {
        final InputStreamReader isReader = new InputStreamReader(is, Charsets.UTF_8);
        final LineNumberReader reader = new LineNumberReader(isReader);
        Config config;
        final Config root = config = this;
        StringBuilder tComment = new StringBuilder();
        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                line = trim(line);
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith(";")) {
                    if (line.equals(";---")) {
                        tComment = new StringBuilder();
                    }
                    else {
                        line = line.substring(1).trim();
                        if (tComment.length() != 0) {
                            tComment.append(Config.lineSeparator);
                        }
                        tComment.append(line);
                    }
                }
                else if (line.startsWith("[")) {
                    if (!line.endsWith("]")) {
                        throw new ParseException("section without closing bracket", reader.getLineNumber(), line);
                    }
                    final String section = line.substring(1, line.length() - 1);
                    final List<String> keys = split(section, '/');
                    final ListIterator<String> it = keys.listIterator();
                    while (it.hasNext()) {
                        it.set(unescapeSection(it.next()));
                    }
                    if (tComment.length() > 0) {
                        config = root.getSub(keys, keys.size() - 1, true);
                        config = config.addSub(keys.get(keys.size() - 1), tComment.toString());
                        tComment = new StringBuilder();
                    }
                    else {
                        config = root.getSub(keys, keys.size(), true);
                    }
                }
                else {
                    final List<String> parts = split(line, '=');
                    if (parts.size() != 2) {
                        throw new ParseException("invalid key-value pair", reader.getLineNumber(), line);
                    }
                    final String key = unescapeValue(parts.get(0).trim());
                    if (key.isEmpty()) {
                        throw new ParseException("empty key", reader.getLineNumber(), line);
                    }
                    String valueStr;
                    for (valueStr = parts.get(1).trim(); valueStr.replaceAll("\\\\.", "xx").endsWith("\\"); valueStr = valueStr.substring(0, valueStr.length() - 1) + " ", valueStr += reader.readLine().trim()) {}
                    valueStr = unescapeValue(valueStr);
                    config.set(key, new Value(key, tComment.toString(), valueStr, reader.getLineNumber()));
                    if (tComment.length() <= 0) {
                        continue;
                    }
                    tComment = new StringBuilder();
                }
            }
        }
        catch (final IOException e) {
            throw e;
        }
        catch (final Exception e2) {
            throw new ParseException("general parse error", reader.getLineNumber(), line, e2);
        }
        finally {
            IOUtils.closeQuietly((Reader)reader);
            IOUtils.closeQuietly((Reader)isReader);
        }
    }
    
    public void load(final File file) throws ParseException, IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            this.load(is);
        }
        finally {
            IOUtils.closeQuietly((InputStream)is);
        }
    }
    
    public void save(final OutputStream os) throws IOException {
        final OutputStreamWriter osWriter = new OutputStreamWriter(os, Charsets.UTF_8);
        final BufferedWriter writer = new BufferedWriter(osWriter);
        try {
            writer.write("; ");
            writer.write(this.name);
            writer.newLine();
            writer.write("; created ");
            writer.write(DateFormat.getDateTimeInstance().format(new Date()));
            writer.newLine();
            writer.write(";---");
            writer.newLine();
            final Config root = this;
            final Deque<Config> todo = new ArrayDeque<Config>();
            todo.add(this);
            Config config;
            while ((config = todo.poll()) != null) {
                if (!config.values.isEmpty() || !config.comment.isEmpty() || config.sections.isEmpty()) {
                    writer.newLine();
                    if (config != root) {
                        if (!config.comment.isEmpty()) {
                            final String[] split;
                            final String[] commentParts = split = config.comment.split("\\n");
                            for (final String comment : split) {
                                writer.write("; ");
                                writer.write(comment);
                                writer.newLine();
                            }
                        }
                        writer.write(91);
                        final List<String> keys = new ArrayList<String>();
                        Config cSection = config;
                        do {
                            keys.add(cSection.name);
                            cSection = cSection.parent;
                        } while (cSection != root);
                        for (int i = keys.size() - 1; i >= 0; --i) {
                            writer.write(escapeSection(keys.get(i)));
                            if (i > 0) {
                                writer.write(" / ");
                            }
                        }
                        writer.write(93);
                        writer.newLine();
                    }
                    for (final Value value : config.values.values()) {
                        if (!value.comment.isEmpty()) {
                            for (final String line : value.comment.split("\\n")) {
                                writer.write("; ");
                                writer.write(line);
                                writer.newLine();
                            }
                        }
                        writer.write(escapeValue(value.name));
                        writer.write(" = ");
                        writer.write(escapeValue(value.getString()));
                        writer.newLine();
                    }
                }
                final ArrayList<Config> toAdd = new ArrayList<Config>(config.sections.size());
                for (final Config section : config.sections.values()) {
                    if (section.saveWithParent) {
                        toAdd.add(section);
                    }
                }
                final ListIterator<Config> it = toAdd.listIterator(toAdd.size());
                while (it.hasPrevious()) {
                    todo.addFirst(it.previous());
                }
            }
            writer.newLine();
        }
        finally {
            IOUtils.closeQuietly((Writer)writer);
            IOUtils.closeQuietly((Writer)osWriter);
        }
    }
    
    public void save(final File file) throws IOException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            this.save(os);
        }
        finally {
            IOUtils.closeQuietly((OutputStream)os);
        }
    }
    
    private Config getSub(final List<String> keys, final int end, final boolean create) {
        Config ret = this;
        for (int i = 0; i < end; ++i) {
            final String key = keys.get(i);
            assert key.length() > 0;
            Config config = ret.sections.get(key);
            if (config == null) {
                if (!create) {
                    return null;
                }
                config = new Config(ret, key, "");
                ret.sections.put(key, config);
            }
            ret = config;
        }
        return ret;
    }
    
    private static List<String> split(final String str, final char splitChar) {
        final List<String> ret = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean empty = true;
        boolean passNext = false;
        boolean quoted = false;
        for (int i = 0; i < str.length(); ++i) {
            final char c = str.charAt(i);
            if (passNext) {
                current.append(c);
                empty = false;
                passNext = false;
            }
            else if (c == '\\') {
                current.append(c);
                empty = false;
                passNext = true;
            }
            else if (c == '\"') {
                current.append(c);
                empty = false;
                quoted = !quoted;
            }
            else if (!quoted && c == splitChar) {
                ret.add(current.toString().trim());
                current = new StringBuilder();
                empty = true;
            }
            else if (!Character.isWhitespace(c) || !empty) {
                current.append(c);
                empty = false;
            }
        }
        ret.add(current.toString().trim());
        return ret;
    }
    
    private static String escapeSection(final String str) {
        return str.replaceAll("([\\[\\];/])", "\\\\$1").replace("\n", "\\n");
    }
    
    private static String unescapeSection(final String str) {
        return str.replaceAll("\\\\([\\[\\];/])", "$1").replace("\\n", "\n");
    }
    
    private static String escapeValue(final String str) {
        return str.replaceAll("([\\[\\];=\\\\])", "\\\\$1").replace("\n", "\\\n");
    }
    
    private static String unescapeValue(final String str) {
        return str.replaceAll("\\\\([\\[\\];=])", "$1");
    }
    
    private static String trim(final String str) {
        int len;
        int start;
        for (len = str.length(), start = 0; start < len; ++start) {
            final char c = str.charAt(start);
            if (c > ' ' && c != '\ufeff') {
                break;
            }
        }
        int end;
        for (end = len - 1; end >= start; --end) {
            final char c = str.charAt(end);
            if (c > ' ' && c != '\ufeff') {
                break;
            }
        }
        if (start > 0 || end < len - 1) {
            return str.substring(start, end + 1);
        }
        return str;
    }
    
    static {
        lineSeparator = System.getProperty("line.separator");
    }
    
    public static class Value
    {
        public final String name;
        public String comment;
        public String value;
        private final int line;
        private Number numberCache;
        
        public Value(final String name, final String comment, final String value) {
            this(name, comment, value, -1);
        }
        
        private Value(final String name, final String comment, final String value, final int line) {
            this.name = name;
            this.comment = comment;
            this.value = value;
            this.line = line;
        }
        
        public String getString() {
            return this.value;
        }
        
        public boolean getBool() {
            return Boolean.valueOf(this.value);
        }
        
        public int getInt() {
            try {
                return this.getNumber().intValue();
            }
            catch (final java.text.ParseException e) {
                throw new ParseException("invalid value", this, e);
            }
        }
        
        public float getFloat() {
            try {
                return this.getNumber().floatValue();
            }
            catch (final java.text.ParseException e) {
                throw new ParseException("invalid value", this, e);
            }
        }
        
        public double getDouble() {
            try {
                return this.getNumber().doubleValue();
            }
            catch (final java.text.ParseException e) {
                throw new ParseException("invalid value", this, e);
            }
        }
        
        public <T> void set(final T value) {
            this.value = String.valueOf(value);
            this.numberCache = null;
        }
        
        public int getLine() {
            return this.line;
        }
        
        private Number getNumber() throws java.text.ParseException {
            if (this.numberCache == null) {
                this.numberCache = NumberFormat.getInstance(Locale.US).parse(this.value);
            }
            return this.numberCache;
        }
    }
    
    public static class ParseException extends RuntimeException
    {
        private static final long serialVersionUID = 8721912755972301225L;
        
        public ParseException(final String msg, final int line, final String content) {
            super(formatMsg(msg, line, content));
        }
        
        public ParseException(final String msg, final int line, final String content, final Exception e) {
            super(formatMsg(msg, line, content), e);
        }
        
        public ParseException(final String msg, final Value value) {
            super(formatMsg(msg, value));
        }
        
        public ParseException(final String msg, final Value value, final Exception e) {
            super(formatMsg(msg, value), e);
        }
        
        private static String formatMsg(final String msg, final int line, String content) {
            if (!isPrintable(content)) {
                content = content + "|" + toPrintable(content);
            }
            if (line >= 0) {
                return msg + " at line " + line + " (" + content + ").";
            }
            return msg + " at an unknown line (" + content + ").";
        }
        
        private static String formatMsg(final String msg, final Value value) {
            return formatMsg(msg, value.getLine(), value.name + " = " + value.getString());
        }
        
        private static boolean isPrintable(final String str) {
            for (int len = str.length(), i = 0; i < len; ++i) {
                final char c = str.charAt(i);
                if (c < ' ' || c > '~') {
                    return false;
                }
            }
            return true;
        }
        
        private static String toPrintable(final String str) {
            final int len = str.length();
            String ret = "";
            for (int i = 0; i < len; ++i) {
                final char c = str.charAt(i);
                if (c < ' ' || c > '~') {
                    if (i > 0) {
                        ret += ',';
                    }
                    ret += String.format("0x%x", (int)c);
                    if (i < len - 1) {
                        ret += ',';
                    }
                }
                else {
                    ret += c;
                }
            }
            return ret;
        }
    }
}
