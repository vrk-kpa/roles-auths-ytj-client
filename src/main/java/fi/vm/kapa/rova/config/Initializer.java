/**
 * The MIT License
 * Copyright (c) 2016 Population Register Centre
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fi.vm.kapa.rova.config;

import fi.vm.kapa.rova.engine.evaluation.BaseSpringConfig;
import fi.vm.kapa.rova.logging.LogbackConfigurator;
import fi.vm.kapa.rova.logging.MDCFilter;
import fi.vm.kapa.rova.ribbon.MetadataAwarePredicate;
import fi.vm.kapa.rova.ytj.YTJ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.EnumSet;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class Initializer extends BaseSpringConfig implements WebApplicationInitializer {

    private static final String MDC_FILTER = "mdcFilter";

    @Autowired
    private LogbackConfigurator logConfigurator;

    @Bean
    public EmbeddedServletContainerCustomizer embeddedServletContainerCustomizer() {
        return logConfigurator.containerCustomizer();
    }

    @Override
    public void onStartup(ServletContext ctx) throws ServletException {
        ctx.addFilter(MDC_FILTER, MDCFilter.class)
                .addMappingForUrlPatterns(
                        EnumSet.of(DispatcherType.REQUEST,
                                DispatcherType.FORWARD), false, "/*");
    }

    @Bean
    public EurekaInstanceConfigBean eurekaInstanceConfig(InetUtils inetUtils) {
        EurekaInstanceConfigBean b = new EurekaInstanceConfigBean(inetUtils);
        b.getMetadataMap().put(MetadataAwarePredicate.API_VERSION_METADATA_FIELD, YTJ.API_VERSION);
        return b;
    }
}
