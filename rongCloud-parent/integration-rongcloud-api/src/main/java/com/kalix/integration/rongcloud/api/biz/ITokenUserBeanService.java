package com.kalix.integration.rongcloud.api.biz;



import com.kalix.framework.core.api.biz.IBizService;
import com.kalix.framework.core.api.persistence.JsonStatus;
import com.kalix.integration.rongcloud.entities.TokenUserBean;

/**
 * @类描述：获取融云token接口类
 * @创建人：fwb
 * @创建时间：2014-04-03 下午6:29
 * @修改人：
 * @修改时间：
 * @修改备注：
 */

public interface ITokenUserBeanService extends IBizService<TokenUserBean> {

    JsonStatus setToken(Long userId, String userName, String userImg);


}
