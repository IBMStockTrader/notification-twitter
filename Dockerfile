FROM websphere-liberty:microProfile
COPY server.xml /config/server.xml
COPY target/loyalty-level-1.0-SNAPSHOT.war /config/apps/LoyaltyLevel.war
