Feature: Read Prepaid Credit
  As a customer
  I want to check my prepaid balance
  So that I know how much credit I have

  Scenario: Check initial balance
    # UPDATED: Unique wording
    Given the read-credit service is initialized
    # UPDATED: Unique wording
    And a read-credit client with ID 1 exists
    When I check the client balance
    # UPDATED: No symbol
    Then the balance should be 0.00 EUR

  Scenario: Check balance after top-up
    Given the read-credit service is initialized
    And a read-credit client with ID 1 exists
    # UPDATED: No symbol
    And the read-credit client tops up 100.00 EUR
    When I check the client balance
    Then the balance should be 100.00 EUR

  Scenario: Check balance after charging
    Given the read-credit service is initialized
    # UPDATED: No symbol
    And a read-credit client with ID 1 exists with balance 50.00 EUR
    # UPDATED: No symbol
    And the read-credit client spends 10.00 EUR on charging
    When I check the client balance
    Then the balance should be 40.00 EUR