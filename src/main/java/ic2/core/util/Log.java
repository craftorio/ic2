// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import org.apache.logging.log4j.LogManager;
import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.logging.log4j.Level;
import java.util.EnumMap;
import java.util.Map;
import org.apache.logging.log4j.Logger;

public class Log
{
    private static final boolean debug;
    private final Logger parent;
    private final Map<LogCategory, Logger> loggers;
    
    public Log(final Logger parent) {
        this.loggers = new EnumMap<LogCategory, Logger>(LogCategory.class);
        this.parent = parent;
    }
    
    public void error(final LogCategory category, final String msg, final Object... args) {
        this.log(category, Level.FATAL, msg, args);
    }
    
    public void error(final LogCategory category, final Throwable t, final String msg, final Object... args) {
        this.log(category, Level.FATAL, t, msg, args);
    }
    
    public void error(final LogCategory category, final String msg) {
        this.log(category, Level.FATAL, msg);
    }
    
    public void error(final LogCategory category, final Throwable t, final String msg) {
        this.log(category, Level.FATAL, t, msg);
    }
    
    public void warn(final LogCategory category, final String msg, final Object... args) {
        this.log(category, Level.WARN, msg, args);
    }
    
    public void warn(final LogCategory category, final Throwable t, final String msg, final Object... args) {
        this.log(category, Level.WARN, t, msg, args);
    }
    
    public void warn(final LogCategory category, final String msg) {
        this.log(category, Level.WARN, msg);
    }
    
    public void warn(final LogCategory category, final Throwable t, final String msg) {
        this.log(category, Level.WARN, t, msg);
    }
    
    public void info(final LogCategory category, final String msg, final Object... args) {
        this.log(category, Level.INFO, msg, args);
    }
    
    public void info(final LogCategory category, final Throwable t, final String msg, final Object... args) {
        this.log(category, Level.INFO, t, msg, args);
    }
    
    public void info(final LogCategory category, final String msg) {
        this.log(category, Level.INFO, msg);
    }
    
    public void info(final LogCategory category, final Throwable t, final String msg) {
        this.log(category, Level.INFO, t, msg);
    }
    
    public void debug(final LogCategory category, final String msg, final Object... args) {
        this.log(category, Level.DEBUG, msg, args);
    }
    
    public void debug(final LogCategory category, final Throwable t, final String msg, final Object... args) {
        this.log(category, Level.DEBUG, t, msg, args);
    }
    
    public void debug(final LogCategory category, final String msg) {
        this.log(category, Level.DEBUG, msg);
    }
    
    public void debug(final LogCategory category, final Throwable t, final String msg) {
        this.log(category, Level.DEBUG, t, msg);
    }
    
    public void trace(final LogCategory category, final String msg, final Object... args) {
        this.log(category, Level.TRACE, msg, args);
    }
    
    public void trace(final LogCategory category, final Throwable t, final String msg, final Object... args) {
        this.log(category, Level.TRACE, t, msg, args);
    }
    
    public void trace(final LogCategory category, final String msg) {
        this.log(category, Level.TRACE, msg);
    }
    
    public void trace(final LogCategory category, final Throwable t, final String msg) {
        this.log(category, Level.TRACE, t, msg);
    }
    
    public void log(final LogCategory category, final Level level, String msg, final Object... args) {
        if (args.length > 0) {
            if (Log.debug) {
                assert !msg.contains("{}");
                for (final Object o : args) {
                    assert !(o instanceof Throwable);
                }
            }
            msg = String.format(msg, args);
        }
        this.log(category, level, msg);
    }
    
    public void log(final LogCategory category, final Level level, final Throwable t, String msg, final Object... args) {
        if (args.length > 0) {
            if (Log.debug) {
                assert !msg.contains("{}");
                for (final Object o : args) {
                    assert !(o instanceof Throwable);
                }
            }
            try {
                msg = String.format(msg, args);
            }
            catch (final Throwable t2) {
                this.log(LogCategory.General, Level.WARN, t2, "Log string format failed.");
                for (final Object arg : args) {
                    msg = msg + " " + arg;
                }
            }
        }
        this.log(category, level, t, msg);
    }
    
    public void log(final LogCategory category, final Level level, final String msg) {
        this.getLogger(category).log(level, msg);
    }
    
    public void log(final LogCategory category, final Level level, final Throwable t, final String msg) {
        this.getLogger(category).log(level, msg, t);
    }
    
    public PrintStream getPrintStream(final LogCategory category, final Level level) {
        return new PrintStream(new LogOutputStream(this, category, level), true);
    }
    
    private Logger getLogger(final LogCategory category) {
        Logger ret = this.loggers.get(category);
        if (ret == null) {
            ret = LogManager.getLogger(this.parent.getName() + "." + category.name());
            this.loggers.put(category, ret);
        }
        return ret;
    }
    
    static {
        debug = Util.hasAssertions();
    }
}
