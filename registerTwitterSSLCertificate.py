# [7/31/19 19:27:41:676 UTC] SSL certificate and key management > SSL configurations > NodeDefaultSSLSettings > Key stores and certificates > NodeDefaultKeyStore > Signer certificates > Retrieve from port

AdminTask.retrieveSignerFromPort('[-keyStoreName NodeDefaultKeyStore -keyStoreScope (cell):DefaultCell01:(node):DefaultNode01 -host api.twitter.com -port 443 -certificateAlias twitter -sslConfigName NodeDefaultSSLSettings -sslConfigScopeName (cell):DefaultCell01:(node):DefaultNode01 ]')

AdminConfig.save()

