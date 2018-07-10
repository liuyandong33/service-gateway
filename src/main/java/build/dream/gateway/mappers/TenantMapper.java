package build.dream.gateway.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigInteger;
import java.util.Map;

@Mapper
public interface TenantMapper {
    Map<String, Object> obtainTenantInfo(@Param("shopId") BigInteger shopId);
}
