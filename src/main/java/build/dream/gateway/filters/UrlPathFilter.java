package build.dream.gateway.filters;

import build.dream.common.saas.domains.Tenant;
import build.dream.common.utils.CommonUtils;
import build.dream.common.utils.ConfigurationUtils;
import build.dream.gateway.constants.Constants;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UrlPathFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        String serviceId = requestContext.get(FilterConstants.SERVICE_ID_KEY).toString();
        if ((ConfigurationUtils.getConfigurationSafe(Constants.DEPLOYMENT_ENVIRONMENT) + "-" + Constants.SERVICE_NAME_POSAPI).equals(serviceId)) {
            Map<String, List<String>> requestQueryParams = (Map<String, List<String>>) requestContext.get("requestQueryParams");
            if (MapUtils.isEmpty(requestQueryParams)) {
                return false;
            }
            if (CollectionUtils.isNotEmpty(requestQueryParams.get("partitionCode"))) {
                return true;
            }

            if (CollectionUtils.isNotEmpty(requestQueryParams.get("tenantId"))) {
                return true;
            }

            if (CollectionUtils.isNotEmpty(requestQueryParams.get("tenantCode"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        Map<String, List<String>> requestQueryParams = (Map<String, List<String>>) requestContext.get("requestQueryParams");
        if (MapUtils.isEmpty(requestQueryParams)) {
            return null;
        }
        String partitionCode = StringUtils.join(requestQueryParams.get("partitionCode"), ",");
        String deploymentEnvironment = ConfigurationUtils.getConfigurationSafe(Constants.DEPLOYMENT_ENVIRONMENT);
        if (StringUtils.isNotBlank(partitionCode)) {
            requestContext.put(FilterConstants.SERVICE_ID_KEY, deploymentEnvironment + "-" + partitionCode + "-" + Constants.SERVICE_NAME_POSAPI);
            return null;
        }

        List<String> tenantIds = requestQueryParams.get("tenantId");
        if (CollectionUtils.isNotEmpty(tenantIds)) {
            String tenantId = tenantIds.get(0);
            Tenant tenant = CommonUtils.obtainTenantInfo(tenantId, null);
            if (tenant != null) {
                requestContext.put(FilterConstants.SERVICE_ID_KEY, deploymentEnvironment + "-" + tenant.getPartitionCode() + "-" + Constants.SERVICE_NAME_POSAPI);
                return null;
            }
        }

        List<String> tenantCodes = requestQueryParams.get("tenantCode");
        if (CollectionUtils.isNotEmpty(tenantCodes)) {
            String tenantCode = tenantCodes.get(0);
            Tenant tenant = CommonUtils.obtainTenantInfo(null, tenantCode);
            if (tenant != null) {
                requestContext.put(FilterConstants.SERVICE_ID_KEY, deploymentEnvironment + "-" + tenant.getPartitionCode() + "-" + Constants.SERVICE_NAME_POSAPI);
                return null;
            }
        }
        return null;
    }
}
