FROM websphere-liberty:microProfile
COPY server.xml /config/server.xml
COPY loyalty-level.war /config/apps/LoyaltyLevel.war
