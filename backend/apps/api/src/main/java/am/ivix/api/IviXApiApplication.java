
package am.ivix.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "am.ivix.api",          // контроллеры и конфигурации API
        "am.ivix.users",        // сервисы пользователей
        "am.ivix.profiles",     // профили
        "am.ivix.catalog"       // каталог и категории
})
@EnableJpaRepositories(basePackages = {
        "am.ivix.users.repo",
        "am.ivix.profiles.repo",
        "am.ivix.catalog.repo"
})
@EntityScan(basePackages = {
        "am.ivix.users.domain",
        "am.ivix.profiles.domain",
        "am.ivix.catalog.domain"
})
public class IviXApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(IviXApiApplication.class, args);
    }
}
