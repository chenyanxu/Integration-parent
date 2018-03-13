package com.kalix.integration.rongcloud.biz;
import com.kalix.integration.rongcloud.rong.io.rong.RongCloud;
import com.kalix.integration.rongcloud.rong.io.rong.models.TokenResult;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by fwb on 2018/3/12.
 * 根据当前的应用，动态加载初始化融云token
 */
@Command(scope = "kalix", name = "init-rongcloud", description = "init rongcloud token")
@Service
public class InitRongCloudCommand implements Action {
    @Argument(index = 0, name = "arg", description = "The command argument", required = false, multiValued = false)
    @Completion(MyCompleter.class)
   private String appKey = "kj7swf8okidb2";//替换成您的appkey
   private String appSecret = "xCcyoNpqIh";//替换成匹配上面key的secret

    @Override
    public Object execute() throws Exception {
        init();
        return null;
    }

    private void init() {
        DataSource dataSource = Util.getKalixDataSource();
        RongCloud rongCloud = RongCloud.getInstance(appKey, appSecret);
        String sql="";
        Statement statement=null;
        Connection conn=null;
        ResultSet rs=null;
        // 获取 Token 方法
        TokenResult userGetTokenResult = null;
        try {
             conn = dataSource.getConnection();
             statement = conn.createStatement();
             conn.setAutoCommit(false);
             rs=statement.executeQuery("SELECT * from SYS_USER order by id ");
            // int i=0;
            while (rs.next())
            {
                //频繁调用融云接口会被拒绝，延迟五秒
                Thread.currentThread().sleep(5000);//毫秒
              long userid= rs.getInt("id");
              System.out.println(userid);
              String username= rs.getString("name");
              String userImage=rs.getString("icon");
              userGetTokenResult = rongCloud.user.getToken(String.valueOf(userid), username, "http://localhost:5984/kalix/332d506754c04760adf3646b6312d394/S206A-239+853110-239+855770.jpg");
              sql="INSERT INTO public.sys_token_user (id, createby, creationdate, updateby, updatedate,version_,token,userid) " +
                      "                               VALUES ('"+userid+"',null,null,null,null,null,'"+userGetTokenResult.getToken()+"','"+userid+"')";
                statement.addBatch(sql);
//                i++;
//                if(i==20)
//                {
//                    break;
//                }
            }
            statement.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

            if(rs!=null)
            {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if(statement!=null)
            {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(conn!=null)
            {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        Util.outPrint("脚本执行成功！");
    }

    /**
     * clear data of db
     */
    private void beforeClearData() {
//        StringReader reader = new StringReader(appClearSql + funClearSql + role_funClearSql + app_funClearSql);
//        try {
//            Util.outPrint("clean application data!");
//            scriptRunner.runScript(reader);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    public static void main(String[] args) throws Exception {
        InitRongCloudCommand n =new InitRongCloudCommand();
        n.execute();
    }
}

