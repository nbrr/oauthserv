# Testing
Partial analysis of OAuth 2.0 spec in order to write tests.

## Testing values
### Registered Clients
    id = 123e4567-e89b-12d3-a456-556642440000
    type = confidential
    secret = client123
    scope = ceci cela
    redirectionUri = https://example.com

### Resource Owners
    id = alice
    name = Alice
    secret = Alice123
---
    id = bob
    name = Bob
    secret = Bob123

## Grant Types
### Authorization Code Grant
#### Authorization Request
##### Nominal cases
All parameters are passed accordingly with the specification, and their values
are such that they do not raise errors.
  
###### Scenario: all parameters included
    http://localhost:8080/authorization?response_type=code&client_id=123e4567-e89b-12d3-a456-556642440000&redirect_uri=https://google.com&state=42&scope=ceci cela

###### Degenerate cases
Missing parameters.
Non valid value to parameter.

#### Token Request
##### Nominal cases
All parameters are passed accordingly with the specification and their values
are such that they do not raise errors.
###### Scenario: all parameters included, client is confidential
    curl -v -X POST \
    --data-urlencode "grant_type=authorization_code"\
    --data-urlencode "client_id=123e4567-e89b-12d3-a456-556642440000"\
    --data-urlencode "redirect_uri=https://google.com"\
    --data-urlencode "code=CODE_FROM_AUTHORIZATION"\
    --data-urlencode "client_secret=client123"\
    "http://localhost:8080/token"

##### Degenerate cases
Missing parameters.
Non valid value to parameter.

### Implicit Grant
#### Authorization Request
##### Nominal cases
All parameters are passed accordingly with the specification, and their values
are such that they do not raise errors.

###### Scenario: all parameters included
    http://localhost:8080/authorization?response_type=token&client_id=123e4567-e89b-12d3-a456-556642440000&redirect_uri=https://example.com&state=42&scope=ceci cela

##### Degenerate cases
Missing parameters.
Non valid value to parameter.
