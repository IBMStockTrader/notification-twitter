package com.ibm.hybrid.cloud.sample.portfolio;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.enterprise.context.ApplicationScoped;

//jwa: switched from no-longer-maintained com.bettercloud.vault to io.github.jopenlibs.vault version
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.response.AuthResponse;

@ApplicationScoped
public class VaultDriver {

    private String role = System.getenv("VAULT_ROLE");
    private String address = System.getenv("VAULT_ADDRESS");
    private String jwtPath = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    private Vault vault;

    private String readFile(String filename) {
        String jwt = "";
        try {
          File myObj = new File(filename);
          Scanner myReader = new Scanner(myObj);
          while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            jwt += data;
          }
          myReader.close();
        } catch (FileNotFoundException e) {
          System.out.println("An error occurred.");
          e.printStackTrace();
        }
        return jwt;
      }
    
    public VaultDriver() {
        String jwt = readFile(jwtPath);
        try {
            VaultConfig config = new VaultConfig().address(address).build();
            vault = new Vault(config);
            AuthResponse response = vault.auth().loginByKubernetes(role, jwt);
            config.token(response.getAuthClientToken());
            vault = new Vault(config);
        } catch (VaultException e) {
            e.printStackTrace();
        }
    }

    public Vault getDriver() {
        return vault;
    }
}
