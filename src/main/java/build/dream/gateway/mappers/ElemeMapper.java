package build.dream.gateway.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface ElemeMapper {
    Map<String, Object> obtainMappingInfo(@Param("shopId") Long shopId);
}
