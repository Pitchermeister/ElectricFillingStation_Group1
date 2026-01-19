Feature: Read Prepaid Credit
  As a customer
  I want to check my prepaid balance
  So that I know how much credit I have

  Scenario: Check initial balance
    Given the system is initialized
    And a client with ID 1 exists
    When I check the client balance
    Then the balance should be €0.00

  Scenario: Check balance after top-up
    Given the system is initialized
    And a client with ID 1 exists
    And the client tops up €100.00
    When I check the client balance
    Then the balance should be €100.00

  Scenario: Check balance after charging
    Given the system is initialized
    And a client with ID 1 exists with balance €50.00
    And the client spends €10.00 on charging
    When I check the client balance
    Then the balance should be €40.00
