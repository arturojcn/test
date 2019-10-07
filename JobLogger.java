import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
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

  public JobLogger(Logger logger, boolean logToFileParam, boolean logToConsoleParam, boolean logToDatabaseParam,
      boolean logMessageParam, boolean logWarningParam, boolean logErrorParam, Map dbParamsMap) {
    logger = logger;
    logError = logErrorParam;
    logMessage = logMessageParam;
    logWarning = logWarningParam;
    logToDatabase = logToDatabaseParam;
    logToFile = logToFileParam;
    logToConsole = logToConsoleParam;
    dbParams = dbParamsMap;

    if (!this.logToConsole && !this.logToFile && !this.logToDatabase ||
            !this.logError && !this.logMessage && !this.logWarning) {
      throw new Exception("Invalid configuration or you must specify the log level EJ: Error, Warning or Message");
    }
  }

  public static void LogMessage(String messageText, boolean message, boolean warning, boolean error) throws Exception {

    if (messageText == null || messageText.trim().isEmpty()) {
      return;
    }

    if (!message && !warning && !error) {
      throw new Exception("Error or Warning or Message must be specified");
    }

    int logType = 0;
    String logMsg = ''
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

    Connection connection = null;
    Properties connectionProps = new Properties();
    connectionProps.put("user", dbParams.get("userName"));
    connectionProps.put("password", dbParams.get("password"));

    connection = DriverManager.getConnection(
        "jdbc:" + dbParams.get("dbms") + "://" + dbParams.get("serverName") + ":" + dbParams.get("portNumber") + "/",
        connectionProps);

    File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");
    if (!logFile.exists()) {
      logFile.createNewFile();
    }

    FileHandler fh = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt");
    ConsoleHandler ch = new ConsoleHandler();

    if (logToFile) {
      logger.addHandler(fh);
      logger.log(Level.INFO, messageText);
    }

    if (logToConsole) {
      logger.addHandler(ch);
      logger.log(Level.INFO, messageText);
    }

    if (logToDatabase) {
      stmt.executeUpdate("insert into Log_Values('" + logMsg + "', " + String.valueOf(logType) + ")");
    }
  }
}
