
package com.uber.uberapi.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class UberSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/passenger/**").hasAnyRole("PASSENGER", "ADMIN")
                .antMatchers("/driver/**").hasAnyRole("DRIVER", "ADMIN")
                .antMatchers("/**").permitAll()
                .and()
                .formLogin();

    }
}
