package build.dream.gateway.configurations;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfiguration {
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "primary.datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().type(DruidDataSource.class).build();
    }
}
