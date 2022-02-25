package org.stellar.anchor.paymentservice.circle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import org.stellar.anchor.paymentservice.*;
import org.stellar.anchor.paymentservice.circle.config.CirclePaymentConfig;

public class CircleDemo {
  static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public static void main(String[] args) {
    System.out.println("=============");
    System.out.println("Hello, World!");
    System.out.println("_____________");

    CirclePaymentConfig config = PropertyCirclePaymentConfig.sandboxInstance();
    CirclePaymentService service = new CirclePaymentService(config);

    // ping()
    System.out.println("\n============= ping()");
    try {
      service.ping().block();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.out.println("Ping successful");
    System.out.println("_____________");

    // getDistributionAccountAddress()
    System.out.println("\n============= getDistributionAccountAddress()");
    String distributionAccountId = service.getDistributionAccountAddress().block();
    System.out.println("distributionAccountId = " + distributionAccountId);
    System.out.println("_____________");

    // getAccount("1000662797") merchantAccount
    System.out.println("\n============= getAccount(\"1000662797\") // merchantAccount");
    Account merchantAccount = service.getAccount("1000662797").block();
    System.out.println("merchantAccount = " + gson.toJson(merchantAccount));
    System.out.println("_____________");

    //    // createAccount()("Marcelo's wallet") newAccount
    //    System.out.println("\n============= createAccount()(\"Marcelo's wallet\") // newAccount");
    //    Account newAccount = service.createAccount("Marcelo's wallet").block();
    //    System.out.println("newAccount = " + gson.toJson(newAccount));
    //    System.out.println("_____________");

    // getAccount("1000667920") userAccount
    System.out.println("\n============= getAccount(\"1000667920\") // merchantAccount");
    Account userAccount = service.getAccount("1000667920").block();
    System.out.println("userAccount = " + gson.toJson(userAccount));
    System.out.println("_____________");

    //    // sendPayment(merchantAccount, userAccount, "circle:USD", BigDecimal.valueOf(10000)) //
    // paymentCircleToCircle
    //    System.out.println(
    //        "\n============= sendPayment(merchantAccount, userAccount, \"circle:USD\",
    // BigDecimal.valueOf(10000)) // paymentCircleToCircle");
    //    Payment paymentCircleToCircle = service.sendPayment(merchantAccount, userAccount,
    // "circle:USD", BigDecimal.valueOf(10000)).block();
    //    System.out.println("payment = " + gson.toJson(paymentCircleToCircle));
    //    System.out.println("_____________");

    // sendPayment(merchantAccount, stellarDestination, "circle:USDC...", BigDecimal.valueOf(100))
    // // paymentCircleToStellar
    //    System.out.println(
    //        "\n============= sendPayment(merchantAccount, stellarDestination, \"circle:USDC...\",
    // BigDecimal.valueOf(100)) // paymentCircleToStellar");
    //    Account stellarDestination =
    //        new Account(
    //            PaymentNetwork.STELLAR,
    //            "GAC2OWWDD75GCP4II35UCLYA7JB6LDDZUBZQLYANAVIHIRJAAQBSCL2S",
    //            new Account.Capabilities());
    //    String currencyName = "stellar:" + CircleAsset.stellarUSDC(Network.TESTNET);
    //    Payment paymentCircleToStellar =
    //        service
    //            .sendPayment(merchantAccount, stellarDestination, currencyName,
    // BigDecimal.valueOf(100))
    //            .block();
    //    System.out.println("payment = " + gson.toJson(paymentCircleToStellar));
    //    System.out.println("_____________");

    // service.sendPayment(merchantAccount, wireDestination, currencyName, BigDecimal.valueOf(200))
    // // paymentCircleToWire
    //    System.out.println(
    //        "\n============= sendPayment(merchantAccount, stellarDestination, \"circle:USDC...\",
    // BigDecimal.valueOf(100)) // paymentCircleToWire");
    //    Account wireDestination =
    //        new Account(
    //            PaymentNetwork.BANK_WIRE,
    //            "a4e76642-81c5-47ca-9229-ebd64efd74a7",
    //            "marcelo@test.com",
    //            new Account.Capabilities());
    //    Payment paymentCircleToWire =
    //        service
    //            .sendPayment(merchantAccount, wireDestination, "iso4217:USD",
    // BigDecimal.valueOf(200))
    //            .block();
    //    System.out.println("payment = " + gson.toJson(paymentCircleToWire));
    //    System.out.println("_____________");

    //    // getAccountPaymentHistory("1000662797", null, null) merchantAccountHistory
    //    System.out.println(
    //        "\n============= getAccountPaymentHistory(\"1000662797\", null, null) //
    // merchantAccountHistory");
    //    PaymentHistory merchantAccountHistory =
    //        service.getAccountPaymentHistory("1000662797", null, null).block();
    //    System.out.println("merchantAccountHistory = " + gson.toJson(merchantAccountHistory));
    //    System.out.println("_____________");

    //    // getAccountPaymentHistory("1000667920", null, null) userAccountHistory
    //    System.out.println(
    //        "\n============= getAccountPaymentHistory(\"1000667920\", null, null) ->
    // userAccountHistory");
    //    PaymentHistory userAccountHistory =
    //        service.getAccountPaymentHistory("1000667920", null, null).block();
    //    System.out.println("merchantAccountHistory = " + gson.toJson(userAccountHistory));
    //    System.out.println("_____________");

    //    // getDepositInstructions(...) circleDepositInstructions
    //    System.out.println(
    //        "\n============= getAccountPaymentHistory(...) -> circleDepositInstructions");
    //    DepositRequirements requirements =
    //        new DepositRequirements("1000667920", PaymentNetwork.CIRCLE, "circle:USD");
    //    DepositInstructions circleDepositInstructions =
    //        service.getDepositInstructions(requirements).block();
    //    System.out.println("circleDepositInstructions = " +
    // gson.toJson(circleDepositInstructions));
    //    System.out.println("_____________");
    //
    //    // getDepositInstructions(...) stellarDepositInstructions
    //    System.out.println(
    //        "\n============= getAccountPaymentHistory(...) -> stellarDepositInstructions");
    //    requirements = new DepositRequirements("1000667920", PaymentNetwork.STELLAR,
    // "circle:USD");
    //    DepositInstructions stellarDepositInstructions =
    //        service.getDepositInstructions(requirements).block();
    //    System.out.println("circleDepositInstructions = " +
    // gson.toJson(stellarDepositInstructions));
    //    System.out.println("_____________");

    // getDepositInstructions(...) wireDepositInstructions
    System.out.println("\n============= getAccountPaymentHistory(...) -> wireDepositInstructions");
    DepositRequirements requirements =
        new DepositRequirements(
            "1000662797",
            null,
            PaymentNetwork.BANK_WIRE,
            "a4e76642-81c5-47ca-9229-ebd64efd74a7",
            "circle:USD");
    DepositInstructions wireDepositInstructions =
        service.getDepositInstructions(requirements).block();
    System.out.println("circleDepositInstructions = " + gson.toJson(wireDepositInstructions));
    System.out.println("_____________");
  }
}

@Data
class PropertyCirclePaymentConfig implements CirclePaymentConfig {
  private String name = "";
  private boolean enabled = false;
  private String circleUrl;
  private String secretKey;
  private String horizonUrl;
  private String stellarNetwork = "TESTNET";

  public static PropertyCirclePaymentConfig sandboxInstance() {
    PropertyCirclePaymentConfig config = new PropertyCirclePaymentConfig();
    config.setName("circle");
    config.setEnabled(true);
    config.setCircleUrl("https://api-sandbox.circle.com/");
    config.setSecretKey(
        "QVBJX0tFWToyYzgwYmU4NDhlYWI5MTBiZDVkMDI1YmM4YzY5NTkwZDo5ZjQzMzQ4NDY4ZTA2NjBkZjM0ODQwYjk1NmM5MWFiZQ==");
    config.setHorizonUrl("https://horizon-testnet.stellar.org");
    config.setStellarNetwork("TESTNET");
    return config;
  }
}
