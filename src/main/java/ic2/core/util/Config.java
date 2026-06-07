package ic2.core.util;

import com.google.common.base.Charsets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

public class Config
{
	private final Config parent;
	public final String name;
	private String comment;
	private boolean saveWithParent = true;
	private final Map<String, Config> sections = new LinkedHashMap<>();
	private final Map<String, Config.Value> values = new LinkedHashMap<>();
	private static final String lineSeparator = System.getProperty("line.separator");

	public Config(String name)
	{
		this(null, name, "");
	}

	private Config(Config parent, String name, String comment)
	{
		assert parent != this;
		this.parent = parent;
		this.name = name;
		this.comment = comment;
	}

	public Config getRoot()
	{
		Config ret = this;

		while (ret.parent != null)
		{
			ret = ret.parent;
		}

		return ret;
	}

	public Config getSub(String key)
	{
		List<String> parts = split(key, '/');
		return this.getSub(parts, parts.size(), false);
	}

	public Config addSub(String key, String aComment)
	{
		assert split(key, '/').size() == 1;
		Config config = this.sections.get(key);
		if (config == null)
		{
			config = new Config(this, key, aComment);
			this.sections.put(key, config);
		} else
		{
			config.comment = aComment;
		}

		return config;
	}

	public Config.Value get(String key)
	{
		List<String> parts = split(key, '/');
		Config config = this.getSub(parts, parts.size() - 1, false);
		return config == null ? null : config.values.get(parts.get(parts.size() - 1));
	}

	public void set(String key, Config.Value value)
	{
		List<String> parts = split(key, '/');
		assert parts.get(parts.size() - 1).equals(value.name);
		Config config = this.getSub(parts, parts.size() - 1, true);
		config.values.put(parts.get(parts.size() - 1), value);
	}

	public <T> void set(String key, T value)
	{
		List<String> parts = split(key, '/');
		Config config = this.getSub(parts, parts.size() - 1, true);
		String tName = parts.get(parts.size() - 1);
		Config.Value existingValue = config.values.get(tName);
		if (existingValue == null)
		{
			existingValue = new Config.Value(tName, "", null);
			config.values.put(tName, existingValue);
		}

		existingValue.set(value);
	}

	public Config.Value remove(String key)
	{
		List<String> parts = split(key, '/');
		Config config = this.getSub(parts, parts.size() - 1, true);
		String tName = parts.get(parts.size() - 1);
		return config.values.remove(tName);
	}

	public void clear()
	{
		this.sections.clear();
		this.values.clear();
	}

	public void sort()
	{
		List<Entry<String, Config.Value>> valueList = new ArrayList<>(this.values.entrySet());
		Collections.sort(valueList, new Comparator<Entry<String, Config.Value>>()
		{
			public int compare(Entry<String, Config.Value> a, Entry<String, Config.Value> b)
			{
				return a.getKey().compareTo(b.getKey());
			}
		});
		this.values.clear();

		for (Entry<String, Config.Value> entry : valueList)
		{
			this.values.put(entry.getKey(), entry.getValue());
		}
	}

	public Iterator<Config> sectionIterator()
	{
		return this.sections.values().iterator();
	}

	public boolean hasChildSection()
	{
		return !this.sections.isEmpty();
	}

	public int getNumberOfSections()
	{
		return this.sections.size();
	}

	public Iterator<Config.Value> valueIterator()
	{
		return this.values.values().iterator();
	}

	public boolean isEmptySection()
	{
		return this.values.isEmpty();
	}

	public int getNumberOfConfigs()
	{
		return this.values.size();
	}

	public void setSaveWithParent(boolean saveWithParent)
	{
		this.saveWithParent = saveWithParent;
	}

	public void load(InputStream is) throws IOException, Config.ParseException
	{
		InputStreamReader isReader = new InputStreamReader(is, Charsets.UTF_8);
		LineNumberReader reader = new LineNumberReader(isReader);
		Config root = this;
		Config config = root;
		StringBuilder tComment = new StringBuilder();
		String line = "";

		try
		{
			while ((line = reader.readLine()) != null)
			{
				line = trim(line);
				if (!line.isEmpty())
				{
					if (line.startsWith(";"))
					{
						if (line.equals(";---"))
						{
							tComment = new StringBuilder();
						} else
						{
							line = line.substring(1).trim();
							if (tComment.length() != 0)
							{
								tComment.append(lineSeparator);
							}

							tComment.append(line);
						}
					} else if (line.startsWith("["))
					{
						if (!line.endsWith("]"))
						{
							throw new Config.ParseException("section without closing bracket", reader.getLineNumber(), line);
						}

						String section = line.substring(1, line.length() - 1);
						List<String> keys = split(section, '/');
						ListIterator<String> it = keys.listIterator();

						while (it.hasNext())
						{
							it.set(unescapeSection(it.next()));
						}

						if (tComment.length() > 0)
						{
							config = root.getSub(keys, keys.size() - 1, true);
							config = config.addSub(keys.get(keys.size() - 1), tComment.toString());
							tComment = new StringBuilder();
						} else
						{
							config = root.getSub(keys, keys.size(), true);
						}
					} else
					{
						List<String> parts = split(line, '=');
						if (parts.size() != 2)
						{
							throw new Config.ParseException("invalid key-value pair", reader.getLineNumber(), line);
						}

						String key = unescapeValue(parts.get(0).trim());
						if (key.isEmpty())
						{
							throw new Config.ParseException("empty key", reader.getLineNumber(), line);
						}

						String valueStr = parts.get(1).trim();

						while (valueStr.replaceAll("\\\\.", "xx").endsWith("\\"))
						{
							valueStr = valueStr.substring(0, valueStr.length() - 1) + " ";
							valueStr = valueStr + reader.readLine().trim();
						}

						valueStr = unescapeValue(valueStr);
						config.set(key, new Config.Value(key, tComment.toString(), valueStr, reader.getLineNumber()));
						if (tComment.length() > 0)
						{
							tComment = new StringBuilder();
						}
					}
				}
			}
		} catch (IOException e)
		{
			throw e;
		} catch (Exception e)
		{
			throw new Config.ParseException("general parse error", reader.getLineNumber(), line, e);
		} finally
		{
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(isReader);
		}
	}

	public void load(File file) throws Config.ParseException, IOException
	{
		FileInputStream is = null;

		try
		{
			is = new FileInputStream(file);
			this.load(is);
		} finally
		{
			IOUtils.closeQuietly(is);
		}
	}

	public void save(OutputStream os) throws IOException
	{
		OutputStreamWriter osWriter = new OutputStreamWriter(os, Charsets.UTF_8);
		BufferedWriter writer = new BufferedWriter(osWriter);

		try
		{
			writer.write("; ");
			writer.write(this.name);
			writer.newLine();
			writer.write("; created ");
			writer.write(DateFormat.getDateTimeInstance().format(new Date()));
			writer.newLine();
			writer.write(";---");
			writer.newLine();
			Config root = this;
			Deque<Config> todo = new ArrayDeque<>();
			todo.add(this);

			Config config;
			while ((config = todo.poll()) != null)
			{
				if (!config.values.isEmpty() || !config.comment.isEmpty() || config.sections.isEmpty())
				{
					writer.newLine();
					if (config != root)
					{
						if (!config.comment.isEmpty())
						{
							String[] commentParts = config.comment.split("\\n");

							for (String comment : commentParts)
							{
								writer.write("; ");
								writer.write(comment);
								writer.newLine();
							}
						}

						writer.write(91);
						List<String> keys = new ArrayList<>();
						Config cSection = config;

						do
						{
							keys.add(cSection.name);
							cSection = cSection.parent;
						} while (cSection != root);

						for (int i = keys.size() - 1; i >= 0; i--)
						{
							writer.write(escapeSection(keys.get(i)));
							if (i > 0)
							{
								writer.write(" / ");
							}
						}

						writer.write(93);
						writer.newLine();
					}

					for (Config.Value value : config.values.values())
					{
						if (!value.comment.isEmpty())
						{
							for (String line : value.comment.split("\\n"))
							{
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

				ArrayList<Config> toAdd = new ArrayList<>(config.sections.size());

				for (Config section : config.sections.values())
				{
					if (section.saveWithParent)
					{
						toAdd.add(section);
					}
				}

				ListIterator<Config> it = toAdd.listIterator(toAdd.size());

				while (it.hasPrevious())
				{
					todo.addFirst(it.previous());
				}
			}

			writer.newLine();
		} finally
		{
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(osWriter);
		}
	}

	public void save(File file) throws IOException
	{
		FileOutputStream os = null;

		try
		{
			os = new FileOutputStream(file);
			this.save(os);
		} finally
		{
			IOUtils.closeQuietly(os);
		}
	}

	private Config getSub(List<String> keys, int end, boolean create)
	{
		Config ret = this;

		for (int i = 0; i < end; i++)
		{
			String key = keys.get(i);
			assert key.length() > 0;
			Config config = ret.sections.get(key);
			if (config == null)
			{
				if (!create)
				{
					return null;
				}

				config = new Config(ret, key, "");
				ret.sections.put(key, config);
			}

			ret = config;
		}

		return ret;
	}

	private static List<String> split(String str, char splitChar)
	{
		List<String> ret = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean empty = true;
		boolean passNext = false;
		boolean quoted = false;

		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			if (passNext)
			{
				current.append(c);
				empty = false;
				passNext = false;
			} else if (c == '\\')
			{
				current.append(c);
				empty = false;
				passNext = true;
			} else if (c == '"')
			{
				current.append(c);
				empty = false;
				quoted = !quoted;
			} else if (!quoted && c == splitChar)
			{
				ret.add(current.toString().trim());
				current = new StringBuilder();
				empty = true;
			} else if (!Character.isWhitespace(c) || !empty)
			{
				current.append(c);
				empty = false;
			}
		}

		ret.add(current.toString().trim());
		return ret;
	}

	private static String escapeSection(String str)
	{
		return str.replaceAll("([\\[\\];/])", "\\\\$1").replace("\n", "\\n");
	}

	private static String unescapeSection(String str)
	{
		return str.replaceAll("\\\\([\\[\\];/])", "$1").replace("\\n", "\n");
	}

	private static String escapeValue(String str)
	{
		return str.replaceAll("([\\[\\];=\\\\])", "\\\\$1").replace("\n", "\\\n");
	}

	private static String unescapeValue(String str)
	{
		return str.replaceAll("\\\\([\\[\\];=])", "$1");
	}

	private static String trim(String str)
	{
		int len = str.length();

		int start;
		for (start = 0; start < len; start++)
		{
			char c = str.charAt(start);
			if (c > ' ' && c != '\ufeff')
			{
				break;
			}
		}

		int end;
		for (end = len - 1; end >= start; end--)
		{
			char c = str.charAt(end);
			if (c > ' ' && c != '\ufeff')
			{
				break;
			}
		}

		return start <= 0 && end >= len - 1 ? str : str.substring(start, end + 1);
	}

	public static class ParseException extends RuntimeException
	{
		private static final long serialVersionUID = 8721912755972301225L;

		public ParseException(String msg, int line, String content)
		{
			super(formatMsg(msg, line, content));
		}

		public ParseException(String msg, int line, String content, Exception e)
		{
			super(formatMsg(msg, line, content), e);
		}

		public ParseException(String msg, Config.Value value)
		{
			super(formatMsg(msg, value));
		}

		public ParseException(String msg, Config.Value value, Exception e)
		{
			super(formatMsg(msg, value), e);
		}

		private static String formatMsg(String msg, int line, String content)
		{
			if (!isPrintable(content))
			{
				content = content + "|" + toPrintable(content);
			}

			return line >= 0 ? msg + " at line " + line + " (" + content + ")." : msg + " at an unknown line (" + content + ").";
		}

		private static String formatMsg(String msg, Config.Value value)
		{
			return formatMsg(msg, value.getLine(), value.name + " = " + value.getString());
		}

		private static boolean isPrintable(String str)
		{
			int len = str.length();

			for (int i = 0; i < len; i++)
			{
				char c = str.charAt(i);
				if (c < ' ' || c > '~')
				{
					return false;
				}
			}

			return true;
		}

		private static String toPrintable(String str)
		{
			int len = str.length();
			String ret = "";

			for (int i = 0; i < len; i++)
			{
				char c = str.charAt(i);
				if (c >= ' ' && c <= '~')
				{
					ret = ret + c;
				} else
				{
					if (i > 0)
					{
						ret = ret + ",";
					}

					ret = ret + String.format("0x%x", Integer.valueOf(c));
					if (i < len - 1)
					{
						ret = ret + ",";
					}
				}
			}

			return ret;
		}
	}

	public static class Value
	{
		public final String name;
		public String comment;
		public String value;
		private final int line;
		private Number numberCache;

		public Value(String name, String comment, String value)
		{
			this(name, comment, value, -1);
		}

		private Value(String name, String comment, String value, int line)
		{
			this.name = name;
			this.comment = comment;
			this.value = value;
			this.line = line;
		}

		public String getString()
		{
			return this.value;
		}

		public boolean getBool()
		{
			return Boolean.valueOf(this.value);
		}

		public int getInt()
		{
			try
			{
				return this.getNumber().intValue();
			} catch (java.text.ParseException e)
			{
				throw new Config.ParseException("invalid value", this, e);
			}
		}

		public float getFloat()
		{
			try
			{
				return this.getNumber().floatValue();
			} catch (java.text.ParseException e)
			{
				throw new Config.ParseException("invalid value", this, e);
			}
		}

		public double getDouble()
		{
			try
			{
				return this.getNumber().doubleValue();
			} catch (java.text.ParseException e)
			{
				throw new Config.ParseException("invalid value", this, e);
			}
		}

		public <T> void set(T value)
		{
			this.value = String.valueOf(value);
			this.numberCache = null;
		}

		public int getLine()
		{
			return this.line;
		}

		private Number getNumber() throws java.text.ParseException
		{
			if (this.numberCache == null)
			{
				this.numberCache = NumberFormat.getInstance(Locale.US).parse(this.value);
			}

			return this.numberCache;
		}
	}
}
