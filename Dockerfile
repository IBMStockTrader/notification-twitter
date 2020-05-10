#       Copyright 2017-2020 IBM Corp All Rights Reserved

#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at

#       http://www.apache.org/licenses/LICENSE-2.0

#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

# FROM websphere-liberty:microProfile3
FROM openliberty/open-liberty:kernel-java11-openj9-ubi

COPY --chown=1001:0 server.xml /config/server.xml
COPY --chown=1001:0 jvm.options /config/jvm.options
COPY --chown=1001:0 target/notification-twitter-1.0-SNAPSHOT.war /config/apps/NotificationTwitter.war
COPY --chown=1001:0 key.jks /config/resources/security/key.jks
# COPY --chown=1001:0 ltpa.keys /output/resources/security/ltpa.keys

RUN configure.sh
