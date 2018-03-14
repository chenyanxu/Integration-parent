package com.kalix.integration.rongcloud.biz;
import com.kalix.framework.core.util.ConfigUtil;
import com.kalix.framework.core.util.StringUtils;
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
    private static String CONFIG_FILE_NAME = "ConfigWebContext";
    @Override
    public Object execute() throws Exception {
        init();
        return null;
    }

    private void init() {
        DataSource dataSource = Util.getKalixDataSource();
        String appKey = (String) ConfigUtil.getConfigProp("appKey", CONFIG_FILE_NAME);
        String appSecret = (String) ConfigUtil.getConfigProp("appSecret", CONFIG_FILE_NAME);
        RongCloud rongCloud = RongCloud.getInstance(appKey, appSecret);

        String serverUrl = (String) ConfigUtil.getConfigProp("server_url", CONFIG_FILE_NAME);
        String sql="";
        Statement statement=null;
        Connection conn=null;
        ResultSet rs=null;
        String userHeaderImg="";
        // 获取 Token 方法
        TokenResult userGetTokenResult = null;
        try {
             conn = dataSource.getConnection();
             statement = conn.createStatement();
             statement.execute("DELETE from public.sys_token_user");
             conn.setAutoCommit(false);
             rs=statement.executeQuery("SELECT * from SYS_USER order by id ");
            // int i=0;
            while (rs.next())
            {
                //频繁调用融云接口会被拒绝，延迟2秒
                Thread.currentThread().sleep(2000);//毫秒
              long userid= rs.getInt("id");
              System.out.println(userid);
              String username= rs.getString("name");
              String userImage=rs.getString("icon");
              if(StringUtils.isNotEmpty(userImage))
              {
                  userHeaderImg=userImage;
              }else
              {
                  userHeaderImg=serverUrl+"/static/images/default_user.png";
              }
              userGetTokenResult = rongCloud.user.getToken(String.valueOf(userid), username, userHeaderImg);
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


    public static void main(String[] args) throws Exception {
        InitRongCloudCommand n =new InitRongCloudCommand();
        n.execute();
    }
}

