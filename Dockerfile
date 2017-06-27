FROM websphere-liberty:microProfile
COPY server.xml /config/server.xml
COPY LoyaltyLevel.war /config/apps/LoyaltyLevel.war
