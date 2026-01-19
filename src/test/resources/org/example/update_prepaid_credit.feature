Feature: Update Prepaid Credit
  As a customer
  I want to top up my prepaid account
  So that I can charge my vehicle

  Scenario: Top up prepaid account
    Given the system is initialized
    And a client with ID 1 exists
    When the client tops up €50.00
    Then the client balance should be €50.00

  Scenario: Multiple top-ups accumulate
    Given the system is initialized
    And a client with ID 1 exists
    When the client tops up €50.00
    And the client tops up €25.00
    Then the client balance should be €75.00

  Scenario: Top up before charging
    Given the system is initialized
    And a client with ID 1 exists with balance €0.00
    When the client tops up €100.00
    Then the client should be able to charge
