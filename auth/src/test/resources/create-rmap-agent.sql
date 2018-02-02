INSERT into Users(name, email, isActive, rmapAgentUri, authKeyUri, doRMapAgentSync, createdDate, lastAccessedDate)
SELECT
  'RMap Test User', 'testuser@example.com', TRUE, 'rmap:testagenturi', 'http://rmap-hub.org/authids/testauthid', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT * FROM Users WHERE rmapAgentUri = 'rmap:testagenturi'
);

INSERT into UserIdentityProviders(identityProvider, providerAccountId, providerAccountPublicId, providerAccountDisplayName, providerAccountProfileUrl, userId, createdDate, LASTAUTHENTICATEDDATE)
SELECT
  'http://exampleidprovider.org', '1234567890', 'rmaptestuser', 'RMap Test User', 'https://exampleidprovider.org/rmaptestuser', (select userId from Users where name='RMap Test User'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT * FROM UserIdentityProviders WHERE providerAccountId = '1234567890'
);

INSERT into ApiKeys(accessKey, secret, keyUri, label, note, keyStatus, includeInEvent, userId, createdDate, lastModifiedDate)
SELECT
  'uah2CKDaBsEw3cEQ', 'NSbdzctrP46ZvhTi', 'rmap:testkeyuri', 'RMap test key', 'Test key for RMap', 'ACTIVE', FALSE, (select userId from Users where name='RMap Test User'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT * FROM ApiKeys WHERE keyUri = 'rmap:testkeyuri'
);