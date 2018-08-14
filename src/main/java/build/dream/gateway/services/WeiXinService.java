package build.dream.gateway.services;

import build.dream.common.saas.domains.WeiXinOpenPlatformApplication;
import build.dream.common.saas.domains.WeiXinPublicAccount;
import build.dream.common.utils.DatabaseHelper;
import build.dream.common.utils.SearchModel;
import build.dream.gateway.constants.Constants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeiXinService {
    @Transactional(readOnly = true)
    public WeiXinPublicAccount obtainWeiXinPublicAccount(String appId) {
        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition("app_id", Constants.SQL_OPERATION_SYMBOL_EQUAL, appId);
        WeiXinPublicAccount weiXinPublicAccount = DatabaseHelper.find(WeiXinPublicAccount.class, searchModel);
        return weiXinPublicAccount;
    }

    @Transactional(readOnly = true)
    public WeiXinOpenPlatformApplication obtainWeiXinOpenPlatformApplication(String appId) {
        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition("app_id", Constants.SQL_OPERATION_SYMBOL_EQUAL, appId);
        WeiXinOpenPlatformApplication weiXinOpenPlatformApplication = DatabaseHelper.find(WeiXinOpenPlatformApplication.class, searchModel);
        return weiXinOpenPlatformApplication;
    }
}
