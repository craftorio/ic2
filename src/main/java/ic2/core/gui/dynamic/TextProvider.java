package ic2.core.gui.dynamic;

import com.google.common.base.Supplier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.minecraft.network.chat.Component;

public class TextProvider
{
	public static TextProvider.ITextProvider of(String text)
	{
		return text.isEmpty() ? new TextProvider.ConstantEmpty() : new TextProvider.Constant(text);
	}

	public static TextProvider.ITextProvider of(Supplier<String> supplier)
	{
		return new TextProvider.AbstractTextProvider()
		{
			@Override
			public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens)
			{
				return supplier.get();
			}

			@Override
			public String getConstant(Class<?> baseClass)
			{
				return supplier.get();
			}
		};
	}

	public static TextProvider.ITextProvider of(Component text)
	{
		return new TextProvider.Translate(new TextProvider.Constant(text.getString()));
	}

	public static TextProvider.ITextProvider ofTranslated(String key)
	{
		return new TextProvider.Translate(new TextProvider.Constant(key));
	}

	public static TextProvider.ITextProvider parse(String text, Class<?> baseClass)
	{
		Queue<List<TextProvider.AbstractTextProvider>> continuations = Collections.asLifoQueue(new ArrayDeque<>());
		StringBuilder continuationTypes = new StringBuilder();
		char currentType = 0;
		List<TextProvider.AbstractTextProvider> providers = new ArrayList<>();
		StringBuilder part = new StringBuilder(text.length());
		boolean escaped = false;

		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (escaped)
			{
				part.append(c);
				escaped = false;
			} else if (c == '\\')
			{
				escaped = true;
			} else if (c == '{')
			{
				finish(part, providers);
				continuations.add(providers);
				continuationTypes.append(currentType);
				currentType = c;
				providers = new ArrayList<>();
			} else if (currentType == '{' && c == ',')
			{
				finish(part, providers);
				providers.add(null);
			} else if (currentType == '{' && c == '}')
			{
				finish(part, providers);
				TextProvider.AbstractTextProvider format = null;
				List<TextProvider.AbstractTextProvider> args = new ArrayList<>();
				int start = 0;

				for (int j = start; j < providers.size(); j++)
				{
					if (providers.get(j) == null)
					{
						TextProvider.AbstractTextProvider provider = getProvider(providers, start, j);
						if (format == null)
						{
							format = provider;
						} else
						{
							args.add(provider);
						}

						start = j + 1;
					}
				}

				TextProvider.AbstractTextProvider provider = getProvider(providers, start, providers.size());
				if (format == null)
				{
					format = provider;
				} else
				{
					args.add(provider);
				}

				if (args.isEmpty())
				{
					provider = new TextProvider.Translate(format);
				} else
				{
					provider = new TextProvider.TranslateFormat(format, args);
				}

				providers = continuations.remove();
				currentType = continuationTypes.charAt(continuationTypes.length() - 1);
				continuationTypes.setLength(continuationTypes.length() - 1);
				providers.add(provider);
			} else if (c == '%')
			{
				if (currentType != '%')
				{
					if (i + 1 < text.length() && text.charAt(i + 1) == '%')
					{
						part.append('%');
						i++;
					} else
					{
						finish(part, providers);
						continuations.add(providers);
						continuationTypes.append(currentType);
						currentType = c;
						providers = new ArrayList<>();
					}
				} else
				{
					finish(part, providers);
					TextProvider.AbstractTextProvider provider = getResolver(getProvider(providers, 0, providers.size()), baseClass);
					providers = continuations.remove();
					currentType = continuationTypes.charAt(continuationTypes.length() - 1);
					continuationTypes.setLength(continuationTypes.length() - 1);
					providers.add(provider);
				}
			} else
			{
				part.append(c);
			}
		}

		finish(part, providers);
		if (currentType != 0)
		{
			return new TextProvider.Constant("ERROR: unfinished token " + currentType + " in " + text);
		} else
		{
			return escaped ? new TextProvider.Constant("ERROR: unfinished escape sequence in " + text) : getProvider(providers, 0, providers.size());
		}
	}

	private static void finish(StringBuilder part, List<TextProvider.AbstractTextProvider> providers)
	{
		if (!part.isEmpty())
		{
			providers.add(new TextProvider.Constant(part.toString()));
			part.setLength(0);
		}
	}

	private static TextProvider.AbstractTextProvider getProvider(List<TextProvider.AbstractTextProvider> providers, int start, int end)
	{
		assert start <= end;
		if (start == end)
		{
			return new TextProvider.ConstantEmpty();
		} else
		{
			return start + 1 == end ? providers.get(start) : new TextProvider.Merge(new ArrayList<>(providers.subList(start, end)));
		}
	}

	private static TextProvider.AbstractTextProvider getResolver(TextProvider.AbstractTextProvider token, Class<?> baseClass)
	{
		String staticToken = token.getConstant(baseClass);
		if (staticToken == null)
		{
			return new TextProvider.TokenResolverDynamic(token);
		}

		String staticResult = resolveToken(staticToken, baseClass, null, emptyTokens());
		return staticResult != null ? new TextProvider.Constant(staticResult) : new TextProvider.TokenResolverStatic(staticToken);
	}

	private static String resolveToken(String token, Class<?> baseClass, Object base, Map<String, TextProvider.ITextProvider> tokens)
	{
		TextProvider.ITextProvider ret = tokens.get(token);
		if (ret != null)
		{
			return ret instanceof TextProvider.AbstractTextProvider ? ((TextProvider.AbstractTextProvider) ret).getRaw(base, tokens) : ret.get(base, tokens);
		} else if (baseClass == null)
		{
			return null;
		} else if (token.startsWith("base."))
		{
			Object value = retrieve(token, "base.".length(), baseClass, base);
			return toString(value);
		} else
		{
			return null;
		}
	}

	private static Object retrieve(String path, int start, Class<?> subjectClass, Object subject)
	{
		int end;
		do
		{
			end = path.indexOf(46, start);
			if (end == -1)
			{
				end = path.length();
			}

			String part = path.substring(start, end);
			if (part.endsWith("()"))
			{
				part = part.substring(0, part.length() - "()".length());
				Method method = getMethodOptional(subjectClass, part);
				if (method == null)
				{
					return null;
				}

				subject = invokeMethodOptional(method, subject);

			} else
			{
				Field field = getFieldOptional(subjectClass, part);
				if (field == null)
				{
					return null;
				}

				subject = getFieldValueOptional(field, subject);

			}
			if (subject == null)
			{
				return null;
			}
			subjectClass = subject.getClass();

			start = end + 1;
		} while (end != path.length());

		return subject;
	}

	private static Method getMethodOptional(Class<?> cls, String name)
	{
		try
		{
			return cls.getMethod(name);
		} catch (NoSuchMethodException e)
		{
			return null;
		} catch (SecurityException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static Object invokeMethodOptional(Method method, Object obj)
	{
		if (obj == null && !Modifier.isStatic(method.getModifiers()))
		{
			return null;
		}

		Object ret;
		try
		{
			ret = method.invoke(obj);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return ret;
	}

	private static Field getFieldOptional(Class<?> cls, String name)
	{
		try
		{
			return cls.getField(name);
		} catch (NoSuchFieldException e)
		{
			return null;
		} catch (SecurityException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static Object getFieldValueOptional(Field field, Object obj)
	{
		if (obj == null && !Modifier.isStatic(field.getModifiers()))
		{
			return null;
		}

		Object ret;
		try
		{
			ret = field.get(obj);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return ret;
	}

	private static String toString(Object o)
	{
		return o == null ? null : o.toString();
	}

	public static Map<String, TextProvider.ITextProvider> emptyTokens()
	{
		return Collections.emptyMap();
	}

	public interface ITextProvider
	{
		String get(Object var1, Map<String, TextProvider.ITextProvider> var2);

		String getOptional(Object var1, Map<String, TextProvider.ITextProvider> var2);
	}

	private abstract static class AbstractTextProvider implements TextProvider.ITextProvider
	{
		@Override
		public final String get(Object base, Map<String, TextProvider.ITextProvider> tokens)
		{
			String result = this.getRaw(base, tokens);
			return result != null ? result : "ERROR";
		}

		@Override
		public final String getOptional(Object base, Map<String, TextProvider.ITextProvider> tokens)
		{
			return this.getRaw(base, tokens);
		}

		protected abstract String getRaw(Object var1, Map<String, TextProvider.ITextProvider> var2);

		protected abstract String getConstant(Class<?> var1);
	}

	private static class Constant extends TextProvider.AbstractTextProvider
	{
		private final String text;

		public Constant(String text)
		{
			this.text = text;
		}

		@Override
		public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens)
		{
			return this.text;
		}

		@Override
		public String getConstant(Class<?> baseClass)
		{
			return this.text;
		}
	}

	private static class ConstantEmpty extends TextProvider.AbstractTextProvider
	{
		@Override
		public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens)
		{
			return "";
		}

		@Override
		public String getConstant(Class<?> baseClass)
		{
			return "";
		}
	}

	private static class Merge extends TextProvider.AbstractTextProvider
	{
		private final List<TextProvider.AbstractTextProvider> providers;

		public Merge(List<TextProvider.AbstractTextProvider> providers)
		{
			this.providers = providers;
		}

		@Override
		public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens)
		{
			StringBuilder ret = new StringBuilder();

			for (TextProvider.AbstractTextProvider provider : this.providers)
			{
				String part = provider.getRaw(base, tokens);
				if (part == null)
				{
					return null;
				}

				ret.append(part);
			}

			return ret.toString();
		}

		@Override
		public String getConstant(Class<?> baseClass)
		{
			StringBuilder ret = new StringBuilder();

			for (TextProvider.AbstractTextProvider provider : this.providers)
			{
				String part = provider.getConstant(baseClass);
				if (part == null)
				{
					return null;
				}

				ret.append(part);
			}

			return ret.toString();
		}
	}

	private static class TokenResolverDynamic extends TextProvider.AbstractTextProvider
	{
		private final TextProvider.AbstractTextProvider token;

		public TokenResolverDynamic(TextProvider.AbstractTextProvider token)
		{
			this.token = token;
		}

		@Override
		public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens)
		{
			String token = this.token.getRaw(base, tokens);
			return token == null ? null : TextProvider.resolveToken(token, base != null ? base.getClass() : null, base, tokens);
		}

		@Override
		public String getConstant(Class<?> baseClass)
		{
			String token = this.token.getConstant(baseClass);
			return token == null ? null : TextProvider.resolveToken(token, baseClass, null, TextProvider.emptyTokens());
		}
	}

	private static class TokenResolverStatic extends TextProvider.AbstractTextProvider
	{
		private final String token;

		public TokenResolverStatic(String token)
		{
			this.token = token;
		}

		@Override
		public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens)
		{
			return TextProvider.resolveToken(this.token, base != null ? base.getClass() : null, base, tokens);
		}

		@Override
		public String getConstant(Class<?> baseClass)
		{
			return TextProvider.resolveToken(this.token, baseClass, null, TextProvider.emptyTokens());
		}
	}

	private static class Translate extends TextProvider.AbstractTextProvider
	{
		private final TextProvider.AbstractTextProvider key;

		public Translate(TextProvider.AbstractTextProvider key)
		{
			this.key = key;
		}

		@Override
		public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens)
		{
			String key = this.key.getRaw(base, tokens);
			return key == null ? null : Component.translatable(key).getString();
		}

		@Override
		public String getConstant(Class<?> baseClass)
		{
			return null;
		}
	}

	private static class TranslateFormat extends TextProvider.AbstractTextProvider
	{
		private final TextProvider.AbstractTextProvider format;
		private final List<TextProvider.AbstractTextProvider> args;

		public TranslateFormat(TextProvider.AbstractTextProvider format, List<TextProvider.AbstractTextProvider> args)
		{
			this.format = format;
			this.args = args;
		}

		@Override
		public String getRaw(Object base, Map<String, TextProvider.ITextProvider> tokens)
		{
			String format = this.format.getRaw(base, tokens);
			if (format == null)
			{
				return null;
			}

			Object[] cArgs = new Object[this.args.size()];

			for (int i = 0; i < this.args.size(); i++)
			{
				String arg = this.args.get(i).getRaw(base, tokens);
				if (arg == null)
				{
					return null;
				}

				cArgs[i] = arg;
			}

			return Component.translatable(format, cArgs).getString();
		}

		@Override
		public String getConstant(Class<?> baseClass)
		{
			return null;
		}
	}
}
