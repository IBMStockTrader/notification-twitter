FROM websphere-liberty:microProfile
COPY server.xml /config/server.xml
COPY build/libs/loyalty-level.war /config/apps/LoyaltyLevel.war
