Feature: Create Client
  As a system administrator
  I want to register new clients
  So that they can use the charging network

  Scenario: Register a new client
    Given the system is initialized
    When I register a client with ID 1, name "John Doe" and email "john@example.com"
    Then the client should be saved in the system
    And the client should have ID 1
    And the client should have name "John Doe"
    And the client should have email "john@example.com"
    And the client should have zero balance

  Scenario: Register multiple clients
    Given the system is initialized
    When I register client ID 1 "Alice" with email "alice@test.com"
    And I register client ID 2 "Bob" with email "bob@test.com"
    Then the system should have 2 clients
