Feature: Read Prepaid Credit
  As a customer
  I want to check my prepaid balance
  So that I know how much credit I have

  Scenario: Check initial balance
    Given the read-credit service is initialized
    And a read-credit customer "Alice" exists
    When "Alice" checks their balance
    Then the balance should be 0.00 EUR

  Scenario: Check balance after top-up
    Given the read-credit service is initialized
    And a read-credit customer "Alice" exists
    And "Alice" tops up 100.00 EUR
    When "Alice" checks their balance
    Then the balance should be 100.00 EUR

  Scenario: Check balance after charging
    Given the read-credit service is initialized
    And a read-credit customer "Alice" exists with balance 50.00 EUR
    And "Alice" spends 10.00 EUR on charging
    When "Alice" checks their balance
    Then the balance should be 40.00 EUR

  Scenario: Top-up is credited only to the specified customer
    Given the read-credit service is initialized
    And a read-credit customer "Alice" exists
    And a read-credit customer "Peter" exists
    When "Peter" tops up 100.00 EUR
    And "Alice" checks their balance
    Then the balance should be 0.00 EUR
    When "Peter" checks their balance
    Then the balance should be 100.00 EUR

  Scenario: Check balance with multiple top-ups (edge case)
    Given the read-credit service is initialized
    And a read-credit customer "Alice" exists
    When "Alice" tops up 25.50 EUR
    And "Alice" tops up 50.25 EUR
    And "Alice" tops up 24.25 EUR
    Then the balance should be 100.00 EUR

  Scenario: Check balance after spending entire balance (edge case)
    Given the read-credit service is initialized
    And a read-credit customer "Alice" exists with balance 50.00 EUR
    And "Alice" spends 50.00 EUR on charging
    When "Alice" checks their balance
    Then the balance should be 0.00 EUR

  Scenario: Check balance with very small amount (edge case)
    Given the read-credit service is initialized
    And a read-credit customer "Alice" exists
    And "Alice" tops up 0.01 EUR
    When "Alice" checks their balance
    Then the balance should be 0.01 EUR

  Scenario: Multiple customers checking balance concurrently (edge case)
    Given the read-credit service is initialized
    And a read-credit customer "Alice" exists with balance 100.00 EUR
    And a read-credit customer "Bob" exists with balance 50.00 EUR
    And a read-credit customer "Carol" exists with balance 75.00 EUR
    When "Alice" checks their balance
    And "Bob" checks their balance
    And "Carol" checks their balance
    Then "Alice" balance should be 100.00 EUR
    And "Bob" balance should be 50.00 EUR
    And "Carol" balance should be 75.00 EUR