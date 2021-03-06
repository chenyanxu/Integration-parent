package com.kalix.integration.rongcloud.biz;


import com.kalix.admin.core.api.biz.IAddFieldService;
import com.kalix.admin.core.entities.UserBean;
import com.kalix.framework.core.util.ConfigUtil;
import com.kalix.integration.rongcloud.api.dao.ITokenUserBeanDao;
import com.kalix.integration.rongcloud.rong.io.rong.models.TokenResult;
import com.kalix.integration.rongcloud.entities.TokenUserBean;
import com.kalix.integration.rongcloud.rong.io.rong.RongCloud;
import com.kalix.framework.core.api.persistence.JsonStatus;
import com.kalix.framework.core.impl.biz.ShiroGenericBizServiceImpl;


/**
 * @类描述：角色服务类
 * @创建人：sunlf
 * @创建时间：2014-05-14 上午10:43
 * @修改人：
 * @修改时间：
 * @修改备注：
 */

public class TokenUserBeanServiceImpl extends ShiroGenericBizServiceImpl<ITokenUserBeanDao, TokenUserBean>  implements IAddFieldService{

//    String appKey = "kj7swf8okidb2";//替换成您的appkey
//    String appSecret = "xCcyoNpqIh";//替换成匹配上面key的secret
    private static String CONFIG_FILE_NAME = "ConfigWebContext";
    public JsonStatus setField(UserBean entity)
    {

        String appKey = (String) ConfigUtil.getConfigProp("appKey", CONFIG_FILE_NAME);
        String appSecret = (String) ConfigUtil.getConfigProp("appSecret", CONFIG_FILE_NAME);
        JsonStatus jsonStatus = new JsonStatus();
        RongCloud rongCloud = RongCloud.getInstance(appKey, appSecret);

        // 获取 Token 方法
        TokenResult userGetTokenResult = null;
        try {
            userGetTokenResult = rongCloud.user.getToken(String.valueOf(entity.getId()), entity.getName(), entity.getIcon());
            TokenUserBean tokenUserBean = new TokenUserBean();
            tokenUserBean.setToken(userGetTokenResult.getToken());
            tokenUserBean.setUserId(entity.getId());
            dao.save(tokenUserBean);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonStatus;
    }
}