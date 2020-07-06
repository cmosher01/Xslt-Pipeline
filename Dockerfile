FROM openjdk:14-oracle AS build

MAINTAINER Christopher A. Mosher <cmosher01@gmail.com>

USER root
ENV HOME /root
WORKDIR $HOME

RUN echo "org.gradle.daemon=false" >gradle.properties

COPY gradle/ gradle/
COPY gradlew ./
RUN ./gradlew --version

COPY settings.gradle ./
COPY build.gradle ./
COPY src/ ./src/

RUN ./gradlew build



FROM openjdk:14-oracle

USER root
ENV HOME /root
WORKDIR $HOME

COPY --from=build /root/build/distributions/*.tar ./
RUN tar xvf *.tar --strip-components=1 -C /usr/local

RUN useradd user
USER user
ENV HOME /home/user
WORKDIR $HOME

ENTRYPOINT ["xslt-pipeline"]
