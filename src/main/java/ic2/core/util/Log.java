package ic2.core.util;

import java.io.PrintStream;
import java.util.EnumMap;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
   private static final boolean debug = Util.hasAssertions();
   private final Logger parent;
   private final Map<LogCategory, Logger> loggers = new EnumMap<>(LogCategory.class);

   public Log(Logger parent) {
      this.parent = parent;
   }

   public void error(LogCategory category, String msg, Object... args) {
      this.log(category, Level.FATAL, msg, args);
   }

   public void error(LogCategory category, Throwable t, String msg, Object... args) {
      this.log(category, Level.FATAL, t, msg, args);
   }

   public void error(LogCategory category, String msg) {
      this.log(category, Level.FATAL, msg);
   }

   public void error(LogCategory category, Throwable t, String msg) {
      this.log(category, Level.FATAL, t, msg);
   }

   public void warn(LogCategory category, String msg, Object... args) {
      this.log(category, Level.WARN, msg, args);
   }

   public void warn(LogCategory category, Throwable t, String msg, Object... args) {
      this.log(category, Level.WARN, t, msg, args);
   }

   public void warn(LogCategory category, String msg) {
      this.log(category, Level.WARN, msg);
   }

   public void warn(LogCategory category, Throwable t, String msg) {
      this.log(category, Level.WARN, t, msg);
   }

   public void info(LogCategory category, String msg, Object... args) {
      this.log(category, Level.INFO, msg, args);
   }

   public void info(LogCategory category, Throwable t, String msg, Object... args) {
      this.log(category, Level.INFO, t, msg, args);
   }

   public void info(LogCategory category, String msg) {
      this.log(category, Level.INFO, msg);
   }

   public void info(LogCategory category, Throwable t, String msg) {
      this.log(category, Level.INFO, t, msg);
   }

   public void debug(LogCategory category, String msg, Object... args) {
      this.log(category, Level.DEBUG, msg, args);
   }

   public void debug(LogCategory category, Throwable t, String msg, Object... args) {
      this.log(category, Level.DEBUG, t, msg, args);
   }

   public void debug(LogCategory category, String msg) {
      this.log(category, Level.DEBUG, msg);
   }

   public void debug(LogCategory category, Throwable t, String msg) {
      this.log(category, Level.DEBUG, t, msg);
   }

   public void trace(LogCategory category, String msg, Object... args) {
      this.log(category, Level.TRACE, msg, args);
   }

   public void trace(LogCategory category, Throwable t, String msg, Object... args) {
      this.log(category, Level.TRACE, t, msg, args);
   }

   public void trace(LogCategory category, String msg) {
      this.log(category, Level.TRACE, msg);
   }

   public void trace(LogCategory category, Throwable t, String msg) {
      this.log(category, Level.TRACE, t, msg);
   }

   public void log(LogCategory category, Level level, String msg, Object... args) {
      if (args.length > 0) {
         if (debug) {
            assert !msg.contains("{}");

            for (Object o : args) {
               assert !(o instanceof Throwable);
            }
         }

         msg = String.format(msg, args);
      }

      this.log(category, level, msg);
   }

   public void log(LogCategory category, Level level, Throwable t, String msg, Object... args) {
      if (args.length > 0) {
         if (debug) {
            assert !msg.contains("{}");

            for (Object o : args) {
               assert !(o instanceof Throwable);
            }
         }

         try {
            msg = String.format(msg, args);
         } catch (Throwable t2) {
            this.log(LogCategory.General, Level.WARN, t2, "Log string format failed.");

            for (Object arg : args) {
               msg = msg + " " + arg;
            }
         }
      }

      this.log(category, level, t, msg);
   }

   public void log(LogCategory category, Level level, String msg) {
      this.getLogger(category).log(level, msg);
   }

   public void log(LogCategory category, Level level, Throwable t, String msg) {
      this.getLogger(category).log(level, msg, t);
   }

   public PrintStream getPrintStream(LogCategory category, Level level) {
      return new PrintStream(new LogOutputStream(this, category, level), true);
   }

   private Logger getLogger(LogCategory category) {
      Logger ret = this.loggers.get(category);
      if (ret == null) {
         ret = LogManager.getLogger(this.parent.getName() + "." + category.name());
         this.loggers.put(category, ret);
      }

      return ret;
   }
}
