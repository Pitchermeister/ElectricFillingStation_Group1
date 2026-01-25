Feature: Update Prepaid Credit
  As a customer
  I want to top up my prepaid account
  So that I can charge my vehicle

  Scenario: Top up prepaid account
    Given the update-credit service is initialized
    And an update-credit customer "Alice" exists
    When the update-credit customer "Alice" tops up 50.00 EUR
    Then the update-credit customer "Alice" balance should be 50.00 EUR

  Scenario: Multiple top-ups accumulate
    Given the update-credit service is initialized
    And an update-credit customer "Alice" exists
    When the update-credit customer "Alice" tops up 50.00 EUR
    And the update-credit customer "Alice" tops up 25.00 EUR
    Then the update-credit customer "Alice" balance should be 75.00 EUR

  Scenario: Top up before charging
    Given the update-credit service is initialized
    And an update-credit customer "Alice" exists with balance 0.00 EUR
    When the update-credit customer "Alice" tops up 100.00 EUR
    Then the update-credit customer "Alice" should be able to charge
