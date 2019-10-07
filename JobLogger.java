import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobLogger {
  private static boolean logToFile;
  private static boolean logToConsole;
  private static boolean logMessage;
  private static boolean logWarning;
  private static boolean logError;
  private static boolean logToDatabase;
  private boolean initialized;
  private static Map dbParams;
  private static Logger logger;

  public static class Builder {
    private static boolean logToFile = false;
    private static boolean logToConsole = false;
    private static boolean logMessage = false;
    private static boolean logWarning = false;
    private static boolean logError = false;
    private static boolean logToDatabase = false;
    private static Map dbParams;
    private static Logger logger;

    public Builder() {
      this.logger = Logger.getLogger("MyLog");
    }

    public Builder withLogToFile() {
      this.logToFile = true;
      return this;
    }

    public Builder withLogToConsole() {
      this.logToConsole = true;
      return this;
    }

    public Builder withLogMessage() {
      this.logMessage = true;
      return this;
    }

    public Builder withLogWarning() {
      this.logWarning = true;
      return this;
    }

    public Builder withLogError() {
      this.logError = true;
      return this;
    }

    public Builder withLogToDatabase(final Map dbParams) {
      this.logToDatabase = true;
      this.dbParams = dbParams;
      return this;
    }

    public JobLogger build(){
      return new JobLogger(logger, logToFileParam, logToConsoleParam, logToDatabaseParam,
              logMessageParam, logWarningParam, logErrorParam, dbParamsMap);
    }
  }

  private JobLogger(Logger logger, boolean logToFileParam, boolean logToConsoleParam, boolean logToDatabaseParam,
      boolean logMessageParam, boolean logWarningParam, boolean logErrorParam, Map dbParamsMap) {
    this.logger = logger;
    this.logError = logErrorParam;
    this.logMessage = logMessageParam;
    this.logWarning = logWarningParam;
    this.logToDatabase = logToDatabaseParam;
    this.logToFile = logToFileParam;
    this.logToConsole = logToConsoleParam;
    this.dbParams = dbParamsMap;

    if (!this.logToConsole && !this.logToFile && !this.logToDatabase ||
            !this.logError && !this.logMessage && !this.logWarning) {
      throw new Exception("Invalid configuration or you must specify the log level EJ: Error, Warning or Message");
    }
  }

  private static void logMessage(String messageText, boolean message, boolean warning, boolean error) throws Exception {

    if (messageText == null || messageText.trim().isEmpty()) {
      return;
    }

    if (!message && !warning && !error) {
      throw new Exception("Error or Warning or Message must be specified");
    }

    int logType = 0;
    String logMsg = '';
    messageText = messageText.trim();
    String baseMsg = String.format("%s %s", DateFormat.getDateInstance(DateFormat.LONG).format(new Date()), messageText);

    if (message && this.logMessage) {
      logType = 1;
      logMsg = String.format("%s message %s", logMsg, baseMsg);
    }

    if (error && this.logError) {
      logType = 2;
      logMsg = String.format("%s error %s", logMsg, baseMsg);
    }

    if (warning && this.logWarning) {
      logType = 3;
      logMsg = String.format("%s warning %s", logMsg, baseMsg);
    }

    if (logToFile) {
      this.logFile(messageText);
    }

    if (logToConsole) {
      this.logConsole(messageText);
    }

    if (logToDatabase) {
      this.logDB(String logMsg, int logType);
    }
  }

  private static void logFile(String messageText) {
    String filePath = String.format("%s/logFile.txt", this.dbParams.get("logFileFolder");
    File logFile = new File(filePath);
    FileHandler fh = new FileHandler(filePath);
    if (!logFile.exists()) {
      logFile.createNewFile();
    }
    this.logger.addHandler(fh);
    this.logger.log(Level.INFO, messageText);
  }

  private static void logConsole(String messageText) {
    ConsoleHandler ch = new ConsoleHandler();
    this.logger.addHandler(ch);
    this.logger.log(Level.INFO, messageText);
  }

  private static void logDB(String logMsg, int logType) {
    LogDBProvider logDBProvider = new LogDBProvider(dbParamsMap);

    try {
      logDBProvider.getConnection();
      logDBProvider.insertLog(logMsg, logType);
    } catch (Exception e) {
      logDBProvider.closeConnection();
    }
  }

  public logMessage(String msg) {
    this.logMessage(msg, true, false, false);
  }

  public logWarning(String msg) {
    this.logMessage(msg, false, true, false);
  }

  public logError(String msg) {
    this.logMessage(msg, false, false, true);
  }
}
