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

 
  Scenario: Top up with minimum positive amount (edge case)
    Given the update-credit service is initialized
    And an update-credit customer "Alice" exists
    When the update-credit customer "Alice" tops up 0.01 EUR
    Then the update-credit customer "Alice" balance should be 0.01 EUR

  Scenario: Top up multiple times with fractional amounts (edge case)
    Given the update-credit service is initialized
    And an update-credit customer "Alice" exists
    When the update-credit customer "Alice" tops up 10.25 EUR
    And the update-credit customer "Alice" tops up 5.75 EUR
    And the update-credit customer "Alice" tops up 3.99 EUR
    Then the update-credit customer "Alice" balance should be 19.99 EUR

  Scenario: Top up after exhausting balance (edge case)
    Given the update-credit service is initialized
    And an update-credit customer "Alice" exists with balance 100.00 EUR
    And "Alice" spends 100.00 EUR on charging
    When the update-credit customer "Alice" tops up 75.50 EUR
    Then the update-credit customer "Alice" balance should be 75.50 EUR

  Scenario: Top up with very precise decimal (edge case)
    Given the update-credit service is initialized
    And an update-credit customer "Alice" exists
    When the update-credit customer "Alice" tops up 123.45 EUR
    Then the update-credit customer "Alice" balance should be 123.45 EUR