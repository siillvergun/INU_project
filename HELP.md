# Getting Started

### Local PostgreSQL

IntelliJ에서 Spring Boot 앱을 `local` 프로필로 실행하려면 먼저 로컬 PostgreSQL 서버를 실행합니다.

```bash
pg_ctl -D /opt/homebrew/var/postgresql@18 -l /opt/homebrew/var/log/postgresql@18.log start
```

PostgreSQL이 켜져 있는지 확인합니다.

```bash
pg_isready -h localhost -p 5432
```

현재 프로젝트는 `.env` 또는 `application-local.yml`의 DB 설정을 사용합니다. 로컬 DB는 다음 값에 맞춰 준비되어 있어야 합니다.

```text
DB_HOST=localhost
DB_PORT=5432
DB_NAME=spring_board
DB_USERNAME=<your local DB user>
DB_PASSWORD=<your local DB password>
```

DB를 중지하려면 다음 명령을 사용합니다.

```bash
pg_ctl -D /opt/homebrew/var/postgresql@18 stop -m fast
```

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.1/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.1/gradle-plugin/packaging-oci-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.1/reference/web/servlet.html)
* [Thymeleaf](https://docs.spring.io/spring-boot/4.0.1/reference/web/servlet.html#web.servlet.spring-mvc.template-engines)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.0.1/reference/data/sql.html#data.sql.jpa-and-spring-data)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
