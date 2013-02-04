package eu.cloudtm.reporter.logging;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 4.0
 */
public class LogFactory {

   public static enum LogLevel {
      TRACE("trace", Level.ALL),
      INFO("info", Level.INFO),
      WARN("warn", Level.WARNING),
      ERROR("error", Level.SEVERE);

      final String name;
      final Level level;

      private LogLevel(String name, Level level) {
         this.name = name;
         this.level = level;
      }

      public static LogLevel fromString(String name) {
         for (LogLevel logLevel : values()) {
            if (logLevel.name.equalsIgnoreCase(name)) {
               return logLevel;
            }
         }
         return null;
      }
   }

   private static final Map<Class, Log> logs = new HashMap<Class, Log>();
   private static LogLevel current = LogLevel.TRACE;

   private static final FileHandler handler = new FileHandler();

   public synchronized static Log getLog(Class clazz) {
      Log log = logs.get(clazz);
      if (log == null) {
         log = new Log(createLogger(clazz));
         logs.put(clazz, log);
      }
      return log;
   }

   public synchronized static void setLogLevel(String logLevel) {
      LogLevel newLogLevel = LogLevel.fromString(logLevel);
      if (newLogLevel == null) {
         return;
      }
      current = newLogLevel;
      for (Log log : logs.values()) {
         log.setLevel(newLogLevel);
      }
   }

   public synchronized static void setOutputFile(String filename) {
      if (filename == null || filename.isEmpty()) {
         return;
      }

      try {
         handler.setPrintStream(new PrintStream(filename));
      } catch (FileNotFoundException e) {
         //ignore
      }
   }

   private static Logger createLogger(Class clazz) {
      Logger logger = Logger.getLogger(clazz.getName());
      logger.setUseParentHandlers(false);
      logger.addHandler(handler);
      logger.setLevel(current.level);
      return logger;
   }

   private static class FileHandler extends Handler {
      private static final String LOG_FORMAT = "%s [%s] %s";
      private PrintStream printStream = System.out;
      private boolean closed = false;

      @Override
      public synchronized void publish(LogRecord logRecord) {
         if (closed) {
            return;
         }
         String levelName = "UNKNOWN";
         Level level = logRecord.getLevel();
         for (LogLevel logLevel : LogLevel.values()) {
            if (logLevel.level == level) {
               levelName = logLevel.name.toUpperCase();
               break;
            }
         }

         printStream.println(String.format(LOG_FORMAT, logRecord.getMillis(), levelName, logRecord.getMessage()));
      }

      @Override
      public synchronized void flush() {
         if (!closed) {
            printStream.flush();
         }
      }

      @Override
      public synchronized void close() throws SecurityException {
         if (printStream != System.out) {
            printStream.close();
         }
         closed = true;
      }

      public synchronized void setPrintStream(PrintStream printStream) {
         this.printStream = printStream;
      }
   }

}
