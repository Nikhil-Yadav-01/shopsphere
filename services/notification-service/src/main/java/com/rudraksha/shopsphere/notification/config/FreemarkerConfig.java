package com.rudraksha.shopsphere.notification.config;

import freemarker.template.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

@Configuration
public class FreemarkerConfig {

    @Bean
    public FreeMarkerConfigurationFactoryBean getFreeMarkerConfiguration() {
        FreeMarkerConfigurationFactoryBean bean = new FreeMarkerConfigurationFactoryBean();
        bean.setTemplateLoaderPath("classpath:/templates");
        return bean;
    }

    @Bean
    public Configuration freeMarkerConfiguration(FreeMarkerConfigurationFactoryBean factory) throws Exception {
        Configuration configuration = factory.getObject();
        configuration.setDefaultEncoding("UTF-8");
        configuration.setClassicCompatibleMode(true);
        return configuration;
    }
}
