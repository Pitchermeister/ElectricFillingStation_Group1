Feature: Update Prepaid Credit
  As a customer
  I want to top up my prepaid account
  So that I can charge my vehicle

  Scenario: Top up prepaid account
    # UPDATED: Unique wording
    Given the update-credit service is initialized
    # UPDATED: Unique wording
    And an update-credit client with ID 1 exists
    # UPDATED: No symbol
    When the update-credit client tops up 50.00 EUR
    Then the client balance should be 50.00 EUR

  Scenario: Multiple top-ups accumulate
    Given the update-credit service is initialized
    And an update-credit client with ID 1 exists
    When the update-credit client tops up 50.00 EUR
    And the update-credit client tops up 25.00 EUR
    Then the client balance should be 75.00 EUR

  Scenario: Top up before charging
    Given the update-credit service is initialized
    And an update-credit client with ID 1 exists with balance 0.00 EUR
    When the update-credit client tops up 100.00 EUR
    Then the client should be able to charge