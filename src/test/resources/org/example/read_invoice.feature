Feature: Read Invoice
  As a customer
  I want to view my invoice history
  So that I can track my charging expenses

  Scenario: View invoice after charging
    Given the system is initialized
    And a client with ID 1 exists with balance €100.00
    And a location with ID 1 "City Center" exists with 1 charger
    And the client completes a charging session
    When I request the invoice for client 1
    Then the invoice should show the session details
    And the invoice should show the location name
    And the invoice should show the charged kWh
    And the invoice should show the price

  Scenario: Invoice sorted by start time
    Given the system is initialized
    And a client with ID 1 exists with balance €100.00
    And the client completes 3 sessions at different times
    When I request the invoice for client 1
    Then the sessions should be sorted chronologically
    And the invoice should show 3 items

  Scenario: Invoice shows balance information
    Given the system is initialized
    And a client with ID 1 exists
    And the client has topped up €100.00 total
    And the client has spent €30.00 on charging
    When I request the invoice
    Then the invoice should show total top-ups €100.00
    And the invoice should show total spent €30.00
    And the invoice should show remaining balance €70.00
