package crud.config;

import crud.model.Authority;
import crud.model.User;
import crud.service.AuthorityService;
import crud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Collection;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private UserService userService;
    private AuthorityService authorityService;
    PasswordEncoder passwordEncoder;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (httpServletRequest, httpServletResponse, authentication) -> {
            User loggedInUser = (User) authentication.getPrincipal();
            Collection<Authority> authorities = loggedInUser.getAuthorities();
            if (authorities.contains(authorityService.getAuthorityByName("ROLE_ADMIN"))) {
                httpServletResponse.sendRedirect("/admin/table");
            } else if (authorities.contains(authorityService.getAuthorityByName("ROLE_USER"))) {
                Long id = userService.getUserByUsername(loggedInUser.getUsername()).getId();
                httpServletResponse.sendRedirect("/user/info?id=" + id);
            }
        };
    }

    @Autowired
    public SecurityConfig(UserService userService,
                          AuthorityService authorityService,
                          @Lazy PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authorityService = authorityService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .formLogin()
                .successHandler(authenticationSuccessHandler())
            .and()
                .logout();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }
}
