package eu.cloudtm.reporter.logging;

import eu.cloudtm.reporter.manager.ResourceInfo;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eu.cloudtm.reporter.logging.LogFactory.LogLevel;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 4.0
 */
public class Log {

   private final Logger logger;

   Log(Logger logger) {
      this.logger = logger;
   }

   final void setLevel(LogFactory.LogLevel logLevel) {
      logger.setLevel(logLevel.level);
   }

   public final void logErrorUpdatingAttributes(ResourceInfo where, List<String> attributes, Throwable throwable) {
      if (logger.isLoggable(LogLevel.TRACE.level)) {
         logger.log(LogLevel.WARN.level, String.format("[%s] error updating attributes %s", where, attributes), throwable);
      } else {
         log(LogLevel.WARN.level, "[%s] error updating attributes %s. %s", where, attributes, throwable.getMessage());
      }
   }

   public final void warn(String format, Object... values) {
      log(LogLevel.WARN.level, format, values);
   }

   public final void info(String format, Object... values) {
      log(LogLevel.INFO.level, format, values);
   }

   public final void trace(String format, Object... values) {
      log(LogLevel.TRACE.level, format, values);
   }

   public final void error(String format, Object... values) {
      log(LogLevel.ERROR.level, format, values);
   }

   private void log(Level level, String format, Object... values) {
      if (logger.isLoggable(level)) {
         logger.log(level, String.format(format, values));
      }
   }
}
