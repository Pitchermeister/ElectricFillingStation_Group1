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
