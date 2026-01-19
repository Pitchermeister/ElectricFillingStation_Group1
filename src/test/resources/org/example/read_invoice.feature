Feature: Read Invoice
  As a customer
  I want to view my invoice history
  So that I can track my charging expenses

  Scenario: View invoice after charging
    # UPDATED: Unique wording
    Given the invoice service is initialized
    # UPDATED: Unique wording + removed symbols
    And an invoicing client with ID 1 exists with balance 100.00 EUR
    And an invoicing location with ID 1 "City Center" exists with 1 charger
    And the invoicing client completes a charging session
    When I request the invoice for client 1
    Then the invoice should show the session details
    And the invoice should show the location name
    And the invoice should show the charged kWh
    And the invoice should show the price

  Scenario: Invoice sorted by start time
    # UPDATED: Unique wording
    Given the invoice service is initialized
    And an invoicing client with ID 1 exists with balance 100.00 EUR
    And the invoicing client completes 3 sessions at different times
    When I request the invoice for client 1
    Then the sessions should be sorted chronologically
    And the invoice should show 3 items

  Scenario: Invoice shows balance information
    # UPDATED: Unique wording
    Given the invoice service is initialized
    And an invoicing client with ID 1 exists
    And the invoicing client has topped up 100.00 EUR total
    And the invoicing client has spent 30.00 EUR on charging
    When I request the invoice
    # UPDATED: Removed symbols
    Then the invoice should show total top-ups 100.00 EUR
    And the invoice should show total spent 30.00 EUR
    And the invoice should show remaining balance 70.00 EUR