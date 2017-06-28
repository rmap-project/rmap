INSERT into Users(name, email, isActive, rmapAgentUri, authKeyUri, doRMapAgentSync, createdDate, lastAccessedDate) values ('RMap Test User', 'testuser@example.com', TRUE, 'rmap:testagenturi', 'http://rmap-project.org/authids/testauthid', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT into UserIdentityProviders(identityProvider, providerAccountId, providerAccountPublicId, providerAccountDisplayName, providerAccountProfileUrl, userId, createdDate, LASTAUTHENTICATEDDATE)
values ('http://exampleidprovider.org', '1234567890', 'rmaptestuser', 'RMap Test User', 'https://exampleidprovider.org/rmaptestuser', (select userId from Users where name='RMap Test User'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT into ApiKeys(accessKey, secret, keyUri, label, note, keyStatus, includeInEvent, userId, createdDate, lastModifiedDate)
values ('uah2CKDaBsEw3cEQ', 'NSbdzctrP46ZvhTi', 'rmap:testkeyuri', 'RMap test key', 'Test key for RMap', 'ACTIVE', FALSE, (select userId from Users where name='RMap Test User'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);