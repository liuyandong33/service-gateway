package build.dream.gateway.services;

import build.dream.common.saas.domains.WeiXinOpenPlatformApplication;
import build.dream.common.saas.domains.WeiXinPublicAccount;
import build.dream.common.utils.AESUtils;
import build.dream.common.utils.DatabaseHelper;
import build.dream.common.utils.SearchModel;
import build.dream.gateway.constants.Constants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
public class WeiXinService {
    @Transactional(readOnly = true)
    public WeiXinPublicAccount obtainWeiXinPublicAccount(String appId) {
        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition(WeiXinPublicAccount.ColumnName.APP_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, appId);
        WeiXinPublicAccount weiXinPublicAccount = DatabaseHelper.find(WeiXinPublicAccount.class, searchModel);
        return weiXinPublicAccount;
    }

    @Transactional(readOnly = true)
    public WeiXinOpenPlatformApplication obtainWeiXinOpenPlatformApplication(String appId) {
        SearchModel searchModel = new SearchModel(true);
        searchModel.addSearchCondition(WeiXinOpenPlatformApplication.ColumnName.APP_ID, Constants.SQL_OPERATION_SYMBOL_EQUAL, appId);
        WeiXinOpenPlatformApplication weiXinOpenPlatformApplication = DatabaseHelper.find(WeiXinOpenPlatformApplication.class, searchModel);
        return weiXinOpenPlatformApplication;
    }

    public String decrypt(String data, String encodingAesKey) {
        byte[] encryptedData = Base64.decodeBase64(data);
        byte[] aesKey = Base64.decodeBase64(encodingAesKey);
        byte[] iv = Arrays.copyOfRange(aesKey, 0, 16);

        byte[] original = AESUtils.decrypt(encryptedData, aesKey, iv, AESUtils.ALGORITHM_AES_CBC_NOPADDING);
        byte[] bytes = original = decode(original);

        byte[] networkOrder = Arrays.copyOfRange(bytes, 16, 20);
        int xmlLength = recoverNetworkBytesOrder(networkOrder);

        String plaintext = new String(Arrays.copyOfRange(original, 20, 20 + xmlLength), build.dream.common.constants.Constants.CHARSET_UTF_8);
        return plaintext;
    }

    public String encrypt(String data, String encodingAesKey, String appId) {
        String randomString = RandomStringUtils.randomAlphanumeric(16);

        byte[] randomBytes = randomString.getBytes(Constants.CHARSET_UTF_8);
        byte[] dataBytes = data.getBytes(Constants.CHARSET_UTF_8);
        byte[] networkBytes = getNetworkBytesOrder(dataBytes.length);
        byte[] appIdBytes = appId.getBytes(Constants.CHARSET_UTF_8);

        byte[] bytes = new byte[0];
        ArrayUtils.addAll(bytes, randomBytes);
        ArrayUtils.addAll(bytes, networkBytes);
        ArrayUtils.addAll(bytes, dataBytes);
        ArrayUtils.addAll(bytes, appIdBytes);
        ArrayUtils.addAll(bytes, encodePKCS7(bytes.length));

        byte[] aesKey = Base64.decodeBase64(encodingAesKey);
        byte[] encrypted = AESUtils.encrypt(bytes, aesKey, ArrayUtils.subarray(aesKey, 0, 16), AESUtils.ALGORITHM_AES_CBC_NOPADDING);
        return Base64.encodeBase64String(encrypted);
    }

    private byte[] getNetworkBytesOrder(int sourceNumber) {
        byte[] orderBytes = new byte[4];
        orderBytes[3] = (byte) (sourceNumber & 0xFF);
        orderBytes[2] = (byte) (sourceNumber >> 8 & 0xFF);
        orderBytes[1] = (byte) (sourceNumber >> 16 & 0xFF);
        orderBytes[0] = (byte) (sourceNumber >> 24 & 0xFF);
        return orderBytes;
    }

    private byte[] encodePKCS7(int count) {
        int BLOCK_SIZE = 32;
        int amountToPad = BLOCK_SIZE - (count % BLOCK_SIZE);
        if (amountToPad == 0) {
            amountToPad = BLOCK_SIZE;
        }
        byte target = (byte) (amountToPad & 0xFF);
        char padChar = (char) target;
        String tmp = new String();
        for (int index = 0; index < amountToPad; index++) {
            tmp += padChar;
        }
        return tmp.getBytes(Constants.CHARSET_UTF_8);
    }

    private byte[] decode(byte[] decrypted) {
        int pad = (int) decrypted[decrypted.length - 1];
        if (pad < 1 || pad > 32) {
            pad = 0;
        }
        return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
    }

    private int recoverNetworkBytesOrder(byte[] orderBytes) {
        int sourceNumber = 0;
        for (int i = 0; i < 4; i++) {
            sourceNumber <<= 8;
            sourceNumber |= orderBytes[i] & 0xff;
        }
        return sourceNumber;
    }
}
