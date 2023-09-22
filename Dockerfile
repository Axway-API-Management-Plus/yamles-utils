ARG JRE_IMAGE=amd64/eclipse-temurin:11-jre-ubi9-minimal

FROM $JRE_IMAGE

ARG USERNAME=axway
ARG USER_UID=1000
ARG USER_GID=$USER_UID

COPY /target/yamles-utils-*.zip /tmp/yamles-utils.zip
RUN groupadd --gid ${USER_GID} ${USERNAME} \
 && useradd --uid ${USER_UID} --gid ${USER_GID} -m ${USERNAME} \
 && microdnf update -y \
 && microdnf install unzip -y \
 && mkdir -p /opt \
 && unzip /tmp/yamles-utils.zip -d /opt \
 && mv $(ls -d1 /opt/yamles-utils-*) /opt/yamles-utils \
 && chmod a+x /opt/yamles-utils/bin/yamlesutils.sh \
 && chown --recursive ${USER_UID}:${USER_GID} /opt

USER ${USERNAME}
ENTRYPOINT ["/opt/yamles-utils/bin/yamlesutils.sh"]
