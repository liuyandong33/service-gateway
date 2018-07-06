package build.dream.gateway.filters;

import build.dream.gateway.constants.Constants;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UrlPathFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return FilterConstants.ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        String serviceId = requestContext.get(FilterConstants.PROXY_KEY).toString();
        if (Constants.SERVICE_NAME_POSAPI.equals(serviceId)) {
            Map<String, List<String>> requestQueryParams = (Map<String, List<String>>) requestContext.get("requestQueryParams");
            if (CollectionUtils.isNotEmpty(requestQueryParams.get("partitionCode"))) {
                return true;
            }

            if (CollectionUtils.isNotEmpty(requestQueryParams.get("tenantId"))) {
                return true;
            }

            if (CollectionUtils.isNotEmpty(requestQueryParams.get("branchId"))) {
                return true;
            }

            if (CollectionUtils.isNotEmpty(requestQueryParams.get("tenantCode"))) {
                return true;
            }

            if (CollectionUtils.isNotEmpty(requestQueryParams.get("branchCode"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        Map<String, List<String>> requestQueryParams = (Map<String, List<String>>) requestContext.get("requestQueryParams");
        String partitionCode = StringUtils.join(requestQueryParams.get("partitionCode"), ",");
        if (StringUtils.isNotBlank(partitionCode)) {
            requestContext.put(FilterConstants.PROXY_KEY, Constants.SERVICE_NAME_PLATFORM);
            return null;
        }

        String tenantId = StringUtils.join(requestQueryParams.get("tenantId"), ",");
        if (StringUtils.isNotBlank(tenantId)) {
            requestContext.put(FilterConstants.PROXY_KEY, Constants.SERVICE_NAME_PLATFORM);
            return null;
        }

        String branchId = StringUtils.join(requestQueryParams.get("branchId"), ",");
        if (StringUtils.isNotBlank(branchId)) {
            requestContext.put(FilterConstants.PROXY_KEY, Constants.SERVICE_NAME_PLATFORM);
            return null;
        }

        String tenantCode = StringUtils.join(requestQueryParams.get("tenantCode"), ",");
        if (StringUtils.isNotBlank(tenantCode)) {
            requestContext.put(FilterConstants.PROXY_KEY, Constants.SERVICE_NAME_PLATFORM);
            requestContext.put(FilterConstants.REQUEST_URI_KEY, "/order/obtainOrderInfo");
            return null;
        }

        String branchCode = StringUtils.join(requestQueryParams.get("branchCode"), ",");
        if (StringUtils.isNotBlank(branchCode)) {
            requestContext.put(FilterConstants.PROXY_KEY, Constants.SERVICE_NAME_PLATFORM);
            return null;
        }
        return null;
    }
}
