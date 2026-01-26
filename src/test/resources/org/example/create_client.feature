Feature: Create Client
  As a system administrator
  I want to register new clients
  So that they can use the charging network

  Scenario: Register a new client
    # UPDATED LINE:
    Given the client management system is initialized
    When I register a client with name "John Doe" and email "john@example.com"
    Then the client should be saved in the system
    And the client should have name "John Doe"
    And the client should have email "john@example.com"
    And the client should have zero balance

  Scenario: Register multiple clients
    # UPDATED LINE:
    Given the client management system is initialized
    When I register client "Alice" with email "alice@test.com"
    And I register client "Bob" with email "bob@test.com"
    Then the system should have 2 clients

  Scenario: Register client with minimal name length (edge case)
    Given the client management system is initialized
    When I register a client with name "A" and email "a@example.com"
    Then the client should be saved in the system
    And the client should have name "A"

  Scenario: Register client with very long email (edge case)
    Given the client management system is initialized
    When I register a client with name "John Doe" and email "this.is.a.very.long.email.address.that.contains.many.characters@subdomain.example.co.uk"
    Then the client should be saved in the system
    And the client should have email "this.is.a.very.long.email.address.that.contains.many.characters@subdomain.example.co.uk"

  Scenario: Register client with duplicate email (error case)
    Given the client management system is initialized
    When I register a client with name "John Doe" and email "john@example.com"
    And I register a client with name "Jane Smith" and email "john@example.com"
    Then the system should have 2 clients
    And both clients should be registered (even with duplicate email)