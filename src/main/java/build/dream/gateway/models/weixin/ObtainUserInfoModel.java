package build.dream.gateway.models.weixin;

import build.dream.common.models.BasicModel;
import build.dream.common.utils.ApplicationHandler;
import build.dream.gateway.constants.Constants;

import javax.validation.constraints.NotNull;

public class ObtainUserInfoModel extends BasicModel {
    private static final String[] SCOPES = {Constants.SNSAPI_BASE, Constants.SNSAPI_USERINFO};
    @NotNull
    private String appId;

    @NotNull
    private String scope;

    @NotNull
    private String redirectUri;

    private String state;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public void validateAndThrow() {
        super.validateAndThrow();
        ApplicationHandler.inArray(SCOPES, scope, "scope");
    }
}
