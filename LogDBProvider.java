import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * Provider the connection and save logs into DB
 */
public class LogDBProvider {

    private Map dbParams;
    private Connection connection;
    private Properties connectionProps;

    public LogDBProvider(Map dbParamsMap) {
        this.connectionProps = new Properties();
        this.dbParams = dbParamsMap;
        this.connectionProps.put("user", dbParams.get("userName"));
        this.connectionProps.put("password", dbParams.get("password"));
    }

    public Connection getConnection() {

        try {
            this.connection = new Connection();
            this.connection = DriverManager.getConnection(
                    String.format("jdbc:%s://%s:%s/", this.dbParams.get("dbms"), this.dbParams.get("serverName"), this.dbParams.get("portNumber")),
                    this.connectionProps);
        } catch(SQLException se){
            se.printStackTrace();
            throw new Exception("Error to connect DB ", se.message);
        } catch(Exception e){
            e.printStackTrace();
            throw new Exception(e.message);
        }
    }

    public insertLog (String logMsg, int logType) throws IOException {
        try {
            String querySql =  String.format("insert into Log_Values('%s', %s)", logMsg, String.valueOf(logType));
            Statement stmt = this.connection.createStatement();
            stmt.executeUpdate(querySql);
        } catch(SQLException se){
            se.printStackTrace();
            throw new Exception("Error while insert in DB ", se.message);
        } catch(Exception e){
            e.printStackTrace();
            throw new Exception(e.message);
        } finally {
            this.connection.close();
        }
    }

    public closeConnection () {
        try {
            if(this.connection != null) {
                this.connection.close();
            }
        }catch(SQLException se){
            se.printStackTrace();
        }
    }

}