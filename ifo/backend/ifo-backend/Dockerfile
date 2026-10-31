# Imagem do backend IFO (ADR-018). O Render usa este arquivo; o contexto é esta pasta.
# Nenhum segredo entra na imagem: DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET e CORS_ORIGINS
# chegam por variável de ambiente na hora de rodar.

# 1) Build: baixa as dependências (camada em cache) e gera o .jar sem rodar os testes
#    (os testes precisam do Docker/Testcontainers e rodam antes, na máquina do desenvolvedor)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -Dmaven.test.skip=true package

# 2) Execução: só o JRE 17 e o .jar, com usuário sem privilégios
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --no-create-home ifo
COPY --from=build /app/target/*.jar app.jar
USER ifo

# Fuso de Brasília: o dia do job das recorrências e o "hoje" do servidor dependem dele (UTC viraria o dia 3 h antes)
ENV TZ=America/Sao_Paulo
# Heap baixo para caber em ~512 MB (plano gratuito do Render); pode ser trocado por variável de ambiente
ENV JAVA_OPTS="-Xmx300m -Xss512k -XX:+UseSerialGC -Duser.timezone=America/Sao_Paulo"
# Perfil de produção por padrão (pool para o Neon, cabeçalhos do proxy); o Render injeta PORT
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
