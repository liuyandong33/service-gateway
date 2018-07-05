package build.dream.gateway.services;

import build.dream.common.saas.domains.NotifyRecord;
import build.dream.common.utils.DatabaseHelper;
import build.dream.common.utils.SearchModel;
import build.dream.common.utils.ValidateUtils;
import build.dream.gateway.constants.Constants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class NotifyService {
    @Transactional(rollbackFor = Exception.class)
    public void alipayCallback(Map<String, String> callbackParameters) {
        String outTradeNo = callbackParameters.get("out_trade_no");
        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition("uuid", Constants.SQL_OPERATION_SYMBOL_EQUAL, outTradeNo);
        NotifyRecord notifyRecord = DatabaseHelper.find(NotifyRecord.class, searchModel);
        ValidateUtils.notNull(notifyRecord, "通知记录不存在！");
    }
}
