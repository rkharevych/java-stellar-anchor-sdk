package org.stellar.reference.service.sep31

import java.util.*
import mu.KotlinLogging
import org.stellar.reference.ClientException
import org.stellar.reference.NotFoundException
import org.stellar.reference.data.*
import org.stellar.reference.data.Sep31Type.SEP31_RECEIVER
import org.stellar.reference.data.Status.ACCEPTED
import org.stellar.reference.data.Status.NEEDS_INFO
import org.stellar.reference.repo.CustomerRepo

private val log = KotlinLogging.logger {}

class CustomerService(private val customerRepo: CustomerRepo) {

  fun getCustomer(request: GetCustomerRequest): GetCustomerResponse {
    val maybeCustomer: Customer?
    if (request.id != null) {
      maybeCustomer = customerRepo.findById(request.id)
      if (maybeCustomer == null) {
        throw NotFoundException(String.format("customer for 'id' '%s' not found", request.id))
      }
    } else {
      maybeCustomer =
        customerRepo.findByStellarAccountAndMemoAndMemoType(
          request.account,
          request.memo,
          request.memoType
        )
      if (maybeCustomer == null) {
        return createNewCustomerResponse(request.type)
      }
    }
    log.info { "Getting $maybeCustomer" }
    return createExistingCustomerResponse(maybeCustomer, request.type)
  }

  /**
   * If the customer is found, update the customer. Otherwise, create and insert a new customer.
   *
   * @param request the PUT customer request.
   * @return the PUT Customer response.
   */
  fun upsertCustomer(request: PutCustomerRequest): PutCustomerResponse {
    val customer: Customer
    if (request.id != null) {
      customer = getCustomerByRequestId(request.id)
      updateCustomer(customer, request)
    } else {
      val maybeCustomer =
        customerRepo.findByStellarAccountAndMemoAndMemoType(
          request.account,
          request.memo,
          request.memoType
        )
      if (maybeCustomer == null) {
        customer = createCustomer(request)
      } else {
        customer = maybeCustomer
        updateCustomer(customer, request)
      }
    }
    return PutCustomerResponse(customer.id!!)
  }

  fun delete(customerId: String) {
    customerRepo.deleteById(customerId)
  }

  /**
   * ATTENTION: this function is used for testing purposes only.
   *
   * <p>This method is used to delete a customer's `clabe_number`, which would make its state change
   * to NEEDS_INFO if it's a receiving customer.
   *
   * @param customerId is the id of the customer whose `clabe_number` will be deleted.
   * @throws ClientException if the user was not found.
   */
  fun invalidateCustomerClabe(customerId: String) {
    val maybeCustomer =
      customerRepo.findById(customerId)
        ?: throw NotFoundException(String.format("customer for 'id' '%s' not found", customerId))

    maybeCustomer.clabeNumber = null
    customerRepo.save(maybeCustomer)
  }

  private fun getCustomerByRequestId(id: String): Customer {
    val maybeCustomer = customerRepo.findById(id)
    val notFoundMessage = String.format("customer for 'id' '%s' not found", id)
    if (maybeCustomer == null) {
      throw NotFoundException(notFoundMessage)
    }
    return maybeCustomer
  }

  private fun createNewCustomerResponse(type: String?): GetCustomerResponse {
    val fields: MutableMap<String, CustomerField> = getBasicFields()
    // type can be null.
    if (SEP31_RECEIVER.toString() == type) {
      fields.putAll(getSep31ReceiverFields(type))
    }
    return GetCustomerResponse(null, NEEDS_INFO.toString(), fields, null, null)
  }

  private fun createExistingCustomerResponse(
    customer: Customer,
    type: String?
  ): GetCustomerResponse {
    val providedFields = mutableMapOf<String, ProvidedCustomerField>()
    val fields = mutableMapOf<String, CustomerField>()
    if (customer.firstName != null) {
      providedFields["first_name"] = createFirstNameProvidedField()
    } else {
      fields["first_name"] = createFirstNameField()
    }
    if (customer.lastName != null) {
      providedFields["last_name"] = createLastNameProvidedField()
    } else {
      fields["last_name"] = createLastNameField()
    }
    if (customer.email != null) {
      providedFields["email_address"] = createEmailProvidedField()
    } else {
      fields["email_address"] = createEmailField()
    }
    if (SEP31_RECEIVER.toString() == type) {
      if (customer.bankAccountNumber != null) {
        providedFields["bank_account_number"] = createBankAccountNumberProvidedField()
      } else {
        fields["bank_account_number"] = createBankAccountNumberField(type)
      }
      if (customer.bankAccountType != null) {
        providedFields["bank_account_type"] = createBankAccountTypeProvidedField()
      } else {
        fields["bank_account_type"] = createBankAccountTypeField(type)
      }
      if (customer.bankRoutingNumber != null) {
        providedFields["bank_number"] = createBankNumberProvidedField()
      } else {
        fields["bank_number"] = createBankNumberField(type)
      }
      if (customer.clabeNumber != null) {
        providedFields["clabe_number"] = createClabeNumberProvidedField()
      } else {
        fields["clabe_number"] = createClabeNumberField(type)
      }
    }

    val status = if (fields.isNotEmpty()) NEEDS_INFO else ACCEPTED
    return GetCustomerResponse(customer.id, status.toString(), fields, providedFields, null)
  }

  private fun createCustomer(request: PutCustomerRequest): Customer {
    val customer = Customer(null, null, null, null, null, null, null, null, null, null, null)
    customer.id = UUID.randomUUID().toString()
    updateCustomer(customer, request)
    return customer
  }

  private fun updateCustomer(customer: Customer, request: PutCustomerRequest) {
    customer.stellarAccount = request.account
    customer.memo = request.memo
    customer.memoType = request.memoType

    if (request.firstName != null) {
      customer.firstName = request.firstName
    }
    if (request.lastName != null) {
      customer.lastName = request.lastName
    }
    if (request.emailAddress != null) {
      customer.email = request.emailAddress
    }
    if (request.bankAccountNumber != null) {
      customer.bankAccountNumber = request.bankAccountNumber
    }
    if (request.bankAccountType != null) {
      customer.bankAccountType = request.bankAccountType
    }
    if (request.bankNumber != null) {
      customer.bankRoutingNumber = request.bankNumber
    }
    if (request.clabeNumber != null) {
      customer.clabeNumber = request.clabeNumber
    }
    customerRepo.save(customer)
  }

  private fun getBasicFields(): MutableMap<String, CustomerField> {
    val map = mutableMapOf<String, CustomerField>()
    map["first_name"] = createFirstNameField()
    map["last_name"] = createLastNameField()
    map["email_address"] = createEmailField()
    return map
  }

  private fun getSep31ReceiverFields(type: String): Map<String, CustomerField> {
    val map = mutableMapOf<String, CustomerField>()
    map["bank_account_number"] = createBankAccountNumberField(type)
    map["bank_account_type"] = createBankAccountTypeField(type)
    map["bank_number"] = createBankNumberField(type)
    map["clabe_number"] = createClabeNumberField(type)
    return map
  }

  private fun createFirstNameField(): CustomerField {
    return CustomerField("string", "first name of the customer", null, false)
  }

  private fun createLastNameField(): CustomerField {
    return CustomerField("string", "last name of the customer", null, false)
  }

  private fun createEmailField(): CustomerField {
    return CustomerField("string", "email of the customer", null, false)
  }

  private fun createBankAccountNumberField(type: String): CustomerField {
    return CustomerField(
      "string",
      "bank account number of the customer",
      null,
      SEP31_RECEIVER.toString() != type
    )
  }

  private fun createBankAccountTypeField(type: String): CustomerField {
    return CustomerField(
      "string",
      "bank account type of the customer",
      listOf("checking", "savings"),
      SEP31_RECEIVER.toString() != type
    )
  }

  private fun createBankNumberField(type: String): CustomerField {
    return CustomerField(
      "string",
      "bank routing number of the customer",
      null,
      SEP31_RECEIVER.toString() != type
    )
  }

  private fun createClabeNumberField(type: String): CustomerField {
    return CustomerField(
      "string",
      "Bank account number for Mexico",
      null,
      SEP31_RECEIVER.toString() != type
    )
  }

  private fun createFirstNameProvidedField(): ProvidedCustomerField {
    return ProvidedCustomerField(
      "string",
      "first name of the customer",
      null,
      null,
      ACCEPTED.toString(),
      null
    )
  }

  private fun createLastNameProvidedField(): ProvidedCustomerField {
    return ProvidedCustomerField(
      "string",
      "last name of the customer",
      null,
      null,
      ACCEPTED.toString(),
      null
    )
  }

  private fun createEmailProvidedField(): ProvidedCustomerField {
    return ProvidedCustomerField(
      "string",
      "email of the customer",
      null,
      null,
      ACCEPTED.toString(),
      null
    )
  }

  private fun createBankAccountNumberProvidedField(): ProvidedCustomerField {
    return ProvidedCustomerField(
      "string",
      "bank account number of the customer",
      null,
      null,
      ACCEPTED.toString(),
      null
    )
  }

  private fun createBankAccountTypeProvidedField(): ProvidedCustomerField {
    return ProvidedCustomerField(
      "string",
      "bank account type of the customer",
      listOf("checking", "savings"),
      null,
      ACCEPTED.toString(),
      null
    )
  }

  private fun createBankNumberProvidedField(): ProvidedCustomerField {
    return ProvidedCustomerField(
      "string",
      "bank routing number of the customer",
      null,
      null,
      ACCEPTED.toString(),
      null
    )
  }

  private fun createClabeNumberProvidedField(): ProvidedCustomerField {
    return ProvidedCustomerField(
      "string",
      "bank account number for Mexico",
      null,
      null,
      ACCEPTED.toString(),
      null
    )
  }
}
