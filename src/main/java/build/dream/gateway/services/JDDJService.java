package build.dream.gateway.services;

import build.dream.common.domains.saas.JDDJCode;
import build.dream.common.domains.saas.JDDJToken;
import build.dream.common.utils.CommonRedisUtils;
import build.dream.common.utils.DatabaseHelper;
import build.dream.common.utils.JacksonUtils;
import build.dream.common.utils.UpdateModel;
import build.dream.gateway.constants.Constants;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Map;

@Service
public class JDDJService {
    @Transactional
    public void handleCallback(String code, String token) {
        if (StringUtils.isNotBlank(token)) {
            Map<String, Object> tokenMap = JacksonUtils.readValueAsMap(token, String.class, Object.class);
            String venderId = MapUtils.getString(tokenMap, "venderId");
            UpdateModel updateModel = UpdateModel.builder()
                    .autoSetDeletedFalse()
                    .addContentValue(JDDJToken.ColumnName.DELETED, 1, 1)
                    .addContentValue(JDDJToken.ColumnName.UPDATED_REMARK, "京东到家商家重新授权，删除过期token！", 1)
                    .addSearchCondition(JDDJToken.ColumnName.VENDER_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, venderId)
                    .build();
            DatabaseHelper.universalUpdate(updateModel, JDDJToken.TABLE_NAME);

            JDDJToken jddjToken = JDDJToken.builder()
                    .token(MapUtils.getString(tokenMap, "token"))
                    .expiresIn(MapUtils.getIntValue(tokenMap, "expires_in"))
                    .time(MapUtils.getLongValue(tokenMap, "time"))
                    .uid(MapUtils.getString(tokenMap, "uid"))
                    .userNick(MapUtils.getString(tokenMap, "user_nick"))
                    .venderId(venderId)
                    .createdUserId(BigInteger.ZERO)
                    .updatedUserId(BigInteger.ZERO)
                    .updatedRemark("处理京东到家回调，保存token！")
                    .build();
            DatabaseHelper.insert(jddjToken);
            CommonRedisUtils.hset(Constants.KEY_JDDJ_TOKENS, venderId, JacksonUtils.writeValueAsString(jddjToken));
        }

        if (StringUtils.isNotBlank(code)) {
            Map<String, Object> codeMap = JacksonUtils.readValueAsMap(code, String.class, Object.class);
            String venderId = MapUtils.getString(codeMap, "venderId");

            UpdateModel updateModel = UpdateModel.builder()
                    .autoSetDeletedFalse()
                    .addContentValue(JDDJCode.ColumnName.DELETED, 1, 1)
                    .addContentValue(JDDJCode.ColumnName.UPDATED_REMARK, "京东到家商家重新授权，删除过期code！", 1)
                    .addSearchCondition(JDDJCode.ColumnName.VENDER_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, venderId)
                    .build();
            DatabaseHelper.universalUpdate(updateModel, JDDJCode.TABLE_NAME);

            JDDJCode jddjCode = JDDJCode.builder()
                    .code(MapUtils.getString(codeMap, "code"))
                    .venderId(venderId)
                    .createdUserId(BigInteger.ZERO)
                    .updatedUserId(BigInteger.ZERO)
                    .updatedRemark("处理京东到家回调，保存code！")
                    .build();
            DatabaseHelper.insert(jddjCode);
        }
    }
}
